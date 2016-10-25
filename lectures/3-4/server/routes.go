package main

import "net/http"

type Route struct {
	Name        string
	Method      string
	Pattern     string
	HandlerFunc http.HandlerFunc
}

type Routes []Route

var routes = Routes{
	Route{
		"Index",
		"GET",
		"/",
		Index,
	},
	Route{
		"PortfolioIndex",
		"GET",
		"/p",
		PortfolioIndex,
	},
	Route{
		"PortfolioShow",
		"GET",
		"/p/{portfolioId}",
		PortfolioShow,
	},
	Route{
		"PortfolioCreate",
		"POST",
		"/p",
		PortfolioCreate,
	},
}