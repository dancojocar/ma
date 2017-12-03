package com.example.ma.sm.net;

import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.task.listeners.OnErrorListener;

public interface ServerNotifier {
  void push(Portfolio p, OnErrorListener onErrorListener);
}
