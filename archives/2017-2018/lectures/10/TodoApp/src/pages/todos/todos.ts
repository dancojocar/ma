import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';
import {AddPage} from "../add/add";
import {DetailPage} from "../detail/detail";

@Component({
  templateUrl: 'todos.html'
})
export class TodosPage {

  public todoList: Array<string>;

  constructor(private nav: NavController) {
  }

  ionViewDidEnter() {
    this.todoList = JSON.parse(localStorage.getItem("todos"));
    if (!this.todoList) {
      this.todoList = [];
    }
  }

  delete(index: number) {
    this.todoList.splice(index, 1);
    localStorage.setItem("todos", JSON.stringify(this.todoList));
  }

  add() {
    this.nav.push(AddPage);
  }

  detail(index: number){
    this.nav.push(DetailPage,{ index});
  }

}

