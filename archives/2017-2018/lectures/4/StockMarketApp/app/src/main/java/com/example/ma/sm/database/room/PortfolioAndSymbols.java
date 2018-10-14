package com.example.ma.sm.database.room;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class PortfolioAndSymbols {
  @Embedded
  private PortfolioEntity portfolio;
  @Relation(parentColumn = "id",
      entityColumn = "portfolio_id")
  private List<SymbolEntity> symbols;

  public PortfolioEntity getPortfolio() {
    return portfolio;
  }

  public void setPortfolio(PortfolioEntity portfolio) {
    this.portfolio = portfolio;
  }

  public List<SymbolEntity> getSymbols() {
    return symbols;
  }

  public void setSymbols(List<SymbolEntity> symbols) {
    this.symbols = symbols;
  }

  @Override
  public String toString() {
    return "PS{"+portfolio +
        " " + symbols +
        '}';
  }
}
