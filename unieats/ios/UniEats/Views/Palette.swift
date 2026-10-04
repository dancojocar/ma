import SwiftUI
import UIKit

/// Text and fill colours that keep at least 4.5:1 contrast. The system `.orange`, `.green` and
/// `.red` reach only about 2.2:1 on white (3.1:1 on their own tint), so in light mode text uses
/// darker shades; dark mode keeps the bright system colours, which are legible on black.
extension Color {
    /// Background behind white text, in both appearances.
    static let brandFill = Color(red: 0.66, green: 0.28, blue: 0)
    /// Second stop of the new rating badge gradient, also behind white text.
    static let accentFill = Color(red: 0.70, green: 0.10, blue: 0.35)
    static let brandText = Color.dynamic(light: UIColor(red: 0.66, green: 0.28, blue: 0, alpha: 1), dark: .systemOrange)
    static let openText = Color.dynamic(light: UIColor(red: 0.10, green: 0.45, blue: 0.18, alpha: 1), dark: .systemGreen)
    static let closedText = Color.dynamic(light: UIColor(red: 0.75, green: 0.10, blue: 0.10, alpha: 1), dark: .systemRed)

    private static func dynamic(light: UIColor, dark: UIColor) -> Color {
        Color(uiColor: UIColor { $0.userInterfaceStyle == .dark ? dark : light })
    }
}
