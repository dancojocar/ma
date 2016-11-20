package main


import (
	"encoding/json"
	jwt "github.com/dgrijalva/jwt-go"
	"net/http"
	_ "github.com/dgrijalva/jwt-go/request"
	"github.com/dgrijalva/jwt-go/request"
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
	tokenRequest, err := request.ParseFromRequest(req, request.OAuth2Extractor, func(token *jwt.Token) (interface{}, error) {
		return authBackend.PublicKey, nil
	})
	if err != nil {
		return err
	}
	tokenString := req.Header.Get("Authorization")
	return authBackend.Logout(tokenString, tokenRequest)
}