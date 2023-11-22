import 'dart:math';

import 'package:flutter_finder_algs/finders/astar.dart';
import 'package:flutter_finder_algs/finders/node.dart';

class AStarEuclid extends AStar {
  AStarEuclid() : super(label: 'A* Euclid Path Finder');

  //Euclidean distance
  @override
  double calculateHeuristic(Node node, Node end) {
    final double dx = (node.position.dx - end.position.dx).abs();
    final double dy = (node.position.dy - end.position.dy).abs();
    return sqrt(dx * dx + dy * dy);
  }
}
