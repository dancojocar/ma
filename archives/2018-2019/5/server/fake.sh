#!/bin/sh
curl -H "Content-Type: application/json" -d '{"name":"Sim 101"}' http://localhost:8080/p

curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET http://localhost:8080/p

curl -i -H "Accept: application/json" -X POST -d '{"Username":"test","Password":"test1"}' http://localhost:8080/token-auth

curl -i -H "Authorization: Bearer ..." -X GET http://localhost:8080/p
