import 'package:flutter/material.dart';
import 'package:flutter_mvvm/model/apis/api_response.dart';
import 'package:flutter_mvvm/model/media.dart';
import 'package:flutter_mvvm/view/widgets/player_list_widget.dart';
import 'package:flutter_mvvm/view/widgets/player_widget.dart';
import 'package:flutter_mvvm/view_model/media_view_model.dart';

import 'package:provider/provider.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {


  Widget getMediaWidget(BuildContext context, ApiResponse apiResponse) {
    List<Media>? mediaList = apiResponse.data as List<Media>?;
    switch (apiResponse.status) {
      case Status.LOADING:
        return const Center(child: CircularProgressIndicator());
      case Status.COMPLETED:
        return Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Expanded(
              flex: 8,
              child: PlayerListWidget(mediaList!, (Media media) {
                Provider.of<MediaViewModel>(context, listen: false)
                .setSelectedMedia(media);
              }),
            ),
            Expanded(
              flex: 2,
              child: Align(
                alignment: Alignment.bottomCenter,
                child: PlayerWidget(
                  function: () {
                    setState(() {});
                  },
                ),
              ),
            ),
          ],
        );
      case Status.ERROR:
        return const Center(
          child: Text('Please try again latter!!!'),
        );
      case Status.INITIAL:
      default:
        return const Center(
          child: Text('Search the song by Artist'),
        );
    }
  }

  @override
  Widget build(BuildContext context) {
    final _inputController = TextEditingController();
    ApiResponse apiResponse = Provider.of<MediaViewModel>(context).response;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Media Player'),
      ),
      body: Column(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 10.0),
            child: Row(
              children: <Widget>[
                Expanded(
                  child: Container(
                    margin: const EdgeInsets.symmetric(horizontal: 20.0),
                    decoration: BoxDecoration(
                      color: Theme.of(context).accentColor.withAlpha(50),
                      borderRadius: BorderRadius.circular(30.0),
                    ),
                    child: TextField(
                        style: const TextStyle(
                          fontSize: 15.0,
                          color: Colors.grey,
                        ),
                        controller: _inputController,
                        onChanged: (value) {},
                        onSubmitted: (value) {
                          if (value.isNotEmpty) {
                            Provider.of<MediaViewModel>(context, listen: false)
                                .setSelectedMedia(null);
                            Provider.of<MediaViewModel>(context, listen: false)
                                .fetchMediaData(value);
                          }
                        },
                        decoration: const InputDecoration(
                          border: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          prefixIcon: Icon(
                            Icons.search,
                            color: Colors.grey,
                          ),
                          hintText: 'Enter Artist Name',
                        )),
                  ),
                ),
              ],
            ),
          ),
          Expanded(child: getMediaWidget(context, apiResponse)),
        ],
      ),
    );
  }
}
