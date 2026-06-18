import SwiftUI
import FirebaseCore
import GoogleSignIn
import EventKitUI
import Shared

@main
struct iOSApp: App {

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

// Dismisses the EventKit edit sheet after the user saves or cancels.
final class CalendarEditDelegate: NSObject, EKEventEditViewDelegate {
    static let shared = CalendarEditDelegate()
    func eventEditViewController(_ controller: EKEventEditViewController,
                                 didCompleteWith action: EKEventEditViewAction) {
        controller.dismiss(animated: true)
    }
}
