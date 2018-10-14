package com.example.ma.sm.net;

import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.task.CancellableCall;
import com.example.ma.sm.task.listeners.OnErrorListener;
import com.example.ma.sm.task.listeners.OnSuccessListener;

import java.util.List;

public interface ClientConnection extends ServerNotifier {
  CancellableCall getPortfolios(final OnSuccessListener<List<Portfolio>> osl,
                                final OnErrorListener oel);
}
