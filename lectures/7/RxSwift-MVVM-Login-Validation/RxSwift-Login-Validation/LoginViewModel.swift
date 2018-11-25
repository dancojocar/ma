import Foundation
import RxSwift
struct LoginViewModel {
    var username = Variable<String>("")
    var password = Variable<String>("")
    
    var isValid: Observable<Bool>{
        return Observable.combineLatest(username.asObservable(),password.asObservable()){
            email,password in email.count >= 3 && password.count >= 3
        }
    }
    
    func loginPressed() {
        print("Login pressed")
    }
    
}
