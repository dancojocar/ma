package com.example.ma.sm.model;

import java.util.Date;
import java.util.Random;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Portfolio extends RealmObject {
  @PrimaryKey
  private long id;
  private String name;
  private Date lastModified;
  private RealmList<Symbol> symbols;

  public Portfolio() {
  }

  public Portfolio(String name) {
    this.name = name;
    this.lastModified = new Date(System.currentTimeMillis());
    symbols = new RealmList<>();
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

  public RealmList<Symbol> getSymbols() {
    return symbols;
  }

  public void setSymbols(RealmList<Symbol> symbols) {
    this.symbols = symbols;
  }
}

