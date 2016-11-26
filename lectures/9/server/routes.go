package main

import (
	"github.com/codegangsta/negroni"
)

type Route struct {
	Name        string
	Method      string
	Pattern     string
	HandlerFunc negroni.HandlerFunc
}

type Routes []Route

var authRoutes = Routes{
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
	Route{
		"RefreshAuth",
		"GET",
		"/refresh-token-auth",
		RefreshToken,
	},
	Route{
		"Logout",
		"GET",
		"/logout",
		Logout,
	},
}

var routes = Routes{

	Route{
		"Auth",
		"POST",
		"/token-auth",
		Login,
	},
	Route{
		"EchoHome",
		"GET",
		"/echoHome",
		EchoHome,
	},

}