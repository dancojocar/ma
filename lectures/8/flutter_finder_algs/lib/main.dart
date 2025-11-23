import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_finder_algs/finders/astar_euclid.dart';
import 'package:flutter_finder_algs/path_finder_painter.dart';
import 'package:flutter_finder_algs/finders/astar.dart';
import 'package:flutter_finder_algs/finders/base.dart';
import 'package:flutter_finder_algs/finders/bfs.dart';
import 'package:flutter_finder_algs/finders/dfs.dart';
import 'package:flutter_finder_algs/finders/node.dart';

const int size = 40;
const int walls = 400;

void main() => runApp(const MainApp());

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    final Offset startPosition = Offset(
      Random().nextInt(size).toDouble(),
      Random().nextInt(size).toDouble(),
    );
    final Offset endPosition = Offset(
      Random().nextInt(size).toDouble(),
      Random().nextInt(size).toDouble(),
    );

    final List<List<Node>> nodes = _generateNodes(
      size,
      walls,
      startPosition,
      endPosition,
    );

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        backgroundColor: Colors.blueGrey.shade900,
        body: Center(
          child: GridView.count(
            crossAxisCount: 1,
            children: <Widget>[
              _drawMap(
                Node.cloneList(nodes),
                BFS(),
                startPosition,
                endPosition,
              ),
              _drawMap(
                Node.cloneList(nodes),
                AStar(),
                startPosition,
                endPosition,
              ),
              _drawMap(
                Node.cloneList(nodes),
                AStarEuclid(),
                startPosition,
                endPosition,
              ),
              _drawMap(
                Node.cloneList(nodes),
                DFS(),
                startPosition,
                endPosition,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _drawMap(
    List<List<Node>> nodes,
    BaseFinder pathFinder,
    Offset startPosition,
    Offset endPosition,
  ) {
    final int startX = startPosition.dx.floor();
    final int startY = startPosition.dy.floor();

    final int endX = endPosition.dx.floor();
    final int endY = endPosition.dy.floor();

    final Node start = nodes[startX][startY];
    final Node end = nodes[endX][endY];

    // Create the streams once and make them broadcast
    final Stream<List<List<Node>>> finderStream =
        pathFinder(nodes, start, end).asBroadcastStream();
    final Stream<List<Node>> pathStream =
        pathFinder.getPath(end).asBroadcastStream();

    return Column(
      children: <Widget>[
        const SizedBox(height: 8),
        Text(
          pathFinder.name,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 8),
        StreamBuilder<List<List<Node>>>(
          stream: finderStream,
          initialData: nodes,
          builder: (
            BuildContext context,
            AsyncSnapshot<List<List<Node>>> finderSnapshot,
          ) =>
              StreamBuilder<List<Node>>(
            stream: pathStream,
            initialData: const <Node>[],
            builder: (
              BuildContext context,
              AsyncSnapshot<List<Node>> pathSnapshot,
            ) =>
                CustomPaint(
              size: const Size(300, 300),
              painter: PathFinderPainter(
                finderSnapshot.data!,
                pathSnapshot.data!,
                start,
                end,
              ),
            ),
          ),
        ),
      ],
    );
  }

  List<List<Node>> _generateNodes(
    int size,
    int walls,
    Offset start,
    Offset end,
  ) {
    final List<List<Node>> nodes = <List<Node>>[];

    for (int i = 0; i < size; i++) {
      final List<Node> row = <Node>[];

      for (int j = 0; j < size; j++) {
        row.add(Node(Offset(j.toDouble(), i.toDouble())));
      }

      nodes.add(row);
    }

    final int startX = start.dx.floor();
    final int startY = start.dy.floor();

    final int endX = end.dx.floor();
    final int endY = end.dy.floor();

    for (int i = 0; i < walls; i++) {
      int row;
      int column;
      do {
        row = Random().nextInt(size);
        column = Random().nextInt(size);
      } while ((row == startY && column == startX) ||
          (row == endY && column == endX));

      nodes[row][column].isWall = true;
    }

    return nodes;
  }
}
