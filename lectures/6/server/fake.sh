#!/bin/sh
curl -H "Content-Type: application/json" -d '{"name":"Sim 101"}' http://localhost:8080/p

curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/p

curl -i -H "Accept: application/json" -X POST -d '{"Username":"test","Password":"test1"}' http://localhost:8080/token-auth

curl -i -H "Authorization: Bearer ..." -X GET http://192.168.2.2:8080/p

curl -H "Content-Type: application/json" -H "Authorization: key=AIzaSyBjCYxWYHoMlej2yJ9OCIW5TNnQhR5loRI" -X POST -d '{"to":"/topics/portfolio","notification": {"body":"TestBody","title":"TestTile","icon":"ic_menu_camera"}}' https://fcm.googleapis.com/fcm/send
