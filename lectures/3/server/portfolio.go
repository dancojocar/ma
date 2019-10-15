package main

type Portfolio struct {
	Id           int       `json:"id,omitempty"`
	Name         string    `json:"name,omitempty"`
	LastModified int64 `json:"lastModified,omitempty"`
	Symbols      []Symbol `json:"symbols,omitempty"`
}

type Portfolios []Portfolio