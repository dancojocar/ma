import 'dart:async';
import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_mvvm/model/media.dart';
import 'package:flutter_mvvm/view_model/media_view_model.dart';
import 'package:provider/provider.dart';

enum PlayerState { stopped, playing, paused }

class PlayerWidget extends StatefulWidget {
  final Function function;

  const PlayerWidget({super.key, required this.function});

  @override
  State<StatefulWidget> createState() => _PlayerWidgetState();
}

class _PlayerWidgetState extends State<PlayerWidget> {
  String? _prevSongName;

  late AudioPlayer _audioPlayer;
  Duration? _duration;
  Duration? _position;

  PlayerState _playerState = PlayerState.stopped;
  StreamSubscription? _durationSubscription;
  StreamSubscription? _positionSubscription;
  StreamSubscription? _playerCompleteSubscription;
  StreamSubscription? _playerStateSubscription;

  get _isPlaying => _playerState == PlayerState.playing;

  @override
  void initState() {
    super.initState();
    _initAudioPlayer();
  }

  @override
  void dispose() {
    _audioPlayer.dispose();
    _durationSubscription?.cancel();
    _positionSubscription?.cancel();
    _playerCompleteSubscription?.cancel();
    _playerStateSubscription?.cancel();
    super.dispose();
  }

  void _playCurrentMedia(Media? media) {
    if (media != null && _prevSongName != media.trackName) {
      _prevSongName = media.trackName;
      _position = null;
      _stop();
      _play(media);
    }
  }

  @override
  Widget build(BuildContext context) {
    Media? media = Provider.of<MediaViewModel>(context).media;
    _playCurrentMedia(media);
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            IconButton(
              onPressed: () => null,
              icon: Icon(
                Icons.fast_rewind,
                size: 25.0,
                color: Theme.of(context).brightness == Brightness.dark
                    ? Theme.of(context).colorScheme.primary
                    : const Color(0xFF787878),
              ),
            ),
            ClipOval(
              child: Container(
                color: Theme.of(context).colorScheme.primary.withAlpha(30),
                width: 50.0,
                height: 50.0,
                child: IconButton(
                  onPressed: () {
                    if (_isPlaying) {
                      widget.function();
                      _pause();
                    } else {
                      if (media != null) {
                        widget.function();
                        _play(media);
                      }
                    }
                  },
                  icon: Icon(
                    _isPlaying ? Icons.pause : Icons.play_arrow,
                    size: 30.0,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
              ),
            ),
            IconButton(
              onPressed: () => null,
              icon: Icon(
                Icons.fast_forward,
                size: 25.0,
                color: Theme.of(context).brightness == Brightness.dark
                    ? Theme.of(context).colorScheme.primary
                    : Color(0xFF787878),
              ),
            ),
          ],
        ),
        Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: EdgeInsets.only(left: 12.0, right: 12.0),
              child: Stack(
                children: [
                  Slider(
                    onChanged: (v) {
                      final position = v * _duration!.inMilliseconds;
                      _audioPlayer
                          .seek(Duration(milliseconds: position.round()));
                    },
                    value: (_position != null &&
                            _duration != null &&
                            _position!.inMilliseconds > 0 &&
                            _position!.inMilliseconds <
                                _duration!.inMilliseconds)
                        ? _position!.inMilliseconds / _duration!.inMilliseconds
                        : 0.0,
                  ),
                ],
              ),
            ),
          ],
        ),
      ],
    );
  }

  void _initAudioPlayer() {
    _audioPlayer = AudioPlayer();

    _durationSubscription = _audioPlayer.onDurationChanged.listen((duration) {
      setState(() => _duration = duration);
    });

    _positionSubscription = _audioPlayer.onPositionChanged.listen((position) {
      setState(() => _position = position);
    });

    _playerCompleteSubscription = _audioPlayer.onPlayerComplete.listen((event) {
      _onComplete();
      setState(() => _position = _duration);
    });

    _playerStateSubscription =
        _audioPlayer.onPlayerStateChanged.listen((state) {
      setState(() {
        _playerState = state == PlayerState.playing
            ? PlayerState.playing
            : state == PlayerState.paused
                ? PlayerState.paused
                : PlayerState.stopped;
      });
    });
  }

  void _play(Media media) async {
    await _audioPlayer.play(UrlSource(media.previewUrl!));
    setState(() => _playerState = PlayerState.playing);
    _audioPlayer.setPlaybackRate(1.0);
  }

  void _pause() async {
    await _audioPlayer.pause();
    setState(() => _playerState = PlayerState.paused);
  }

  void _stop() async {
    await _audioPlayer.stop();
    setState(() {
      _playerState = PlayerState.stopped;
      _position = Duration.zero;
    });
  }

  void _onComplete() {
    setState(() => _playerState = PlayerState.stopped);
  }
}
