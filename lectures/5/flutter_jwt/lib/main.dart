import 'package:flutter/material.dart';
import 'package:flutter_jwt/portfolio.dart';
import 'package:flutter_jwt/portfolio_bloc.dart';
import 'package:flutter_jwt/service.dart';

var token = '';

void main() {
  final PortfolioService portfolioService = PortfolioService();
  final PortfolioBloc portfolioBloc = PortfolioBloc(portfolioService);
  runApp(MyApp(portfolioBloc, portfolioService));
}

class MyApp extends StatelessWidget {
  final PortfolioBloc portfolioBloc;
  final PortfolioService portfolioService;

  const MyApp(this.portfolioBloc, this.portfolioService, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fetch Data Example',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Fetch Data Example'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              ElevatedButton(
                child: const Text('Auth'),
                onPressed: () async {
                  var token = await portfolioService.auth();
                  portfolioBloc.getPortfolios(token);
                },
              ),
              PortfolioList(
                portfolioBloc: portfolioBloc,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class PortfolioList extends StatelessWidget {
  final PortfolioBloc portfolioBloc;

  const PortfolioList(
      {Key key = const Key("any_key"), required this.portfolioBloc})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Flexible(
      child: StreamBuilder<List<Portfolio>>(
          stream: portfolioBloc.allPortfolios,
          initialData: const <Portfolio>[],
          builder: (context, snapshot) {
            switch (snapshot.connectionState) {
              case ConnectionState.none:
              case ConnectionState.waiting:
                return const Center(child: CircularProgressIndicator());
              default:
                return ListView.builder(
                  itemCount: snapshot.data!.length,
                  itemBuilder: (context, index) {
                    return Text(
                      '${snapshot.data![index].id} - ${snapshot.data![index].name}',
                      style: Theme.of(context).textTheme.headline4,
                    );
                  },
                );
            }
          }),
    );
  }
}
