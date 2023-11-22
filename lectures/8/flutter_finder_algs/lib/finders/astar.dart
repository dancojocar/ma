import 'dart:async';

import 'package:flutter_finder_algs/finders/base.dart';
import 'package:flutter_finder_algs/finders/node.dart';

class AStar extends BaseFinder {
  AStar({String label = 'A* Path Finder'}) : super(label);

  @override
  Stream<List<List<Node>>> call(
    List<List<Node>> graph,
    Node start,
    Node end, [
    Duration delay = const Duration(milliseconds: 10),
  ]) async* {
    final List<Node> queue = <Node>[start];

    while (queue.isNotEmpty) {
      final Node node = _getBest(queue);

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
          ..cost = node.cost + 1
          ..heuristic = calculateHeuristic(neighbor, end).toDouble()
          ..f = neighbor.cost + neighbor.heuristic
          ..visited = true
          ..previous = node;

        queue.add(neighbor);
      }

      await Future<void>.delayed(delay);
      yield graph;
    }
  }

  //Manhattan distance
  double calculateHeuristic(Node node, Node end) =>
      (node.position.dx - end.position.dx).abs() +
      (node.position.dy - end.position.dy).abs();

  Node _getBest(final List<Node> queue) {
    Node best = queue.first;

    for (final Node node in queue) {
      if (node.f < best.f) {
        best = node;
      }
    }

    queue.remove(best);

    return best;
  }
}
