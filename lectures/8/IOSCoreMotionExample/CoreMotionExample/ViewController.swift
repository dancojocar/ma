import UIKit
import CoreMotion
import SceneKit

class ViewController: UIViewController {
    let motionManager = CMMotionManager()
    var sceneView: SCNView!
    var cubeNode: SCNNode!

    override func viewDidLoad() {
        super.viewDidLoad()

        // Set up the SceneKit view
        sceneView = SCNView(frame: self.view.bounds)
        self.view.addSubview(sceneView)
        
        let scene = SCNScene()
        sceneView.scene = scene
        sceneView.allowsCameraControl = true
        sceneView.backgroundColor = .black
        
        // Add a camera
        let cameraNode = SCNNode()
        cameraNode.camera = SCNCamera()
        cameraNode.position = SCNVector3(x: 0, y: 0, z: 10)
        scene.rootNode.addChildNode(cameraNode)
        
        // Add a light
        let lightNode = SCNNode()
        lightNode.light = SCNLight()
        lightNode.light?.type = .omni
        lightNode.position = SCNVector3(x: 0, y: 10, z: 10)
        scene.rootNode.addChildNode(lightNode)
        
        // Add the colorful cube
        let cubeGeometry = SCNBox(width: 1, height: 1, length: 1, chamferRadius: 0)
        let colors: [UIColor] = [.red, .green, .blue, .yellow, .purple, .cyan]
        var materials: [SCNMaterial] = []
        
        for color in colors {
            let material = SCNMaterial()
            material.diffuse.contents = color
            materials.append(material)
        }
        
        cubeGeometry.materials = materials
        cubeNode = SCNNode(geometry: cubeGeometry)
        scene.rootNode.addChildNode(cubeNode)
        
        // Set autoresizing mask
//        sceneView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        // Start motion updates
        startMotionUpdates()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        sceneView.frame = self.view.bounds

//        if let camera = sceneView.pointOfView?.camera {
//            camera.fieldOfView = Double(sceneView.bounds.width / sceneView.bounds.height)
//        }
    }
    
    func startMotionUpdates() {
        guard motionManager.isDeviceMotionAvailable else {
            print("Device motion is not available")
            return
        }
        
        motionManager.deviceMotionUpdateInterval = 0.02 // 50 Hz
        motionManager.startDeviceMotionUpdates(using: .xArbitraryZVertical, to: .main) { [weak self] motion, error in
            guard let self = self, let motion = motion else {
                if let error = error {
                    print("Motion update error: \(error)")
                }
                return
            }
            DispatchQueue.main.async {
                self.updateCubeRotation(with: motion)
            }
        }
    }

    func updateCubeRotation(with motion: CMDeviceMotion) {
        // Get quaternion from device attitude
        let quaternion = motion.attitude.quaternion
        
        // Apply quaternion to the cube node
        cubeNode.orientation = SCNQuaternion(
            x: Float(quaternion.x),
            y: Float(quaternion.y),
            z: Float(quaternion.z),
            w: Float(quaternion.w)
        )
    }
}
