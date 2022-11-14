package main

type TokenAuthentication struct {
	Token string `json:"token" form:"token"`
	Ok    string `json:"ok" form:"ok"`
}
