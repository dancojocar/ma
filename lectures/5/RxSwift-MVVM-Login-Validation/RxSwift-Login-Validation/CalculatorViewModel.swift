import Foundation
import RxSwift
import RxCocoa

struct CalculatorViewModel {
    var number1 = BehaviorRelay<Double>(value:0.0)
    var number2 = BehaviorRelay<Double>(value:0.0)
    
    var op = BehaviorRelay<Int>(value:0)
    
    var calcAnswer: Observable<Double>{
        return Observable.combineLatest(number1.asObservable(), number2.asObservable(), op.asObservable()){
            num1, num2, op in
            //print(op)

            switch op {
            case 0:
                return num1 + num2
            case 1:
                return num1 - num2
            case 2:
                return num1 * num2
            case 3:
                return num1 / num2
            default:
                return 0.0
            }
        }
    }
    
    var changeOperator: Observable<String>{
        return Observable.combineLatest([op.asObservable()]){ op in
            switch op[0] {
            case 0:
                return "+"
            case 1:
                return "-"
            case 2:
                return "*"
            case 3:
                return "/"
            default:
                return ""
            }
        }
    }
    
    
    func changeOperator(buttonId: Int) {
        self.op.accept(buttonId)
    }
}
