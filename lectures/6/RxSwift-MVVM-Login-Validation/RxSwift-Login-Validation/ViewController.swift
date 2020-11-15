import UIKit
import RxSwift
import RxCocoa

class ViewController: UIViewController {
    
    @IBOutlet weak var username: UITextField!
    @IBOutlet weak var password: UITextField!
    @IBOutlet weak var validitylabel: UILabel!
    @IBOutlet weak var loginbutton: UIButton!

    var loginViewModel = LoginViewModel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        _=username.rx.text.map{ $0 ?? "" }.bind(to: loginViewModel.username)
        _=password.rx.text.map{ $0 ?? "" }.bind(to: loginViewModel.password)
        
        _=loginViewModel.isValid.bind(to: loginbutton.rx.isEnabled)
        
        _=loginViewModel.isValid.subscribe(onNext: { [unowned self] isValid in
            self.validitylabel.text = isValid ? "Valid!" : "Login details are invalid!"
            self.validitylabel.textColor = isValid ? UIColor.green : UIColor.red
        })
        
        loginbutton.addTarget(self, action: #selector(self.loginTapped(_button:)), for: UIControl.Event.touchUpInside)
    }
    
    @objc func loginTapped(_button: UIButton) {
        let calcVC = self.storyboard?.instantiateViewController(withIdentifier: "CalculatorVC") as! CalculatorViewController
        self.present(calcVC, animated: true, completion: nil)
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

