package main

type Symbol struct {
	Id               int       `json:"id,omitempty"`
	Name             string    `json:"name,omitempty"`
	AcquisitionDate  int64 `json:"acquisitionDate,omitempty"`
	Quantity         int64 `json:"quantity,omitempty"`
	AcquisitionPrice float64 `json:"acquisitionPrice,omitempty"`
}

type Symbols []Symbol