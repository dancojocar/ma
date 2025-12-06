import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var animatableView: UIView!
    @IBOutlet weak var speedSlider: UISlider!
    @IBOutlet weak var speedLabel: UILabel!
    
    var isAnimationComplete = false

    override func viewDidLoad() {
        super.viewDidLoad()
        updateSpeedLabel()
        
        animatableView.layer.cornerRadius = 8
        
        animatableView.translatesAutoresizingMaskIntoConstraints = true
    }

    @IBAction func speedSliderChanged(_ sender: UISlider) {
        self.view.layer.speed = sender.value
        updateSpeedLabel()
    }
    
    func updateSpeedLabel() {
        speedLabel.text = String(format: "Speed: %.1fx", speedSlider.value)
    }

    @IBAction func startComplexAnimation(_ sender: Any) {
        
        let originalFrame = self.view.bounds.width - 100
        let startFrame = 50.0
        
        let targetX = isAnimationComplete ? startFrame : originalFrame
        let targetColor = isAnimationComplete ? UIColor.systemBlue : UIColor.systemOrange
        let targetRotation = isAnimationComplete ? 0.0 : CGFloat.pi // 180 degrees
        let targetScale: CGFloat = isAnimationComplete ? 1.0 : 1.5
        
        // We use animateKeyframes for multi-stage complexity
        UIView.animateKeyframes(withDuration: 2.0, delay: 0, options: [.calculationModeCubic], animations: {
            
            // STAGE 1: Move (0% to 50% of duration)
            // Starts at 0s, lasts 1 second (0.5 relative)
            UIView.addKeyframe(withRelativeStartTime: 0.0, relativeDuration: 0.5) {
                self.animatableView.center.x = targetX
            }
            
            // STAGE 2: Rotate & Color (20% to 70% of duration) - Overlapping!
            UIView.addKeyframe(withRelativeStartTime: 0.2, relativeDuration: 0.5) {
                self.animatableView.backgroundColor = targetColor
                self.animatableView.transform = CGAffineTransform(rotationAngle: targetRotation)
            }
            
            // STAGE 3: Scale "Bounce" (50% to 100%)
            UIView.addKeyframe(withRelativeStartTime: 0.5, relativeDuration: 0.5) {
                // We combine the rotation with a scale
                let rotation = CGAffineTransform(rotationAngle: targetRotation)
                self.animatableView.transform = rotation.scaledBy(x: targetScale, y: targetScale)
            }
            
        }) { _ in
            self.isAnimationComplete.toggle()
        }
    }
}
