import Foundation
import RxSwift
import RxCocoa
struct LoginViewModel {
    var username = BehaviorRelay<String>(value: "")
    var password = BehaviorRelay<String>(value: "")
    
    var isValid: Observable<Bool>{
        return Observable.combineLatest(username.asObservable(), password.asObservable()){
            email, password in email.count >= 3 && password.count >= 3
        }
    }
    
    func loginPressed() {
        print("Login pressed")
    }
    
}
