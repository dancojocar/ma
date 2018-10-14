package com.example.ma.sm.database.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {PortfolioEntity.class, SymbolEntity.class}, version = 2)
public abstract class PortfolioDatabase
    extends RoomDatabase {
  public abstract PortfolioDao portfolioDao();

  public abstract PortfolioSymbolsDao portfolioSymbolsDao();

  static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase db) {
      // Since we didn't alter the table, there's nothing else to do here.
    }
  };
}
