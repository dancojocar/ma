package com.example.ma.sm.database.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
    indices = {@Index("portfolio_id")},
    foreignKeys =
    @ForeignKey(entity = PortfolioEntity.class,
        parentColumns = "id",
        childColumns = "portfolio_id",
        onDelete = CASCADE
    ))
public class SymbolEntity {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String name;
  private double value;
  @ColumnInfo(name = "portfolio_id")
  private int portfolio;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public int getPortfolio() {
    return portfolio;
  }

  public void setPortfolio(int portfolio) {
    this.portfolio = portfolio;
  }

  @Override
  public String toString() {
    return "SE{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", value=" + value +
        ", portfolio=" + portfolio +
        '}';
  }
}
