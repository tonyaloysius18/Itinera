import SwiftUI
import FirebaseCore
import GoogleSignIn
import Shared

@main
struct iOSApp: App {

    init() {
        FirebaseApp.configure()

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
