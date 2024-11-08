import 'dart:async';

import 'bloc_base.dart';
import '../repos/repository.dart';
import 'package:rxdart/rxdart.dart';
import '../models/photos.dart';
import '../models/state.dart';

class HomeScreenBloc extends BlocBase {
  static final Repository _repository = Repository();
  late PublishSubject<String> _query;

  init() {
    _query = PublishSubject<String>();
  }

  Stream<Photos> get photosList => _query.stream
      .debounceTime(const Duration(milliseconds: 300))
      .where((String value) => value.isNotEmpty)
      .distinct()
      .transform(streamTransformer);

  final streamTransformer = StreamTransformer<String, Photos>.fromHandlers(
      handleData: (query, sink) async {
    // print("transformer: $query");
    State state = await _repository.imageData(query);
    if (state is SuccessState) {
      // print("received $state.value");
      sink.add(state.value);
    } else {
      ErrorState err = state as ErrorState;
      print("received ${err.msg}");
      sink.addError(err.msg);
    }
  });

  Function(String) get changeQuery => _query.sink.add;

  @override
  void dispose() {
    _query.close();
  }

  String getDescription(Result result) {
    return (result.description.isEmpty)
        ? result.altDescription
        : result.description;
  }
}

final bloc = HomeScreenBloc();
