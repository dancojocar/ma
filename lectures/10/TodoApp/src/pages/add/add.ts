import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
 
@Component({
    templateUrl: 'add.html'
})
export class AddPage {
 
    public todoList: Array<string>;
    public todoItem: string;
 
    constructor(private nav: NavController) {
        this.todoList = JSON.parse(localStorage.getItem("todos"));
        if(!this.todoList) {
            this.todoList = [];
        }
        this.todoItem = "";
    }
 
    save() {
        if(this.todoItem != "") {
            this.todoList.push(this.todoItem);
            localStorage.setItem("todos", JSON.stringify(this.todoList));
            this.nav.pop();
        }
    }
 
}
