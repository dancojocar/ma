package com.example.ma.sm.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PortfolioDao {
  @Query("select * from PortfolioEntity")
  List<PortfolioEntity> getEntries();

  @Query("select * from PortfolioEntity " +
      " where title like :titleArg")
  List<PortfolioEntity> getEntries(String titleArg);

  @Insert
  void insert(PortfolioEntity portfolio);

  @Delete
  void delete(PortfolioEntity portfolio);

  @Update
  void update(PortfolioEntity portfolio);

  @Query("DELETE FROM PortfolioEntity")
  void nukeAll();
}
