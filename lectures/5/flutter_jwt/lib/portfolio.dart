
class Portfolio {
  final int id;
  final String name;
  final int lastModified;

  Portfolio({this.id, this.name, this.lastModified});

  factory Portfolio.fromJson(Map<String, dynamic> json) {
    return Portfolio(
      id: json['id'],
      name: json['name'],
      lastModified: json['lastModified'],
    );
  }

  static List<Portfolio> fromJsonList(List<dynamic> json) {
    List<Portfolio> portfolios = List();
    json.forEach((item) => portfolios.add(Portfolio.fromJson(item)));
    return portfolios;
  }

  @override
  String toString() {
    return 'Portfolio{id: $id, name: $name}';
  }

}