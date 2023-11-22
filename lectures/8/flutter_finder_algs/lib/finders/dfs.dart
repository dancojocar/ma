import 'dart:async';

import 'package:flutter_finder_algs/finders/base.dart';
import 'package:flutter_finder_algs/finders/node.dart';

class DFS extends BaseFinder {
  DFS() : super('DFS Path Finder');

  @override
  Stream<List<List<Node>>> call(
    List<List<Node>> graph,
    Node start,
    Node end, [
    Duration delay = const Duration(milliseconds: 10),
  ]) async* {
    final List<Node> stack = <Node>[start];

    while (stack.isNotEmpty) {
      final Node node = stack.removeLast();

      if (node == end) {
        return;
      }

      node.visited = true;

      final List<Node> neighbors = getNeighbors(graph, node);

      for (final Node neighbor in neighbors) {
        if (neighbor.visited) {
          continue;
        }

        neighbor
          ..visited = true
          ..previous = node;

        stack.add(neighbor);
      }

      await Future<void>.delayed(delay);
      yield graph;
    }
  }
}
