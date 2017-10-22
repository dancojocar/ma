package com.example.ma.sm.net;

import com.example.ma.sm.model.Portfolio;

public interface ServerNotifier {
  void push(Portfolio p);
}
