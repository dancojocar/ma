package com.example.ma.sm.database.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class RoomSample {
  public static final String TAG = RoomSample.class.getSimpleName();

  public void demo(final Context context) {
    Thread bg = new Thread(new Runnable() {
      @Override
      public void run() {
        PortfolioDatabase db = Room.databaseBuilder(context,
            PortfolioDatabase.class, "portfolios.db")
//            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build();

        PortfolioDao portfolioDao = db.portfolioDao();
        PortfolioEntity portfolio = new PortfolioEntity();

        portfolio.setTitle("test");
        portfolio.setDescription("test description");
        portfolioDao.insert(portfolio);

        List<PortfolioEntity> portfolios = portfolioDao.getEntries();
        portfolio=portfolios.get(0);
        for (PortfolioEntity p :
            portfolios) {
          Log.d(TAG, "portfolio = " + p);
        }
//        portfolioDao.delete(portfolio);
//        portfolioDao.nukeAll();

        PortfolioSymbolsDao psDao = db.portfolioSymbolsDao();
        listPortfoliosWithTheirSymbols(psDao);
        SymbolEntity symbol = new SymbolEntity();
        symbol.setName("test Symbol");
        symbol.setValue(10.0);
        symbol.setPortfolio(portfolio.getId());
        psDao.addSymbol(symbol);
        listPortfoliosWithTheirSymbols(psDao);
      }
    });
    bg.start();
  }

  private void listPortfoliosWithTheirSymbols(PortfolioSymbolsDao psDao) {
    List<PortfolioAndSymbols> portfoliosAndSymbols = psDao.portfoliosAndTheirSymbols();
    for (PortfolioAndSymbols ps : portfoliosAndSymbols) {
      Log.d(TAG, "ps: " + ps);
    }
  }
}
