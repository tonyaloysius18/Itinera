import SwiftUI
import FirebaseCore
import GoogleSignIn
import EventKitUI
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
                onResult(nil, nil)
                return
            }
            GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
                if let error = error {
                    print("Google sign-in error: \(error.localizedDescription)")
                    onResult(nil, nil)
                    return
                }
                let idToken = result?.user.idToken?.tokenString
                let accessToken = result?.user.accessToken.tokenString
                onResult(idToken, accessToken)
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
        // The app foregrounds automatically. (Deep-linking handled in Part 2.)
        completionHandler()
    }
}
