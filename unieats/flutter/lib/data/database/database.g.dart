// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database.dart';

// ignore_for_file: type=lint
class $SpotsTable extends Spots with TableInfo<$SpotsTable, SpotRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SpotsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
    'name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _categoryMeta = const VerificationMeta(
    'category',
  );
  @override
  late final GeneratedColumn<String> category = GeneratedColumn<String>(
    'category',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _ratingMeta = const VerificationMeta('rating');
  @override
  late final GeneratedColumn<double> rating = GeneratedColumn<double>(
    'rating',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _priceLevelMeta = const VerificationMeta(
    'priceLevel',
  );
  @override
  late final GeneratedColumn<int> priceLevel = GeneratedColumn<int>(
    'price_level',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _latMeta = const VerificationMeta('lat');
  @override
  late final GeneratedColumn<double> lat = GeneratedColumn<double>(
    'lat',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _lngMeta = const VerificationMeta('lng');
  @override
  late final GeneratedColumn<double> lng = GeneratedColumn<double>(
    'lng',
    aliasedName,
    false,
    type: DriftSqlType.double,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _openNowMeta = const VerificationMeta(
    'openNow',
  );
  @override
  late final GeneratedColumn<bool> openNow = GeneratedColumn<bool>(
    'open_now',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("open_now" IN (0, 1))',
    ),
  );
  static const VerificationMeta _photoUrlMeta = const VerificationMeta(
    'photoUrl',
  );
  @override
  late final GeneratedColumn<String> photoUrl = GeneratedColumn<String>(
    'photo_url',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _descriptionMeta = const VerificationMeta(
    'description',
  );
  @override
  late final GeneratedColumn<String> description = GeneratedColumn<String>(
    'description',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _updatedAtMeta = const VerificationMeta(
    'updatedAt',
  );
  @override
  late final GeneratedColumn<int> updatedAt = GeneratedColumn<int>(
    'updated_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _pendingSyncMeta = const VerificationMeta(
    'pendingSync',
  );
  @override
  late final GeneratedColumn<bool> pendingSync = GeneratedColumn<bool>(
    'pending_sync',
    aliasedName,
    false,
    type: DriftSqlType.bool,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'CHECK ("pending_sync" IN (0, 1))',
    ),
    defaultValue: const Constant(false),
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    name,
    category,
    rating,
    priceLevel,
    lat,
    lng,
    openNow,
    photoUrl,
    description,
    updatedAt,
    pendingSync,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'spots';
  @override
  VerificationContext validateIntegrity(
    Insertable<SpotRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
        _nameMeta,
        name.isAcceptableOrUnknown(data['name']!, _nameMeta),
      );
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('category')) {
      context.handle(
        _categoryMeta,
        category.isAcceptableOrUnknown(data['category']!, _categoryMeta),
      );
    } else if (isInserting) {
      context.missing(_categoryMeta);
    }
    if (data.containsKey('rating')) {
      context.handle(
        _ratingMeta,
        rating.isAcceptableOrUnknown(data['rating']!, _ratingMeta),
      );
    } else if (isInserting) {
      context.missing(_ratingMeta);
    }
    if (data.containsKey('price_level')) {
      context.handle(
        _priceLevelMeta,
        priceLevel.isAcceptableOrUnknown(data['price_level']!, _priceLevelMeta),
      );
    } else if (isInserting) {
      context.missing(_priceLevelMeta);
    }
    if (data.containsKey('lat')) {
      context.handle(
        _latMeta,
        lat.isAcceptableOrUnknown(data['lat']!, _latMeta),
      );
    } else if (isInserting) {
      context.missing(_latMeta);
    }
    if (data.containsKey('lng')) {
      context.handle(
        _lngMeta,
        lng.isAcceptableOrUnknown(data['lng']!, _lngMeta),
      );
    } else if (isInserting) {
      context.missing(_lngMeta);
    }
    if (data.containsKey('open_now')) {
      context.handle(
        _openNowMeta,
        openNow.isAcceptableOrUnknown(data['open_now']!, _openNowMeta),
      );
    } else if (isInserting) {
      context.missing(_openNowMeta);
    }
    if (data.containsKey('photo_url')) {
      context.handle(
        _photoUrlMeta,
        photoUrl.isAcceptableOrUnknown(data['photo_url']!, _photoUrlMeta),
      );
    } else if (isInserting) {
      context.missing(_photoUrlMeta);
    }
    if (data.containsKey('description')) {
      context.handle(
        _descriptionMeta,
        description.isAcceptableOrUnknown(
          data['description']!,
          _descriptionMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_descriptionMeta);
    }
    if (data.containsKey('updated_at')) {
      context.handle(
        _updatedAtMeta,
        updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_updatedAtMeta);
    }
    if (data.containsKey('pending_sync')) {
      context.handle(
        _pendingSyncMeta,
        pendingSync.isAcceptableOrUnknown(
          data['pending_sync']!,
          _pendingSyncMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  SpotRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SpotRow(
      id:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}id'],
          )!,
      name:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}name'],
          )!,
      category:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}category'],
          )!,
      rating:
          attachedDatabase.typeMapping.read(
            DriftSqlType.double,
            data['${effectivePrefix}rating'],
          )!,
      priceLevel:
          attachedDatabase.typeMapping.read(
            DriftSqlType.int,
            data['${effectivePrefix}price_level'],
          )!,
      lat:
          attachedDatabase.typeMapping.read(
            DriftSqlType.double,
            data['${effectivePrefix}lat'],
          )!,
      lng:
          attachedDatabase.typeMapping.read(
            DriftSqlType.double,
            data['${effectivePrefix}lng'],
          )!,
      openNow:
          attachedDatabase.typeMapping.read(
            DriftSqlType.bool,
            data['${effectivePrefix}open_now'],
          )!,
      photoUrl:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}photo_url'],
          )!,
      description:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}description'],
          )!,
      updatedAt:
          attachedDatabase.typeMapping.read(
            DriftSqlType.int,
            data['${effectivePrefix}updated_at'],
          )!,
      pendingSync:
          attachedDatabase.typeMapping.read(
            DriftSqlType.bool,
            data['${effectivePrefix}pending_sync'],
          )!,
    );
  }

  @override
  $SpotsTable createAlias(String alias) {
    return $SpotsTable(attachedDatabase, alias);
  }
}

