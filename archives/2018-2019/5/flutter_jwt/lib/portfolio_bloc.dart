import 'dart:async';
import 'dart:collection';

import 'package:flutter_jwt/portfolio.dart';
import 'package:flutter_jwt/service.dart';
import 'package:rxdart/rxdart.dart';

class PortfolioBloc {
  final PortfolioService service;

  PortfolioBloc(this.service);

  final _portfoliosSubject = BehaviorSubject<List<Portfolio>>();

  Stream<List<Portfolio>> get allPortfolios =>
      _portfoliosSubject.stream;

  getPortfolios(var token) async {
    List<Portfolio> portfolio = await service.fetchPost(token);
    _portfoliosSubject.sink.add(portfolio);
  }

  dispose() {
    _portfoliosSubject.close();
  }
}
