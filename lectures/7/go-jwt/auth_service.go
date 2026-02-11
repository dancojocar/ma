package main

import (
	"encoding/json"
	"strings"
	"github.com/golang-jwt/jwt/v5"
	"net/http"
)

func LoginService(requestUser *User) (int, []byte) {
	authBackend := InitJWTAuthenticationBackend()

	if authBackend.Authenticate(requestUser) {
		token, err := authBackend.GenerateToken(requestUser.UUID)
		if err != nil {
			return http.StatusInternalServerError, []byte("")
		} else {
			response, _ := json.Marshal(TokenAuthentication{token, "true"})
			return http.StatusOK, response
		}
	}

	return http.StatusUnauthorized, []byte("")
}

func RefreshTokenService(requestUser *User) []byte {
	authBackend := InitJWTAuthenticationBackend()
	token, err := authBackend.GenerateToken(requestUser.UUID)
	if err != nil {
		panic(err)
	}
	response, err := json.Marshal(TokenAuthentication{token, "true"})
	if err != nil {
		panic(err)
	}
	return response
}

func LogoutService(req *http.Request) error {
	authBackend := InitJWTAuthenticationBackend()
	tokenString := req.Header.Get("Authorization")
	cleanedTokenString := strings.TrimPrefix(tokenString, "Bearer ")
	tokenRequest, err := jwt.Parse(cleanedTokenString, func(token *jwt.Token) (interface{}, error) {
		return authBackend.PublicKey, nil
	})
	if err != nil {
		return err
	}
	tokenString := req.Header.Get("Authorization")
	return authBackend.Logout(tokenString, tokenRequest)
}
