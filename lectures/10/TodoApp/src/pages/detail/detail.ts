import {Component} from '@angular/core';
import {NavController, NavParams} from 'ionic-angular';

declare let FusionCharts;

/*
 Generated class for the Detail page.

 See http://ionicframework.com/docs/v2/components/#navigation for more info on
 Ionic pages and navigation.
 */
@Component({
  selector: 'page-detail',
  templateUrl: 'detail.html'
})
export class DetailPage {

  public index: any;
  public content: string;
  private data: any;

  static getRandom(min, max) {
    return Math.random() * (max - min) + min;
  }

  constructor(public navCtrl: NavController, public params: NavParams) {
    this.index = params.get("index");
    if (this.index > -1) {
      let todoList = JSON.parse(localStorage.getItem("todos"));
      if (todoList != null) {
        this.content = todoList[this.index];
        console.log("content: " + this.content);
        this.data = {
          "chart": {
            "caption": "Todos",
          },
          "data": todoList.map(function (obj) {
            let random = DetailPage.getRandom(1, 20);
            let newVar = {"label": obj, "value": random, "issliced": obj === this.content ? "1" : "0"};
            console.log("newVar: " + obj + " val: " + random);
            return newVar;
          }, this)
        };
      }
    }
  }

  ionViewDidLoad() {
    console.log('Hello DetailPage Page: ' + this.index);
    let jsonEncodedData = JSON.stringify(this.data);

    FusionCharts.ready(function () {
      let todoChart = new FusionCharts({
        type: 'pie2d',
        renderAt: 'chart-container',
        width: '100%',
        height: '80%',
        dataFormat: 'json',
        dataSource: jsonEncodedData
      });
      todoChart.render();
    });
  }

}
