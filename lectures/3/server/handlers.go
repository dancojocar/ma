package main

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gorilla/mux"
	"io/ioutil"
	"io"
	"strconv"
	"time"
)

func Index(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintln(w, "Welcome!")
}

func PortfolioIndex(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)

	if err := json.NewEncoder(w).Encode(portfolios); err != nil {
		panic(err)
	}
}

func PortfolioShow(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	portfolioId := vars["portfolioId"]

	id, err := strconv.Atoi(portfolioId)
	if err == nil {
		p := RepoFindPortfolio(id)
		TimeFormat := "2006-01-02T15:04:05-07:00"
		if t, err := time.Parse(TimeFormat, r.Header.Get("If-Modified-Since")); err == nil && p.LastModified < (t.Add(1 * time.Second).Unix()) {
			h := w.Header()
			delete(h, "Content-Type")
			delete(h, "Content-Length")
			w.WriteHeader(http.StatusNotModified)
			return
		}
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		w.WriteHeader(http.StatusOK)
		if err := json.NewEncoder(w).Encode(p); err != nil {
			panic(err)
		}
	} else {
		panic(err)
	}
}

func PortfolioCreate(w http.ResponseWriter, r *http.Request) {
	var portfolio Portfolio
	body, err := ioutil.ReadAll(io.LimitReader(r.Body, 1048576))
	if err != nil {
		panic(err)
	}
	if err := r.Body.Close(); err != nil {
		panic(err)
	}
	if err := json.Unmarshal(body, &portfolio); err != nil {
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		w.WriteHeader(422) // unprocessable entity
		if err := json.NewEncoder(w).Encode(err); err != nil {
			panic(err)
		}
	}

	p := RepoCreatePortfolio(portfolio)
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusCreated)
	if err := json.NewEncoder(w).Encode(p); err != nil {
		panic(err)
	}
}