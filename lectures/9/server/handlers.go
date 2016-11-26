package main

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gorilla/mux"
	"io/ioutil"
	"io"
	"strconv"
	"hash/fnv"
	"strings"
	"encoding/binary"
)

func Index(w http.ResponseWriter, r *http.Request, next http.HandlerFunc) {
	fmt.Fprintln(w, "Welcome!")
}

func PortfolioIndex(w http.ResponseWriter, r *http.Request, next http.HandlerFunc) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)

	if err := json.NewEncoder(w).Encode(portfolios); err != nil {
		panic(err)
	}
}

func PortfolioShow(w http.ResponseWriter, r *http.Request, next http.HandlerFunc) {
	vars := mux.Vars(r)
	portfolioId := vars["portfolioId"]

	id, err := strconv.Atoi(portfolioId)
	if err == nil {
		p := RepoFindPortfolio(id)
		h := fnv.New32a()
		b := make([]byte, 8)
		binary.LittleEndian.PutUint64(b, uint64(p.LastModified))
		h.Write(b)
		hash := fmt.Sprint(h.Sum32())
		if match := r.Header.Get("If-None-Match"); match != "" {
			if strings.Contains(match, hash) {
				w.WriteHeader(http.StatusNotModified)
				return
			}
		}
		w.Header().Set("Etag", hash)
		w.Header().Set("Cache-Control", "max-age=2592000") // 30 days
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		w.WriteHeader(http.StatusOK)
		if err := json.NewEncoder(w).Encode(p); err != nil {
			panic(err)
		}
	} else {
		panic(err)
	}
}

func PortfolioCreate(w http.ResponseWriter, r *http.Request, next http.HandlerFunc) {
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