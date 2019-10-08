//: Playground - noun: a place where people can play

import UIKit
import PlaygroundSupport
import RxSwift

let a = Variable(1)
let b = Variable(2)

/**
 - Calculated Variable
 */

//let c = Observable.combineLatest(a.asObservable(),b.asObservable()){ var1,var2 in var1 + var2 }.filter{ var1 in var1 > 0 }.map({ var1 in "\(var1) is positive" })
//
//c.subscribe(onNext:{
//    item in print(item)
//})
//
//a.value = 4
//
//b.value = -8

/**
 - Simple UI bindings
 */

