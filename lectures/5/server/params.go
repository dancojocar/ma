package main

type TokenAuthentication struct {
	Token string `json:"token" form:"token"`
	ok string `json:"ok" form:"ok"`
}
