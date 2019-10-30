package main

import (
	"encoding/json"
	"log"
	"net/http"
)

func Login(w http.ResponseWriter, r *http.Request, _ http.HandlerFunc) {
	requestUser := new(User)
	log.Printf("body: %s", r.Body, )
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&requestUser)
	if err != nil {
		panic(err)
	}

	log.Printf(
		"User: %s",
		requestUser.Username,
	)

	responseStatus, token := LoginService(requestUser)
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(responseStatus)
	_, err = w.Write(token)
	if err != nil {
		panic(err)
	}
}

func RefreshToken(w http.ResponseWriter, r *http.Request, _ http.HandlerFunc) {
	requestUser := new(User)
	decoder := json.NewDecoder(r.Body)
	err := decoder.Decode(&requestUser)
	if err != nil {
		panic(err)
	}

	w.Header().Set("Content-Type", "application/json")
	_, err = w.Write(RefreshTokenService(requestUser))
	if err != nil {
		panic(err)
	}
}

func Logout(w http.ResponseWriter, r *http.Request, _ http.HandlerFunc) {
	err := LogoutService(r)
	w.Header().Set("Content-Type", "application/json")
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
	} else {
		w.WriteHeader(http.StatusOK)
	}
}
