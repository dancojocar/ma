package main

import (
	"net/http"
	"github.com/codegangsta/negroni"
	"github.com/gorilla/mux"
)

func NewRouter() *mux.Router {
	router := mux.NewRouter().StrictSlash(true)
	for _, route := range authRoutes {
		var handler http.Handler
		handler = negroni.New(
			negroni.HandlerFunc(RequireTokenAuthentication),
			negroni.HandlerFunc(route.HandlerFunc),
		)

		handler = Logger(handler, route.Name)

		router.Methods(route.Method).
			Path(route.Pattern).
			Name(route.Name).
			Handler(handler)

	}
	for _, route := range routes {
		var handler http.Handler
		handler =  negroni.New(route.HandlerFunc)

		handler = Logger(handler, route.Name)

		router.Methods(route.Method).
			Path(route.Pattern).
			Name(route.Name).
			Handler(handler)

	}

	return router
}