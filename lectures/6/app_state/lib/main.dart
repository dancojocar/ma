import 'package:flutter/material.dart';
import 'package:app_state/product.dart';
import 'package:scoped_model/scoped_model.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Product layout demo home page'),
    );
  }
}

class MyHomePage extends StatelessWidget {
  MyHomePage({super.key, required this.title});

  final String title;
  final items = Product.getProducts();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: const Text("Product Navigation")),
        body: ListView.builder(
          itemCount: items.length,
          itemBuilder: (context, index) {
            return ProductBox(item: items[index]);
          },
        ));
  }
}

class RatingBox extends StatefulWidget {
  const RatingBox({super.key, required this.item});
  final Product item;

  @override
  RatingBoxState createState() => RatingBoxState();
}

class RatingBoxState extends State<RatingBox> {
  int _rating = 0;
  void _setRatingAsOne() {
    setState(() {
      _rating = 1;
    });
  }

  void _setRatingAsTwo() {
    setState(() {
      _rating = 2;
    });
  }

  void _setRatingAsThree() {
    setState(() {
      _rating = 3;
    });
  }

  @override
  Widget build(BuildContext context) {
    double size = 20;
    print(_rating);
    return Row(
      mainAxisAlignment: MainAxisAlignment.end,
      crossAxisAlignment: CrossAxisAlignment.end,
      mainAxisSize: MainAxisSize.max,
      children: <Widget>[
        Container(
          padding: const EdgeInsets.all(0),
          child: IconButton(
            icon: (_rating >= 1
                ? Icon(
                    Icons.star,
                    size: size,
                  )
                : Icon(
                    Icons.star_border,
                    size: size,
                  )),
            color: Colors.red[500],
            onPressed: _setRatingAsOne,
            iconSize: size,
          ),
        ),
        Container(
          padding: const EdgeInsets.all(0),
          child: IconButton(
            icon: (_rating >= 2
                ? Icon(
                    Icons.star,
                    size: size,
                  )
                : Icon(
                    Icons.star_border,
                    size: size,
                  )),
            color: Colors.red[500],
            onPressed: _setRatingAsTwo,
            iconSize: size,
          ),
        ),
        Container(
          padding: const EdgeInsets.all(0),
          child: IconButton(
            icon: (_rating >= 3
                ? Icon(
                    Icons.star,
                    size: size,
                  )
                : Icon(
                    Icons.star_border,
                    size: size,
                  )),
            color: Colors.red[500],
            onPressed: _setRatingAsThree,
            iconSize: size,
          ),
        ),
      ],
    );
  }
}

class ProductBox extends StatelessWidget {
  const ProductBox({super.key, required this.item});
  final Product item;

  @override
  Widget build(BuildContext context) {
    return Container(
        padding: const EdgeInsets.all(2),
        height: 140,
        child: Card(
          child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: <Widget>[
                Image.asset("assets/${item.image}"),
                Expanded(
                    child: Container(
                        padding: const EdgeInsets.all(5),
                        child: ScopedModel<Product>(
                            model: item,
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                              children: <Widget>[
                                Text(item.name,
                                    style: const TextStyle(
                                        fontWeight: FontWeight.bold)),
                                Text(item.description),
                                Text("Price: ${item.price}"),
                                ScopedModelDescendant<Product>(
                                    builder: (context, child, item) {
                                  return RatingBox(item: item);
                                })
                              ],
                            ))))
              ]),
        ));
  }
}
