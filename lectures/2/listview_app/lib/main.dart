// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/material.dart';

class ExpansionTileSample extends StatelessWidget {
  const ExpansionTileSample({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Students'),
        ),
        body: ListView.builder(
          itemBuilder: (BuildContext context, int index) =>
              EntryItem(_data[index]),
          itemCount: _data.length,
        ),
      ),
    );
  }
}

// One entry in the multilevel list displayed by this app.
class Entry {
  Entry(this.title, [this.children = const <Entry>[]]);

  final String title;
  final List<Entry> children;
}

// The entire multilevel list displayed by this app.
final List<Entry> _data = <Entry>[
  Entry(
    'First Year',
    <Entry>[
      Entry(
        'Englist groups',
        <Entry>[
          Entry('911', <Entry>[
            Entry('911/1',<Entry>[
              Entry('John Smith'),
              Entry('John Doe'),
            ]),
            Entry('911/2'),
          ]),
          Entry('912'),
          Entry('913'),
          Entry('914'),
          Entry('915'),
          Entry('916'),
          Entry('917'),
        ],
      ),
      Entry(
        'Romanian groups',
        <Entry>[
          Entry('211'),
          Entry('212'),
          Entry('213'),
          Entry('214'),
          Entry('215'),
          Entry('216'),
          Entry('217'),
        ],
      ),
    ],
  ),
  Entry(
    'Second Year',
    <Entry>[
      Entry(
        'Englist groups',
        <Entry>[
          Entry('921'),
          Entry('922'),
          Entry('923'),
          Entry('924'),
          Entry('925'),
          Entry('926'),
          Entry('927'),
        ],
      ),
      Entry(
        'Romanian groups',
        <Entry>[
          Entry('221'),
          Entry('222'),
          Entry('223'),
          Entry('224'),
          Entry('225'),
          Entry('226'),
          Entry('227'),
        ],
      ),
    ],
  ),
  Entry(
    'Third Year',
    <Entry>[
      Entry(
        'Englist groups',
        <Entry>[
          Entry('931'),
          Entry('932'),
          Entry('933'),
          Entry('934'),
          Entry('935'),
          Entry('936'),
          Entry('937'),
        ],
      ),
      Entry(
        'Romanian groups',
        <Entry>[
          Entry('231'),
          Entry('232'),
          Entry('233'),
          Entry('234'),
          Entry('235'),
          Entry('236'),
          Entry('237'),
        ],
      ),
    ],
  ),
];

// Displays one Entry. If the entry has children then it's displayed
// with an ExpansionTile.
class EntryItem extends StatelessWidget {
  const EntryItem(this.entry, {Key? key}): super(key: key);

  final Entry entry;

  Widget _buildTiles(Entry root) {
    if (root.children.isEmpty) return ListTile(title: Text(root.title));
    return ExpansionTile(
      key: PageStorageKey<Entry>(root),
      title: Text(root.title),
      children: root.children.map(_buildTiles).toList(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return _buildTiles(entry);
  }
}

void main() {
  runApp(const ExpansionTileSample());
}
