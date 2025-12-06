import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // 1. Create the window manually (standard for "No SceneDelegate" demos)
        window = UIWindow(frame: UIScreen.main.bounds)
        
        // 2. Load the Storyboard
        // Make sure your file is named "Main.storyboard"
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        
        // 3. Instantiate your ViewController
        // This grabs the View Controller marked as "Is Initial View Controller" in the Storyboard
        guard let rootVC = storyboard.instantiateInitialViewController() else {
            print("Error: Could not find initial view controller in Main.storyboard")
            return false
        }
        
        // 4. Make it the root and show it
        window?.rootViewController = rootVC
        window?.makeKeyAndVisible()
        
        return true
    }
    
}
