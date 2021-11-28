import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';

void main() async{
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

 @override
 Widget build(BuildContext context) {
   return const MaterialApp(
     title: 'Baby Names',
     home: MyHomePage(),
   );
 }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key}) : super(key: key);

 @override
 _MyHomePageState createState() {
   return _MyHomePageState();
 }
}

class _MyHomePageState extends State<MyHomePage> {
 @override
 Widget build(BuildContext context) {
   return Scaffold(
     appBar: AppBar(title: const Text('Baby Name Votes')),
     body: _buildBody(context),
   );
 }

 Widget _buildBody(BuildContext context) {
   return StreamBuilder<QuerySnapshot>(
     stream: FirebaseFirestore.instance.collection('baby').snapshots(),
     builder: (context, snapshot) {
       if (!snapshot.hasData) return const LinearProgressIndicator();

       return _buildList(context, snapshot.data!.docs);
     },
   );
 }

 Widget _buildList(BuildContext context, List<DocumentSnapshot> snapshot) {
   return ListView(
     padding: const EdgeInsets.only(top: 20.0),
     children: snapshot.map((DocumentSnapshot document) {
          Map<String, dynamic> data = document.data()! as Map<String, dynamic>;
            return _buildListItem(context, data, document.reference);
          }).toList(),
   );
 }

 Widget _buildListItem(BuildContext context, Map<String, dynamic> record, DocumentReference<Object?> reference) {

   return Padding(
     key: ValueKey(record['name']),
     padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
     child: Container(
       decoration: BoxDecoration(
         border: Border.all(color: Colors.grey),
         borderRadius: BorderRadius.circular(5.0),
       ),
       child: ListTile(
         title: Text(record['name']),
         trailing: Text(record['votes'].toString()),
         onTap: () => FirebaseFirestore.instance.runTransaction((transaction) async {
               final freshSnapshot = await transaction.get(reference);
                final fresh = freshSnapshot.data()! as Map<String, dynamic>;
               transaction
                   .update(reference, {'votes': fresh['votes'] + 1});
             }),
       ),
     ),
   );
 }
}
