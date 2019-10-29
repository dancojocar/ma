package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
)

var environments = map[string]string{
	"production":    "prod.json",
	"preproduction": "pre.json",
	"tests":         "test.json",
}

type Settings struct {
	PrivateKeyPath     string
	PublicKeyPath      string
	JWTExpirationDelta int
}

var settings = Settings{}
var env = "preproduction"

func LoadSettingsByEnv(env string) {
	fmt.Println("** Loading")
	content, err := ioutil.ReadFile(environments[env])
	if err != nil {
		fmt.Println("Error while reading config file", err)
	}
	settings = Settings{}
	jsonErr := json.Unmarshal(content, &settings)
	if jsonErr != nil {
		fmt.Println("Error while parsing config file", jsonErr)
	}
}

func (*Settings) GetEnvironment() string {
	return env
}

func (*Settings) Get() Settings {
	LoadSettingsByEnv("preproduction")
	return settings
}

func (*Settings) IsTestEnvironment() bool {
	return env == "tests"
}
