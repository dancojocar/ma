package com.example.ma.sm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Portfolio implements Serializable {
  private long id;
  private String name;
  private Date lastModified;
  private List<Symbol> symbols;

  public Portfolio() {
  }

  public Portfolio(String name) {
    this.name = name;
    this.lastModified = new Date(System.currentTimeMillis());
    symbols = new ArrayList<>();
    Random r = new Random();
    for (int i = 0; i < r.nextInt(3) + 2; i++) {
      Symbol symbol = new Symbol("S" + i + " " + System.currentTimeMillis());
      symbol.setId(i);
      symbol.setAcquisitionPrice(r.nextDouble() * 10);
      symbol.setAcquisitionDate(new Date(System.currentTimeMillis()));
      symbol.setQuantity(r.nextInt(40) + 10);
      symbols.add(symbol);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public List<Symbol> getSymbols() {
    return symbols;
  }

  public void setSymbols(List<Symbol> symbols) {
    this.symbols = symbols;
  }
}

