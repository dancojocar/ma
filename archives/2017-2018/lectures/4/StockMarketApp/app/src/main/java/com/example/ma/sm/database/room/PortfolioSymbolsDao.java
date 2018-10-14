package com.example.ma.sm.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

@Dao
public interface PortfolioSymbolsDao {
  @Query("select * from PortfolioEntity")
  @Transaction
  List<PortfolioAndSymbols> portfoliosAndTheirSymbols();
  @Insert
  void addSymbol(SymbolEntity symbol);
}
