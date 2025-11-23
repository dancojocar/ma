import SpriteKit
import GameplayKit
import CoreMotion

class GameScene: SKScene {
    var airplane = SKSpriteNode()
    var motionManager = CMMotionManager()
    var destX: CGFloat  = 0.0
    var destY: CGFloat  = 0.0
    
    // Joystick nodes and state
    var joystickBase = SKShapeNode()
    var joystickKnob = SKShapeNode()
    var joystickActive = false
    var joystickVector = CGVector(dx: 0, dy: 0)
    let joystickRadius: CGFloat = 50.0

    override func didMove(to view: SKView) {
       
        // 1
        airplane = SKSpriteNode(imageNamed: "Airplane")
        airplane.position = CGPoint(
            x: frame.size.width/2,
            y: frame.size.height/2)
        self.addChild(airplane)
        
        // Setup virtual joystick in bottom-left corner
        setupJoystick()
        
        if motionManager.isAccelerometerAvailable {
            // 2
            motionManager.accelerometerUpdateInterval = 0.01
            motionManager.startAccelerometerUpdates(to: .main) {
                (data, error) in
                guard let data = data, error == nil else {
                    return
                }
                
                // 3
                let currentX = self.airplane.position.x
                self.destX = currentX + CGFloat(data.acceleration.x * 500)
                // 4
                let currentY = self.airplane.position.y
                self.destY = currentY + CGFloat(data.acceleration.y * 500)
            }
        }
    }
    
    override func update(_ currentTime: TimeInterval) {
        // Base destination from accelerometer
        var targetX = destX
        var targetY = destY
        
        // Apply additional offset from joystick vector
        let joystickStrength: CGFloat = 1500.0
        targetX += joystickVector.dx * joystickStrength
        targetY += joystickVector.dy * joystickStrength
        
        var action = SKAction.moveTo(x: targetX, duration: 10)
        airplane.run(action)
        action = SKAction.moveTo(y: targetY, duration: 10)
        airplane.run(action)
    }
    
    func setupJoystick() {
        let baseRadius = joystickRadius
        let knobRadius: CGFloat = joystickRadius / 2.0
        
        // Place joystick near bottom-right
        let basePosition = CGPoint(
            x: frame.maxX - baseRadius - 100,
            y: frame.minY + baseRadius + 30
        )
        
        joystickBase = SKShapeNode(circleOfRadius: baseRadius)
        joystickBase.position = basePosition
        joystickBase.lineWidth = 2.0
        joystickBase.strokeColor = .white
        joystickBase.fillColor = .clear
        joystickBase.zPosition = 10
        addChild(joystickBase)
        
        joystickKnob = SKShapeNode(circleOfRadius: knobRadius)
        joystickKnob.position = basePosition
        joystickKnob.lineWidth = 1.0
        joystickKnob.strokeColor = .white
        joystickKnob.fillColor = .gray
        joystickKnob.zPosition = 11
        addChild(joystickKnob)
    }
    
    func updateJoystick(with touchLocation: CGPoint) {
        // Vector from base to touch
        let dx = touchLocation.x - joystickBase.position.x
        let dy = touchLocation.y - joystickBase.position.y
        let distance = sqrt(dx*dx + dy*dy)
        
        var clampedDx = dx
        var clampedDy = dy
        
        // Clamp knob within joystick radius
        if distance > joystickRadius {
            let ratio = joystickRadius / distance
            clampedDx *= ratio
            clampedDy *= ratio
        }
        
        // Move knob
        joystickKnob.position = CGPoint(x: joystickBase.position.x + clampedDx,
                                        y: joystickBase.position.y + clampedDy)
        
        // Normalized vector (-1...1)
        joystickVector = CGVector(dx: clampedDx / joystickRadius,
                                  dy: clampedDy / joystickRadius)
    }
    
    func resetJoystick() {
        joystickActive = false
        joystickVector = CGVector(dx: 0, dy: 0)
        joystickKnob.position = joystickBase.position
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard let touch = touches.first else { return }
        let location = touch.location(in: self)
        
        // Activate joystick if touch starts within its base
        if joystickBase.contains(location) {
            joystickActive = true
            updateJoystick(with: location)
        }
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        guard joystickActive, let touch = touches.first else { return }
        let location = touch.location(in: self)
        updateJoystick(with: location)
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        resetJoystick()
    }
    
    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        resetJoystick()
    }
}