class SpotRow extends DataClass implements Insertable<SpotRow> {
  final String id;
  final String name;
  final String category;
  final double rating;
  final int priceLevel;
  final double lat;
  final double lng;
  final bool openNow;
  final String photoUrl;
  final String description;
  final int updatedAt;

  /// True while a local edit has not been confirmed by the server.
  final bool pendingSync;
  const SpotRow({
    required this.id,
    required this.name,
    required this.category,
    required this.rating,
    required this.priceLevel,
    required this.lat,
    required this.lng,
    required this.openNow,
    required this.photoUrl,
    required this.description,
    required this.updatedAt,
    required this.pendingSync,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['name'] = Variable<String>(name);
    map['category'] = Variable<String>(category);
    map['rating'] = Variable<double>(rating);
    map['price_level'] = Variable<int>(priceLevel);
    map['lat'] = Variable<double>(lat);
    map['lng'] = Variable<double>(lng);
    map['open_now'] = Variable<bool>(openNow);
    map['photo_url'] = Variable<String>(photoUrl);
    map['description'] = Variable<String>(description);
    map['updated_at'] = Variable<int>(updatedAt);
    map['pending_sync'] = Variable<bool>(pendingSync);
    return map;
  }

  SpotsCompanion toCompanion(bool nullToAbsent) {
    return SpotsCompanion(
      id: Value(id),
      name: Value(name),
      category: Value(category),
      rating: Value(rating),
      priceLevel: Value(priceLevel),
      lat: Value(lat),
      lng: Value(lng),
      openNow: Value(openNow),
      photoUrl: Value(photoUrl),
      description: Value(description),
      updatedAt: Value(updatedAt),
      pendingSync: Value(pendingSync),
    );
  }

  factory SpotRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SpotRow(
      id: serializer.fromJson<String>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      category: serializer.fromJson<String>(json['category']),
      rating: serializer.fromJson<double>(json['rating']),
      priceLevel: serializer.fromJson<int>(json['priceLevel']),
      lat: serializer.fromJson<double>(json['lat']),
      lng: serializer.fromJson<double>(json['lng']),
      openNow: serializer.fromJson<bool>(json['openNow']),
      photoUrl: serializer.fromJson<String>(json['photoUrl']),
      description: serializer.fromJson<String>(json['description']),
      updatedAt: serializer.fromJson<int>(json['updatedAt']),
      pendingSync: serializer.fromJson<bool>(json['pendingSync']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'name': serializer.toJson<String>(name),
      'category': serializer.toJson<String>(category),
      'rating': serializer.toJson<double>(rating),
      'priceLevel': serializer.toJson<int>(priceLevel),
      'lat': serializer.toJson<double>(lat),
      'lng': serializer.toJson<double>(lng),
      'openNow': serializer.toJson<bool>(openNow),
      'photoUrl': serializer.toJson<String>(photoUrl),
      'description': serializer.toJson<String>(description),
      'updatedAt': serializer.toJson<int>(updatedAt),
      'pendingSync': serializer.toJson<bool>(pendingSync),
    };
  }

  SpotRow copyWith({
    String? id,
    String? name,
    String? category,
    double? rating,
    int? priceLevel,
    double? lat,
    double? lng,
    bool? openNow,
    String? photoUrl,
    String? description,
    int? updatedAt,
    bool? pendingSync,
  }) => SpotRow(
    id: id ?? this.id,
    name: name ?? this.name,
    category: category ?? this.category,
    rating: rating ?? this.rating,
    priceLevel: priceLevel ?? this.priceLevel,
    lat: lat ?? this.lat,
    lng: lng ?? this.lng,
    openNow: openNow ?? this.openNow,
    photoUrl: photoUrl ?? this.photoUrl,
    description: description ?? this.description,
    updatedAt: updatedAt ?? this.updatedAt,
    pendingSync: pendingSync ?? this.pendingSync,
  );
  SpotRow copyWithCompanion(SpotsCompanion data) {
    return SpotRow(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      category: data.category.present ? data.category.value : this.category,
      rating: data.rating.present ? data.rating.value : this.rating,
      priceLevel:
          data.priceLevel.present ? data.priceLevel.value : this.priceLevel,
      lat: data.lat.present ? data.lat.value : this.lat,
      lng: data.lng.present ? data.lng.value : this.lng,
      openNow: data.openNow.present ? data.openNow.value : this.openNow,
      photoUrl: data.photoUrl.present ? data.photoUrl.value : this.photoUrl,
      description:
          data.description.present ? data.description.value : this.description,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
      pendingSync:
          data.pendingSync.present ? data.pendingSync.value : this.pendingSync,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SpotRow(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('category: $category, ')
          ..write('rating: $rating, ')
          ..write('priceLevel: $priceLevel, ')
          ..write('lat: $lat, ')
          ..write('lng: $lng, ')
          ..write('openNow: $openNow, ')
          ..write('photoUrl: $photoUrl, ')
          ..write('description: $description, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('pendingSync: $pendingSync')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    name,
    category,
    rating,
    priceLevel,
    lat,
    lng,
    openNow,
    photoUrl,
    description,
    updatedAt,
    pendingSync,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SpotRow &&
          other.id == this.id &&
          other.name == this.name &&
          other.category == this.category &&
          other.rating == this.rating &&
          other.priceLevel == this.priceLevel &&
          other.lat == this.lat &&
          other.lng == this.lng &&
          other.openNow == this.openNow &&
          other.photoUrl == this.photoUrl &&
          other.description == this.description &&
          other.updatedAt == this.updatedAt &&
          other.pendingSync == this.pendingSync);
}

class SpotsCompanion extends UpdateCompanion<SpotRow> {
  final Value<String> id;
  final Value<String> name;
  final Value<String> category;
  final Value<double> rating;
  final Value<int> priceLevel;
  final Value<double> lat;
  final Value<double> lng;
  final Value<bool> openNow;
  final Value<String> photoUrl;
  final Value<String> description;
  final Value<int> updatedAt;
  final Value<bool> pendingSync;
  final Value<int> rowid;
  const SpotsCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.category = const Value.absent(),
    this.rating = const Value.absent(),
    this.priceLevel = const Value.absent(),
    this.lat = const Value.absent(),
    this.lng = const Value.absent(),
    this.openNow = const Value.absent(),
    this.photoUrl = const Value.absent(),
    this.description = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.pendingSync = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  SpotsCompanion.insert({
    required String id,
    required String name,
    required String category,
    required double rating,
    required int priceLevel,
    required double lat,
    required double lng,
    required bool openNow,
    required String photoUrl,
    required String description,
    required int updatedAt,
    this.pendingSync = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       name = Value(name),
       category = Value(category),
       rating = Value(rating),
       priceLevel = Value(priceLevel),
       lat = Value(lat),
       lng = Value(lng),
       openNow = Value(openNow),
       photoUrl = Value(photoUrl),
       description = Value(description),
       updatedAt = Value(updatedAt);
  static Insertable<SpotRow> custom({
    Expression<String>? id,
    Expression<String>? name,
    Expression<String>? category,
    Expression<double>? rating,
    Expression<int>? priceLevel,
    Expression<double>? lat,
    Expression<double>? lng,
    Expression<bool>? openNow,
    Expression<String>? photoUrl,
    Expression<String>? description,
    Expression<int>? updatedAt,
    Expression<bool>? pendingSync,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (category != null) 'category': category,
      if (rating != null) 'rating': rating,
      if (priceLevel != null) 'price_level': priceLevel,
      if (lat != null) 'lat': lat,
      if (lng != null) 'lng': lng,
      if (openNow != null) 'open_now': openNow,
      if (photoUrl != null) 'photo_url': photoUrl,
      if (description != null) 'description': description,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (pendingSync != null) 'pending_sync': pendingSync,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SpotsCompanion copyWith({
    Value<String>? id,
    Value<String>? name,
    Value<String>? category,
    Value<double>? rating,
    Value<int>? priceLevel,
    Value<double>? lat,
    Value<double>? lng,
    Value<bool>? openNow,
    Value<String>? photoUrl,
    Value<String>? description,
    Value<int>? updatedAt,
    Value<bool>? pendingSync,
    Value<int>? rowid,
  }) {
    return SpotsCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      category: category ?? this.category,
      rating: rating ?? this.rating,
      priceLevel: priceLevel ?? this.priceLevel,
      lat: lat ?? this.lat,
      lng: lng ?? this.lng,
      openNow: openNow ?? this.openNow,
      photoUrl: photoUrl ?? this.photoUrl,
      description: description ?? this.description,
      updatedAt: updatedAt ?? this.updatedAt,
      pendingSync: pendingSync ?? this.pendingSync,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (category.present) {
      map['category'] = Variable<String>(category.value);
    }
    if (rating.present) {
      map['rating'] = Variable<double>(rating.value);
    }
    if (priceLevel.present) {
      map['price_level'] = Variable<int>(priceLevel.value);
    }
    if (lat.present) {
      map['lat'] = Variable<double>(lat.value);
    }
    if (lng.present) {
      map['lng'] = Variable<double>(lng.value);
    }
    if (openNow.present) {
      map['open_now'] = Variable<bool>(openNow.value);
    }
    if (photoUrl.present) {
      map['photo_url'] = Variable<String>(photoUrl.value);
    }
    if (description.present) {
      map['description'] = Variable<String>(description.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<int>(updatedAt.value);
    }
    if (pendingSync.present) {
      map['pending_sync'] = Variable<bool>(pendingSync.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SpotsCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('category: $category, ')
          ..write('rating: $rating, ')
          ..write('priceLevel: $priceLevel, ')
          ..write('lat: $lat, ')
          ..write('lng: $lng, ')
          ..write('openNow: $openNow, ')
          ..write('photoUrl: $photoUrl, ')
          ..write('description: $description, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('pendingSync: $pendingSync, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $ReviewsTable extends Reviews with TableInfo<$ReviewsTable, ReviewRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $ReviewsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _spotIdMeta = const VerificationMeta('spotId');
  @override
  late final GeneratedColumn<String> spotId = GeneratedColumn<String>(
    'spot_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _authorMeta = const VerificationMeta('author');
  @override
  late final GeneratedColumn<String> author = GeneratedColumn<String>(
    'author',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _starsMeta = const VerificationMeta('stars');
  @override
  late final GeneratedColumn<int> stars = GeneratedColumn<int>(
    'stars',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _reviewTextMeta = const VerificationMeta(
    'reviewText',
  );
  @override
  late final GeneratedColumn<String> reviewText = GeneratedColumn<String>(
    'text',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<int> createdAt = GeneratedColumn<int>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    spotId,
    author,
    stars,
    reviewText,
    createdAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'reviews';
  @override
  VerificationContext validateIntegrity(
    Insertable<ReviewRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('spot_id')) {
      context.handle(
        _spotIdMeta,
        spotId.isAcceptableOrUnknown(data['spot_id']!, _spotIdMeta),
      );
    } else if (isInserting) {
      context.missing(_spotIdMeta);
    }
    if (data.containsKey('author')) {
      context.handle(
        _authorMeta,
        author.isAcceptableOrUnknown(data['author']!, _authorMeta),
      );
    } else if (isInserting) {
      context.missing(_authorMeta);
    }
    if (data.containsKey('stars')) {
      context.handle(
        _starsMeta,
        stars.isAcceptableOrUnknown(data['stars']!, _starsMeta),
      );
    } else if (isInserting) {
      context.missing(_starsMeta);
    }
    if (data.containsKey('text')) {
      context.handle(
        _reviewTextMeta,
        reviewText.isAcceptableOrUnknown(data['text']!, _reviewTextMeta),
      );
    } else if (isInserting) {
      context.missing(_reviewTextMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  ReviewRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return ReviewRow(
      id:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}id'],
          )!,
      spotId:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}spot_id'],
          )!,
      author:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}author'],
          )!,
      stars:
          attachedDatabase.typeMapping.read(
            DriftSqlType.int,
            data['${effectivePrefix}stars'],
          )!,
      reviewText:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}text'],
          )!,
      createdAt:
          attachedDatabase.typeMapping.read(
            DriftSqlType.int,
            data['${effectivePrefix}created_at'],
          )!,
    );
  }

  @override
  $ReviewsTable createAlias(String alias) {
    return $ReviewsTable(attachedDatabase, alias);
  }
}

class ReviewRow extends DataClass implements Insertable<ReviewRow> {
  final String id;
  final String spotId;
  final String author;
  final int stars;

  /// Was `body` in schema v1; v2 renamed the SQL column to match the API's `text`.
  final String reviewText;
  final int createdAt;
  const ReviewRow({
    required this.id,
    required this.spotId,
    required this.author,
    required this.stars,
    required this.reviewText,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['spot_id'] = Variable<String>(spotId);
    map['author'] = Variable<String>(author);
    map['stars'] = Variable<int>(stars);
    map['text'] = Variable<String>(reviewText);
    map['created_at'] = Variable<int>(createdAt);
    return map;
  }

  ReviewsCompanion toCompanion(bool nullToAbsent) {
    return ReviewsCompanion(
      id: Value(id),
      spotId: Value(spotId),
      author: Value(author),
      stars: Value(stars),
      reviewText: Value(reviewText),
      createdAt: Value(createdAt),
    );
  }

  factory ReviewRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return ReviewRow(
      id: serializer.fromJson<String>(json['id']),
      spotId: serializer.fromJson<String>(json['spotId']),
      author: serializer.fromJson<String>(json['author']),
      stars: serializer.fromJson<int>(json['stars']),
      reviewText: serializer.fromJson<String>(json['reviewText']),
      createdAt: serializer.fromJson<int>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'spotId': serializer.toJson<String>(spotId),
      'author': serializer.toJson<String>(author),
      'stars': serializer.toJson<int>(stars),
      'reviewText': serializer.toJson<String>(reviewText),
      'createdAt': serializer.toJson<int>(createdAt),
    };
  }

  ReviewRow copyWith({
    String? id,
    String? spotId,
    String? author,
    int? stars,
    String? reviewText,
    int? createdAt,
  }) => ReviewRow(
    id: id ?? this.id,
    spotId: spotId ?? this.spotId,
    author: author ?? this.author,
    stars: stars ?? this.stars,
    reviewText: reviewText ?? this.reviewText,
    createdAt: createdAt ?? this.createdAt,
  );
  ReviewRow copyWithCompanion(ReviewsCompanion data) {
    return ReviewRow(
      id: data.id.present ? data.id.value : this.id,
      spotId: data.spotId.present ? data.spotId.value : this.spotId,
      author: data.author.present ? data.author.value : this.author,
      stars: data.stars.present ? data.stars.value : this.stars,
      reviewText:
          data.reviewText.present ? data.reviewText.value : this.reviewText,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('ReviewRow(')
          ..write('id: $id, ')
          ..write('spotId: $spotId, ')
          ..write('author: $author, ')
          ..write('stars: $stars, ')
          ..write('reviewText: $reviewText, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, spotId, author, stars, reviewText, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is ReviewRow &&
          other.id == this.id &&
          other.spotId == this.spotId &&
          other.author == this.author &&
          other.stars == this.stars &&
          other.reviewText == this.reviewText &&
          other.createdAt == this.createdAt);
}

class ReviewsCompanion extends UpdateCompanion<ReviewRow> {
  final Value<String> id;
  final Value<String> spotId;
  final Value<String> author;
  final Value<int> stars;
  final Value<String> reviewText;
  final Value<int> createdAt;
  final Value<int> rowid;
  const ReviewsCompanion({
    this.id = const Value.absent(),
    this.spotId = const Value.absent(),
    this.author = const Value.absent(),
    this.stars = const Value.absent(),
    this.reviewText = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  ReviewsCompanion.insert({
    required String id,
    required String spotId,
    required String author,
    required int stars,
    required String reviewText,
    required int createdAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       spotId = Value(spotId),
       author = Value(author),
       stars = Value(stars),
       reviewText = Value(reviewText),
       createdAt = Value(createdAt);
  static Insertable<ReviewRow> custom({
    Expression<String>? id,
    Expression<String>? spotId,
    Expression<String>? author,
    Expression<int>? stars,
    Expression<String>? reviewText,
    Expression<int>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (spotId != null) 'spot_id': spotId,
      if (author != null) 'author': author,
      if (stars != null) 'stars': stars,
      if (reviewText != null) 'text': reviewText,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  ReviewsCompanion copyWith({
    Value<String>? id,
    Value<String>? spotId,
    Value<String>? author,
    Value<int>? stars,
    Value<String>? reviewText,
    Value<int>? createdAt,
    Value<int>? rowid,
  }) {
    return ReviewsCompanion(
      id: id ?? this.id,
      spotId: spotId ?? this.spotId,
      author: author ?? this.author,
      stars: stars ?? this.stars,
      reviewText: reviewText ?? this.reviewText,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (spotId.present) {
      map['spot_id'] = Variable<String>(spotId.value);
    }
    if (author.present) {
      map['author'] = Variable<String>(author.value);
    }
    if (stars.present) {
      map['stars'] = Variable<int>(stars.value);
    }
    if (reviewText.present) {
      map['text'] = Variable<String>(reviewText.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<int>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('ReviewsCompanion(')
          ..write('id: $id, ')
          ..write('spotId: $spotId, ')
          ..write('author: $author, ')
          ..write('stars: $stars, ')
          ..write('reviewText: $reviewText, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $OutboxTable extends Outbox with TableInfo<$OutboxTable, OutboxOp> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $OutboxTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _seqMeta = const VerificationMeta('seq');
  @override
  late final GeneratedColumn<int> seq = GeneratedColumn<int>(
    'seq',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _opIdMeta = const VerificationMeta('opId');
  @override
  late final GeneratedColumn<String> opId = GeneratedColumn<String>(
    'op_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
    defaultConstraints: GeneratedColumn.constraintIsAlways('UNIQUE'),
  );
  static const VerificationMeta _typeMeta = const VerificationMeta('type');
  @override
  late final GeneratedColumn<String> type = GeneratedColumn<String>(
    'type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _entityIdMeta = const VerificationMeta(
    'entityId',
  );
  @override
  late final GeneratedColumn<String> entityId = GeneratedColumn<String>(
    'entity_id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _payloadMeta = const VerificationMeta(
    'payload',
  );
  @override
  late final GeneratedColumn<String> payload = GeneratedColumn<String>(
    'payload',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<int> createdAt = GeneratedColumn<int>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    seq,
    opId,
    type,
    entityId,
    payload,
    createdAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'outbox';
  @override
  VerificationContext validateIntegrity(
    Insertable<OutboxOp> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('seq')) {
      context.handle(
        _seqMeta,
        seq.isAcceptableOrUnknown(data['seq']!, _seqMeta),
      );
    }
    if (data.containsKey('op_id')) {
      context.handle(
        _opIdMeta,
        opId.isAcceptableOrUnknown(data['op_id']!, _opIdMeta),
      );
    } else if (isInserting) {
      context.missing(_opIdMeta);
    }
    if (data.containsKey('type')) {
      context.handle(
        _typeMeta,
        type.isAcceptableOrUnknown(data['type']!, _typeMeta),
      );
    } else if (isInserting) {
      context.missing(_typeMeta);
    }
    if (data.containsKey('entity_id')) {
      context.handle(
        _entityIdMeta,
        entityId.isAcceptableOrUnknown(data['entity_id']!, _entityIdMeta),
      );
    } else if (isInserting) {
      context.missing(_entityIdMeta);
    }
    if (data.containsKey('payload')) {
      context.handle(
        _payloadMeta,
        payload.isAcceptableOrUnknown(data['payload']!, _payloadMeta),
      );
    } else if (isInserting) {
      context.missing(_payloadMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {seq};
  @override
  OutboxOp map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return OutboxOp(
      seq:
          attachedDatabase.typeMapping.read(
            DriftSqlType.int,
            data['${effectivePrefix}seq'],
          )!,
      opId:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}op_id'],
          )!,
      type:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}type'],
          )!,
      entityId:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}entity_id'],
          )!,
      payload:
          attachedDatabase.typeMapping.read(
            DriftSqlType.string,
            data['${effectivePrefix}payload'],
          )!,
      createdAt:
          attachedDatabase.typeMapping.read(
            DriftSqlType.int,
            data['${effectivePrefix}created_at'],
          )!,
    );
  }

  @override
  $OutboxTable createAlias(String alias) {
    return $OutboxTable(attachedDatabase, alias);
  }
}

class OutboxOp extends DataClass implements Insertable<OutboxOp> {
  final int seq;
  final String opId;
  final String type;
  final String entityId;
  final String payload;
  final int createdAt;
  const OutboxOp({
    required this.seq,
    required this.opId,
    required this.type,
    required this.entityId,
    required this.payload,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['seq'] = Variable<int>(seq);
    map['op_id'] = Variable<String>(opId);
    map['type'] = Variable<String>(type);
    map['entity_id'] = Variable<String>(entityId);
    map['payload'] = Variable<String>(payload);
    map['created_at'] = Variable<int>(createdAt);
    return map;
  }

  OutboxCompanion toCompanion(bool nullToAbsent) {
    return OutboxCompanion(
      seq: Value(seq),
      opId: Value(opId),
      type: Value(type),
      entityId: Value(entityId),
      payload: Value(payload),
      createdAt: Value(createdAt),
    );
  }

  factory OutboxOp.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return OutboxOp(
      seq: serializer.fromJson<int>(json['seq']),
      opId: serializer.fromJson<String>(json['opId']),
      type: serializer.fromJson<String>(json['type']),
      entityId: serializer.fromJson<String>(json['entityId']),
      payload: serializer.fromJson<String>(json['payload']),
      createdAt: serializer.fromJson<int>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'seq': serializer.toJson<int>(seq),
      'opId': serializer.toJson<String>(opId),
      'type': serializer.toJson<String>(type),
      'entityId': serializer.toJson<String>(entityId),
      'payload': serializer.toJson<String>(payload),
      'createdAt': serializer.toJson<int>(createdAt),
    };
  }

  OutboxOp copyWith({
    int? seq,
    String? opId,
    String? type,
    String? entityId,
    String? payload,
    int? createdAt,
  }) => OutboxOp(
    seq: seq ?? this.seq,
    opId: opId ?? this.opId,
    type: type ?? this.type,
    entityId: entityId ?? this.entityId,
    payload: payload ?? this.payload,
    createdAt: createdAt ?? this.createdAt,
  );
  OutboxOp copyWithCompanion(OutboxCompanion data) {
    return OutboxOp(
      seq: data.seq.present ? data.seq.value : this.seq,
      opId: data.opId.present ? data.opId.value : this.opId,
      type: data.type.present ? data.type.value : this.type,
      entityId: data.entityId.present ? data.entityId.value : this.entityId,
      payload: data.payload.present ? data.payload.value : this.payload,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('OutboxOp(')
          ..write('seq: $seq, ')
          ..write('opId: $opId, ')
          ..write('type: $type, ')
          ..write('entityId: $entityId, ')
          ..write('payload: $payload, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(seq, opId, type, entityId, payload, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is OutboxOp &&
          other.seq == this.seq &&
          other.opId == this.opId &&
          other.type == this.type &&
          other.entityId == this.entityId &&
          other.payload == this.payload &&
          other.createdAt == this.createdAt);
}

class OutboxCompanion extends UpdateCompanion<OutboxOp> {
  final Value<int> seq;
  final Value<String> opId;
  final Value<String> type;
  final Value<String> entityId;
  final Value<String> payload;
  final Value<int> createdAt;
  const OutboxCompanion({
    this.seq = const Value.absent(),
    this.opId = const Value.absent(),
    this.type = const Value.absent(),
    this.entityId = const Value.absent(),
    this.payload = const Value.absent(),
    this.createdAt = const Value.absent(),
  });
  OutboxCompanion.insert({
    this.seq = const Value.absent(),
    required String opId,
    required String type,
    required String entityId,
    required String payload,
    required int createdAt,
  }) : opId = Value(opId),
       type = Value(type),
       entityId = Value(entityId),
       payload = Value(payload),
       createdAt = Value(createdAt);
  static Insertable<OutboxOp> custom({
    Expression<int>? seq,
    Expression<String>? opId,
    Expression<String>? type,
    Expression<String>? entityId,
    Expression<String>? payload,
    Expression<int>? createdAt,
  }) {
    return RawValuesInsertable({
      if (seq != null) 'seq': seq,
      if (opId != null) 'op_id': opId,
      if (type != null) 'type': type,
      if (entityId != null) 'entity_id': entityId,
      if (payload != null) 'payload': payload,
      if (createdAt != null) 'created_at': createdAt,
    });
  }

  OutboxCompanion copyWith({
    Value<int>? seq,
    Value<String>? opId,
    Value<String>? type,
    Value<String>? entityId,
    Value<String>? payload,
    Value<int>? createdAt,
  }) {
    return OutboxCompanion(
      seq: seq ?? this.seq,
      opId: opId ?? this.opId,
      type: type ?? this.type,
      entityId: entityId ?? this.entityId,
      payload: payload ?? this.payload,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (seq.present) {
      map['seq'] = Variable<int>(seq.value);
    }
    if (opId.present) {
      map['op_id'] = Variable<String>(opId.value);
    }
    if (type.present) {
      map['type'] = Variable<String>(type.value);
    }
    if (entityId.present) {
      map['entity_id'] = Variable<String>(entityId.value);
    }
    if (payload.present) {
      map['payload'] = Variable<String>(payload.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<int>(createdAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('OutboxCompanion(')
          ..write('seq: $seq, ')
          ..write('opId: $opId, ')
          ..write('type: $type, ')
          ..write('entityId: $entityId, ')
          ..write('payload: $payload, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $SpotsTable spots = $SpotsTable(this);
  late final $ReviewsTable reviews = $ReviewsTable(this);
  late final $OutboxTable outbox = $OutboxTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [spots, reviews, outbox];
}

typedef $$SpotsTableCreateCompanionBuilder =
    SpotsCompanion Function({
      required String id,
      required String name,
      required String category,
      required double rating,
      required int priceLevel,
      required double lat,
      required double lng,
      required bool openNow,
      required String photoUrl,
      required String description,
      required int updatedAt,
      Value<bool> pendingSync,
      Value<int> rowid,
    });
typedef $$SpotsTableUpdateCompanionBuilder =
    SpotsCompanion Function({
      Value<String> id,
      Value<String> name,
      Value<String> category,
      Value<double> rating,
      Value<int> priceLevel,
      Value<double> lat,
      Value<double> lng,
      Value<bool> openNow,
      Value<String> photoUrl,
      Value<String> description,
      Value<int> updatedAt,
      Value<bool> pendingSync,
      Value<int> rowid,
    });

class $$SpotsTableFilterComposer extends Composer<_$AppDatabase, $SpotsTable> {
  $$SpotsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get category => $composableBuilder(
    column: $table.category,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get rating => $composableBuilder(
    column: $table.rating,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get priceLevel => $composableBuilder(
    column: $table.priceLevel,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get lat => $composableBuilder(
    column: $table.lat,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<double> get lng => $composableBuilder(
    column: $table.lng,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get openNow => $composableBuilder(
    column: $table.openNow,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get photoUrl => $composableBuilder(
    column: $table.photoUrl,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<bool> get pendingSync => $composableBuilder(
    column: $table.pendingSync,
    builder: (column) => ColumnFilters(column),
  );
}

class $$SpotsTableOrderingComposer
    extends Composer<_$AppDatabase, $SpotsTable> {
  $$SpotsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get category => $composableBuilder(
    column: $table.category,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get rating => $composableBuilder(
    column: $table.rating,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get priceLevel => $composableBuilder(
    column: $table.priceLevel,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get lat => $composableBuilder(
    column: $table.lat,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<double> get lng => $composableBuilder(
    column: $table.lng,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get openNow => $composableBuilder(
    column: $table.openNow,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get photoUrl => $composableBuilder(
    column: $table.photoUrl,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get updatedAt => $composableBuilder(
    column: $table.updatedAt,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<bool> get pendingSync => $composableBuilder(
    column: $table.pendingSync,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$SpotsTableAnnotationComposer
    extends Composer<_$AppDatabase, $SpotsTable> {
  $$SpotsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get category =>
      $composableBuilder(column: $table.category, builder: (column) => column);

  GeneratedColumn<double> get rating =>
      $composableBuilder(column: $table.rating, builder: (column) => column);

  GeneratedColumn<int> get priceLevel => $composableBuilder(
    column: $table.priceLevel,
    builder: (column) => column,
  );

  GeneratedColumn<double> get lat =>
      $composableBuilder(column: $table.lat, builder: (column) => column);

  GeneratedColumn<double> get lng =>
      $composableBuilder(column: $table.lng, builder: (column) => column);

  GeneratedColumn<bool> get openNow =>
      $composableBuilder(column: $table.openNow, builder: (column) => column);

  GeneratedColumn<String> get photoUrl =>
      $composableBuilder(column: $table.photoUrl, builder: (column) => column);

  GeneratedColumn<String> get description => $composableBuilder(
    column: $table.description,
    builder: (column) => column,
  );

  GeneratedColumn<int> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);

  GeneratedColumn<bool> get pendingSync => $composableBuilder(
    column: $table.pendingSync,
    builder: (column) => column,
  );
}

class $$SpotsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $SpotsTable,
          SpotRow,
          $$SpotsTableFilterComposer,
          $$SpotsTableOrderingComposer,
          $$SpotsTableAnnotationComposer,
          $$SpotsTableCreateCompanionBuilder,
          $$SpotsTableUpdateCompanionBuilder,
          (SpotRow, BaseReferences<_$AppDatabase, $SpotsTable, SpotRow>),
          SpotRow,
          PrefetchHooks Function()
        > {
  $$SpotsTableTableManager(_$AppDatabase db, $SpotsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer:
              () => $$SpotsTableFilterComposer($db: db, $table: table),
          createOrderingComposer:
              () => $$SpotsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer:
              () => $$SpotsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> name = const Value.absent(),
                Value<String> category = const Value.absent(),
                Value<double> rating = const Value.absent(),
                Value<int> priceLevel = const Value.absent(),
                Value<double> lat = const Value.absent(),
                Value<double> lng = const Value.absent(),
                Value<bool> openNow = const Value.absent(),
                Value<String> photoUrl = const Value.absent(),
                Value<String> description = const Value.absent(),
                Value<int> updatedAt = const Value.absent(),
                Value<bool> pendingSync = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SpotsCompanion(
                id: id,
                name: name,
                category: category,
                rating: rating,
                priceLevel: priceLevel,
                lat: lat,
                lng: lng,
                openNow: openNow,
                photoUrl: photoUrl,
                description: description,
                updatedAt: updatedAt,
                pendingSync: pendingSync,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String name,
                required String category,
                required double rating,
                required int priceLevel,
                required double lat,
                required double lng,
                required bool openNow,
                required String photoUrl,
                required String description,
                required int updatedAt,
                Value<bool> pendingSync = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SpotsCompanion.insert(
                id: id,
                name: name,
                category: category,
                rating: rating,
                priceLevel: priceLevel,
                lat: lat,
                lng: lng,
                openNow: openNow,
                photoUrl: photoUrl,
                description: description,
                updatedAt: updatedAt,
                pendingSync: pendingSync,
                rowid: rowid,
              ),
          withReferenceMapper:
              (p0) =>
                  p0
                      .map(
                        (e) => (
                          e.readTable<$SpotsTable, SpotRow>(table),
                          BaseReferences<_$AppDatabase, $SpotsTable, SpotRow>(
                            db,
                            table,
                            e,
                          ),
                        ),
                      )
                      .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$SpotsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $SpotsTable,
      SpotRow,
      $$SpotsTableFilterComposer,
      $$SpotsTableOrderingComposer,
      $$SpotsTableAnnotationComposer,
      $$SpotsTableCreateCompanionBuilder,
      $$SpotsTableUpdateCompanionBuilder,
      (SpotRow, BaseReferences<_$AppDatabase, $SpotsTable, SpotRow>),
      SpotRow,
      PrefetchHooks Function()
    >;
typedef $$ReviewsTableCreateCompanionBuilder =
    ReviewsCompanion Function({
      required String id,
      required String spotId,
      required String author,
      required int stars,
      required String reviewText,
      required int createdAt,
      Value<int> rowid,
    });
typedef $$ReviewsTableUpdateCompanionBuilder =
    ReviewsCompanion Function({
      Value<String> id,
      Value<String> spotId,
      Value<String> author,
      Value<int> stars,
      Value<String> reviewText,
      Value<int> createdAt,
      Value<int> rowid,
    });

class $$ReviewsTableFilterComposer
    extends Composer<_$AppDatabase, $ReviewsTable> {
  $$ReviewsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get spotId => $composableBuilder(
    column: $table.spotId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get author => $composableBuilder(
    column: $table.author,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get stars => $composableBuilder(
    column: $table.stars,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get reviewText => $composableBuilder(
    column: $table.reviewText,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$ReviewsTableOrderingComposer
    extends Composer<_$AppDatabase, $ReviewsTable> {
  $$ReviewsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get spotId => $composableBuilder(
    column: $table.spotId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get author => $composableBuilder(
    column: $table.author,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get stars => $composableBuilder(
    column: $table.stars,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get reviewText => $composableBuilder(
    column: $table.reviewText,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$ReviewsTableAnnotationComposer
    extends Composer<_$AppDatabase, $ReviewsTable> {
  $$ReviewsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get spotId =>
      $composableBuilder(column: $table.spotId, builder: (column) => column);

  GeneratedColumn<String> get author =>
      $composableBuilder(column: $table.author, builder: (column) => column);

  GeneratedColumn<int> get stars =>
      $composableBuilder(column: $table.stars, builder: (column) => column);

  GeneratedColumn<String> get reviewText => $composableBuilder(
    column: $table.reviewText,
    builder: (column) => column,
  );

  GeneratedColumn<int> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$ReviewsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $ReviewsTable,
          ReviewRow,
          $$ReviewsTableFilterComposer,
          $$ReviewsTableOrderingComposer,
          $$ReviewsTableAnnotationComposer,
          $$ReviewsTableCreateCompanionBuilder,
          $$ReviewsTableUpdateCompanionBuilder,
          (ReviewRow, BaseReferences<_$AppDatabase, $ReviewsTable, ReviewRow>),
          ReviewRow,
          PrefetchHooks Function()
        > {
  $$ReviewsTableTableManager(_$AppDatabase db, $ReviewsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer:
              () => $$ReviewsTableFilterComposer($db: db, $table: table),
          createOrderingComposer:
              () => $$ReviewsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer:
              () => $$ReviewsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> spotId = const Value.absent(),
                Value<String> author = const Value.absent(),
                Value<int> stars = const Value.absent(),
                Value<String> reviewText = const Value.absent(),
                Value<int> createdAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => ReviewsCompanion(
                id: id,
                spotId: spotId,
                author: author,
                stars: stars,
                reviewText: reviewText,
                createdAt: createdAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String spotId,
                required String author,
                required int stars,
                required String reviewText,
                required int createdAt,
                Value<int> rowid = const Value.absent(),
              }) => ReviewsCompanion.insert(
                id: id,
                spotId: spotId,
                author: author,
                stars: stars,
                reviewText: reviewText,
                createdAt: createdAt,
                rowid: rowid,
              ),
          withReferenceMapper:
              (p0) =>
                  p0
                      .map(
                        (e) => (
                          e.readTable<$ReviewsTable, ReviewRow>(table),
                          BaseReferences<
                            _$AppDatabase,
                            $ReviewsTable,
                            ReviewRow
                          >(db, table, e),
                        ),
                      )
                      .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$ReviewsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $ReviewsTable,
      ReviewRow,
      $$ReviewsTableFilterComposer,
      $$ReviewsTableOrderingComposer,
      $$ReviewsTableAnnotationComposer,
      $$ReviewsTableCreateCompanionBuilder,
      $$ReviewsTableUpdateCompanionBuilder,
      (ReviewRow, BaseReferences<_$AppDatabase, $ReviewsTable, ReviewRow>),
      ReviewRow,
      PrefetchHooks Function()
    >;
typedef $$OutboxTableCreateCompanionBuilder =
    OutboxCompanion Function({
      Value<int> seq,
      required String opId,
      required String type,
      required String entityId,
      required String payload,
      required int createdAt,
    });
typedef $$OutboxTableUpdateCompanionBuilder =
    OutboxCompanion Function({
      Value<int> seq,
      Value<String> opId,
      Value<String> type,
      Value<String> entityId,
      Value<String> payload,
      Value<int> createdAt,
    });

class $$OutboxTableFilterComposer
    extends Composer<_$AppDatabase, $OutboxTable> {
  $$OutboxTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get seq => $composableBuilder(
    column: $table.seq,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get opId => $composableBuilder(
    column: $table.opId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get type => $composableBuilder(
    column: $table.type,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get entityId => $composableBuilder(
    column: $table.entityId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get payload => $composableBuilder(
    column: $table.payload,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$OutboxTableOrderingComposer
    extends Composer<_$AppDatabase, $OutboxTable> {
  $$OutboxTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get seq => $composableBuilder(
    column: $table.seq,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get opId => $composableBuilder(
    column: $table.opId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get type => $composableBuilder(
    column: $table.type,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get entityId => $composableBuilder(
    column: $table.entityId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get payload => $composableBuilder(
    column: $table.payload,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$OutboxTableAnnotationComposer
    extends Composer<_$AppDatabase, $OutboxTable> {
  $$OutboxTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get seq =>
      $composableBuilder(column: $table.seq, builder: (column) => column);

  GeneratedColumn<String> get opId =>
      $composableBuilder(column: $table.opId, builder: (column) => column);

  GeneratedColumn<String> get type =>
      $composableBuilder(column: $table.type, builder: (column) => column);

  GeneratedColumn<String> get entityId =>
      $composableBuilder(column: $table.entityId, builder: (column) => column);

  GeneratedColumn<String> get payload =>
      $composableBuilder(column: $table.payload, builder: (column) => column);

  GeneratedColumn<int> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$OutboxTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $OutboxTable,
          OutboxOp,
          $$OutboxTableFilterComposer,
          $$OutboxTableOrderingComposer,
          $$OutboxTableAnnotationComposer,
          $$OutboxTableCreateCompanionBuilder,
          $$OutboxTableUpdateCompanionBuilder,
          (OutboxOp, BaseReferences<_$AppDatabase, $OutboxTable, OutboxOp>),
          OutboxOp,
          PrefetchHooks Function()
        > {
  $$OutboxTableTableManager(_$AppDatabase db, $OutboxTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer:
              () => $$OutboxTableFilterComposer($db: db, $table: table),
          createOrderingComposer:
              () => $$OutboxTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer:
              () => $$OutboxTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> seq = const Value.absent(),
                Value<String> opId = const Value.absent(),
                Value<String> type = const Value.absent(),
                Value<String> entityId = const Value.absent(),
                Value<String> payload = const Value.absent(),
                Value<int> createdAt = const Value.absent(),
              }) => OutboxCompanion(
                seq: seq,
                opId: opId,
                type: type,
                entityId: entityId,
                payload: payload,
                createdAt: createdAt,
              ),
          createCompanionCallback:
              ({
                Value<int> seq = const Value.absent(),
                required String opId,
                required String type,
                required String entityId,
                required String payload,
                required int createdAt,
              }) => OutboxCompanion.insert(
                seq: seq,
                opId: opId,
                type: type,
                entityId: entityId,
                payload: payload,
                createdAt: createdAt,
              ),
          withReferenceMapper:
              (p0) =>
                  p0
                      .map(
                        (e) => (
                          e.readTable<$OutboxTable, OutboxOp>(table),
                          BaseReferences<_$AppDatabase, $OutboxTable, OutboxOp>(
                            db,
                            table,
                            e,
                          ),
                        ),
                      )
                      .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$OutboxTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $OutboxTable,
      OutboxOp,
      $$OutboxTableFilterComposer,
      $$OutboxTableOrderingComposer,
      $$OutboxTableAnnotationComposer,
      $$OutboxTableCreateCompanionBuilder,
      $$OutboxTableUpdateCompanionBuilder,
      (OutboxOp, BaseReferences<_$AppDatabase, $OutboxTable, OutboxOp>),
      OutboxOp,
      PrefetchHooks Function()
    >;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$SpotsTableTableManager get spots =>
      $$SpotsTableTableManager(_db, _db.spots);
  $$ReviewsTableTableManager get reviews =>
      $$ReviewsTableTableManager(_db, _db.reviews);
  $$OutboxTableTableManager get outbox =>
      $$OutboxTableTableManager(_db, _db.outbox);
}
