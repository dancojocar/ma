package main

import (
	"net/http"
	"html/template"
)

func EchoHome(w http.ResponseWriter, r *http.Request, next http.HandlerFunc) {
	homeTemplate.Execute(w, "ws://"+r.Host+"/echo")
}


var homeTemplate = template.Must(template.ParseFiles("home.html"))
