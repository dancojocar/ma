package com.example.ma.sm.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Symbol extends RealmObject {
  @PrimaryKey
  private long id;
  private String name;
  private Date acquisitionDate;
  private long quantity;
  private double acquisitionPrice;
  private long portfolioId;

  public Symbol() {

  }

  public Symbol(String name) {
    this.name = name;
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

  public Date getAcquisitionDate() {
    return acquisitionDate;
  }

  public void setAcquisitionDate(Date acquisitionDate) {
    this.acquisitionDate = acquisitionDate;
  }

  public long getQuantity() {
    return quantity;
  }

  public void setQuantity(long quantity) {
    this.quantity = quantity;
  }

  public double getAcquisitionPrice() {
    return acquisitionPrice;
  }

  public void setAcquisitionPrice(double acquisitionPrice) {
    this.acquisitionPrice = acquisitionPrice;
  }

  public long getPortfolioId() {
    return portfolioId;
  }

  public void setPortfolioId(long portfolioId) {
    this.portfolioId = portfolioId;
  }
}
