import SwiftUI
import FirebaseCore
import GoogleSignIn
import EventKitUI
import Translation
import UniformTypeIdentifiers
import Shared
import UserNotifications

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(NotificationAppDelegate.self) var appDelegate

    init() {
        FirebaseApp.configure()

        // ===== Google Sign-In bridge =====
        IosGoogleSignIn.shared.provider = { onResult in
            guard let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController else {
                _ = onResult(nil, nil)
                return
            }
            GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
                if let error = error {
                    print("Google sign-in error: \(error.localizedDescription)")
                    _ = onResult(nil, nil)
                    return
                }
                let idToken = result?.user.idToken?.tokenString
                let accessToken = result?.user.accessToken.tokenString
                _ = onResult(idToken, accessToken)
            }
        }

        // ===== Calendar (EventKit) bridge =====
        IosCalendar.shared.provider = { title, location, startMillis, endMillis, allDay in
            let store = EKEventStore()
            let event = EKEvent(eventStore: store)
            event.title = title
            event.location = location
            event.startDate = Date(timeIntervalSince1970: Double(truncating: startMillis) / 1000.0)
            event.endDate = Date(timeIntervalSince1970: Double(truncating: endMillis) / 1000.0)
            event.isAllDay = allDay.boolValue
            event.calendar = store.defaultCalendarForNewEvents

            let editVC = EKEventEditViewController()
            editVC.event = event
            editVC.eventStore = store
            editVC.editViewDelegate = CalendarEditDelegate.shared

            if let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController {
                rootVC.present(editVC, animated: true)
            }
        }

        // ===== File picker (document) bridge — inside init() =====
        IosFilePicker.shared.provider = IosDocumentFilePickerProvider()

        // ===== Translation bridge (iOS 17.4+) =====
        if #available(iOS 17.4, *) {
            TranslatorBridge.shared.install()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}

// Holds a strong reference so the delegate isn't deallocated mid-pick.
final class DocPickerHolder {
    static let shared = DocPickerHolder()
    var delegate: DocPickerDelegate?
}

final class IosDocumentFilePickerProvider: FilePickerProvider {
    func pick(onResult: @escaping (String?, String?, String?) -> Void) {
        let delegate = DocPickerDelegate { base64, name, mime in
            onResult(base64, name, mime)
        }
        DocPickerHolder.shared.delegate = delegate   // keep it alive

        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [.item])
        picker.allowsMultipleSelection = false
        picker.delegate = delegate

        if let rootVC = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
            .first?.rootViewController {
            rootVC.present(picker, animated: true)
        }
    }
}

final class DocPickerDelegate: NSObject, UIDocumentPickerDelegate {
    private let onResult: (String?, String?, String?) -> Void
    init(onResult: @escaping (String?, String?, String?) -> Void) {
        self.onResult = onResult
    }

    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let url = urls.first else { onResult(nil, nil, nil); return }
        let didStart = url.startAccessingSecurityScopedResource()
        defer { if didStart { url.stopAccessingSecurityScopedResource() } }
        do {
            let data = try Data(contentsOf: url)
            let base64 = data.base64EncodedString()
            let name = url.lastPathComponent
            let mime = UTType(filenameExtension: url.pathExtension)?.preferredMIMEType ?? "application/octet-stream"
            onResult(base64, name, mime)
        } catch {
            onResult(nil, nil, nil)
        }
    }

    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        onResult(nil, nil, nil)
    }
}

// Dismisses the EventKit edit sheet after the user saves or cancels.
final class CalendarEditDelegate: NSObject, EKEventEditViewDelegate {
    static let shared = CalendarEditDelegate()
    func eventEditViewController(_ controller: EKEventEditViewController,
                                 didCompleteWith action: EKEventEditViewAction) {
        controller.dismiss(animated: true)
    }
}

// Handles notification presentation (foreground) and taps.
final class NotificationAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    // Show the banner + play sound even when the app is in the FOREGROUND.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .list])
    }

    // Fires when the user TAPS the notification.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        if let tripId = userInfo["tripId"] as? String, !tripId.isEmpty {
            PendingDeepLink.shared.tripId = tripId
        }
        completionHandler()
    }
}

/**
 * Bridges Apple's Translation framework (iOS 17.4+) to the Kotlin
 * `IosTranslator.provider`. Apple's API is SwiftUI-bound (`.translationTask`),
 * so we host a small SwiftUI view that owns a TranslationSession and processes
 * requests. The host is 1x1 (not alpha-0 / zero-frame) so the first-run
 * language-model download prompt can present.
 */
@available(iOS 17.4, *)
final class TranslatorBridge {
    static let shared = TranslatorBridge()

    struct Request {
        let text: String
        let source: String
        let target: String
        let onResult: (String?) -> Void
    }

    private var hostingController: UIHostingController<TranslatorHostView>?
    private let model = TranslatorModel()

    func install() {
        IosTranslator.shared.provider = { [weak self] text, source, target, onResult in
            guard let self else {
                _ = onResult(nil)
                return
            }
            DispatchQueue.main.async {
                self.ensureHostInstalled()
                self.model.enqueue(
                    Request(text: text, source: source, target: target) { result in
                        _ = onResult(result)
                    }
                )
            }
        }
    }

    private func ensureHostInstalled() {
        guard hostingController == nil else { return }

        let host = UIHostingController(rootView: TranslatorHostView(model: model))
        host.view.backgroundColor = .clear
        // 1x1 and effectively invisible, but a real, present view so Apple can
        // show its language-download confirmation if needed.
        host.view.frame = CGRect(x: 0, y: 0, width: 1, height: 1)
        host.view.isUserInteractionEnabled = false

        if let window = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
            .first {
            window.addSubview(host.view)
            window.sendSubviewToBack(host.view)
        }
        hostingController = host
    }
}

@available(iOS 17.4, *)
final class TranslatorModel: ObservableObject {
    @Published var configuration: TranslationSession.Configuration?
    private var pending: TranslatorBridge.Request?

    func enqueue(_ request: TranslatorBridge.Request) {
        pending = request

        let newSource = Locale.Language(identifier: request.source)
        let newTarget = Locale.Language(identifier: request.target)

        if let cfg = configuration,
           cfg.source == newSource,
           cfg.target == newTarget {
            // Same language pair — reuse the session, just re-run translate()
            // on the new text. invalidate() re-fires .translationTask without
            // tearing down (and re-downloading) the model.
            configuration?.invalidate()
        } else {
            // New (or first) language pair — set a fresh configuration.
            configuration = TranslationSession.Configuration(
                source: newSource,
                target: newTarget
            )
        }
    }

    func run(with session: TranslationSession) async {
        guard let request = pending else { return }
        print("ITINERA: iOS translating '\(request.text)' \(request.source)->\(request.target)")
        do {
            let response = try await session.translate(request.text)
            print("ITINERA: iOS got '\(response.targetText)'")
            request.onResult(response.targetText)
        } catch {
            print("ITINERA: iOS translate failed - \(error)")
            request.onResult(nil)
        }
        pending = nil
    }
}

@available(iOS 17.4, *)
struct TranslatorHostView: View {
    @ObservedObject var model: TranslatorModel

    var body: some View {
        Color.clear
            .frame(width: 1, height: 1)
            .translationTask(model.configuration) { session in
                await model.run(with: session)
            }
    }
}
