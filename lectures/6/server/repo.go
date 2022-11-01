package main

import (
	"fmt"
	"time"
)

var currentId int

var portfolios Portfolios

// Give us some seed data
func init() {
	RepoCreatePortfolio(Portfolio{Name: "Simulation 100", Symbols: []Symbol{{Name: "S100-1", Quantity: 200, AcquisitionPrice: 10.1}, {Name: "S100-2", Quantity: 50, AcquisitionPrice: 31.2}}})
	RepoCreatePortfolio(Portfolio{Name: "Simulation 200", Symbols: []Symbol{{Name: "S200-1", Quantity: 100, AcquisitionPrice: 50.2},}})
}

func RepoFindPortfolio(id int) Portfolio {
	for _, p := range portfolios {
		if p.Id == id {
			return p
		}
	}
	// return empty Portfolio if not found
	return Portfolio{}
}

func RepoCreatePortfolio(p Portfolio) Portfolio {
	currentId += 1
	p.Id = currentId
	p.LastModified = time.Now().Unix()
	portfolios = append(portfolios, p)
	return p
}

func RepoDestroyPortfolio(id int) error {
	for i, t := range portfolios {
		if t.Id == id {
			portfolios = append(portfolios[:i], portfolios[i+1:]...)
			return nil
		}
	}
	return fmt.Errorf("could not find Portfolio with id of %d to delete", id)
}
