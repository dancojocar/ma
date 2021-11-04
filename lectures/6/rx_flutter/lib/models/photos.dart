import 'dart:convert';

class Photos {
  int total;
  int totalPages;
  List<Result> results;

  Photos({
    required this.total,
    required this.totalPages,
    required this.results,
  });

  factory Photos.fromRawJson(String str) => Photos.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory Photos.fromJson(Map<String, dynamic> json) => Photos(
        total: json["total"],
        totalPages: json["total_pages"],
        results: List<Result>.from(
            json["results"].map((x) => Result.fromJson(x))),
      );

  Map<String, dynamic> toJson() => {
        "total": total,
        "total_pages": totalPages,
        "results": List<dynamic>.from(results.map((x) => x.toJson())),
      };
}

class Result {
  String id;
  DateTime createdAt;
  DateTime updatedAt;
  int width;
  int height;
  String color;
  String description;
  String altDescription;
  Urls urls;
  ResultLinks links;
  List<dynamic> categories;
  bool sponsored;
  dynamic sponsoredBy;
  dynamic sponsoredImpressionsId;
  int likes;
  bool likedByUser;
  List<dynamic> currentUserCollections;
  User user;
  List<Tag> tags;

  Result({
    required this.id,
    required this.createdAt,
    required this.updatedAt,
    required this.width,
    required this.height,
    required this.color,
    required this.description,
    required this.altDescription,
    required this.urls,
    required this.links,
    required this.categories,
    required this.sponsored,
    this.sponsoredBy,
    this.sponsoredImpressionsId,
    required this.likes,
    required this.likedByUser,
    required this.currentUserCollections,
    required this.user,
    required this.tags,
  });

  factory Result.fromRawJson(String str) => Result.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory Result.fromJson(Map<String, dynamic> json) => Result(
        id: json["id"],
        createdAt: DateTime.parse(json["created_at"]),
        updatedAt: DateTime.parse(json["updated_at"]),
        width: json["width"],
        height: json["height"],
        color: json["color"],
        description: json["description"]??"",
        altDescription: json["alt_description"]??"",
        urls: Urls.fromJson(json["urls"]),
        links: ResultLinks.fromJson(json["links"]),
        categories: List<dynamic>.from(json["categories"].map((x) => x)),
        sponsored: json["sponsored"]??false,
        sponsoredBy: json["sponsored_by"],
        sponsoredImpressionsId: json["sponsored_impressions_id"],
        likes: json["likes"],
        likedByUser: json["liked_by_user"],
        currentUserCollections: List<dynamic>.from(
            json["current_user_collections"].map((x) => x)),
        user: User.fromJson(json["user"]),
        tags: List<Tag>.from(json["tags"].map((x) => Tag.fromJson(x))),
      );

  Map<String, dynamic> toJson() => {
        "id": id,
        "created_at": createdAt.toIso8601String(),
        "updated_at": updatedAt.toIso8601String(),
        "width": width,
        "height": height,
        "color": color,
        "description": description??"empty",
        "alt_description": altDescription,
        "urls": urls.toJson(),
        "links": links.toJson(),
        "categories": List<dynamic>.from(categories.map((x) => x)),
        "sponsored": sponsored,
        "sponsored_by": sponsoredBy,
        "sponsored_impressions_id": sponsoredImpressionsId,
        "likes": likes,
        "liked_by_user": likedByUser,
        "current_user_collections":
            List<dynamic>.from(currentUserCollections.map((x) => x)),
        "user": user.toJson(),
        "tags": List<dynamic>.from(tags.map((x) => x.toJson())),
      };
}

class ResultLinks {
  String self;
  String html;
  String download;
  String downloadLocation;

  ResultLinks({
    required this.self,
    required this.html,
    required this.download,
    required this.downloadLocation,
  });

  factory ResultLinks.fromRawJson(String str) =>
      ResultLinks.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory ResultLinks.fromJson(Map<String, dynamic> json) => ResultLinks(
        self: json["self"],
        html: json["html"],
        download: json["download"],
        downloadLocation: json["download_location"],
      );

  Map<String, dynamic> toJson() => {
        "self": self,
        "html": html,
        "download": download,
        "download_location": downloadLocation,
      };
}

class Tag {
  String title;

  Tag({
    required this.title,
  });

  factory Tag.fromRawJson(String str) => Tag.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory Tag.fromJson(Map<String, dynamic> json) => Tag(
        title: json["title"],
      );

  Map<String, dynamic> toJson() => {
        "title": title,
      };
}

class Urls {
  String raw;
  String full;
  String regular;
  String small;
  String thumb;

  Urls({
    required this.raw,
    required this.full,
    required this.regular,
    required this.small,
    required this.thumb,
  });

  factory Urls.fromRawJson(String str) => Urls.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory Urls.fromJson(Map<String, dynamic> json) => Urls(
        raw: json["raw"],
        full: json["full"],
        regular: json["regular"],
        small: json["small"],
        thumb: json["thumb"],
      );

  Map<String, dynamic> toJson() => {
        "raw": raw,
        "full": full,
        "regular": regular,
        "small": small,
        "thumb": thumb,
      };
}

class User {
  String id;
  DateTime updatedAt;
  String username;
  String name;
  String firstName;
  String lastName;
  String twitterUsername;
  String portfolioUrl;
  String bio;
  String location;
  UserLinks links;
  ProfileImage profileImage;
  String instagramUsername;
  int totalCollections;
  int totalLikes;
  int totalPhotos;
  bool acceptedTos;

  User({
    required this.id,
    required this.updatedAt,
    required this.username,
    required this.name,
    required this.firstName,
    required this.lastName,
    required this.twitterUsername,
    required this.portfolioUrl,
    required this.bio,
    required this.location,
    required this.links,
    required this.profileImage,
    required this.instagramUsername,
    required this.totalCollections,
    required this.totalLikes,
    required this.totalPhotos,
    required this.acceptedTos,
  });

  factory User.fromRawJson(String str) => User.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory User.fromJson(Map<String, dynamic> json) => User(
        id: json["id"],
        updatedAt: DateTime.parse(json["updated_at"]),
        username: json["username"],
        name: json["name"],
        firstName: json["first_name"],
        lastName: json["last_name"]??"",
        twitterUsername:
            json["twitter_username"]??"",
        portfolioUrl:
            json["portfolio_url"]??"",
        bio: json["bio"]??"",
        location: json["location"]??"",
        links: UserLinks.fromJson(json["links"]),
        profileImage: ProfileImage.fromJson(json["profile_image"]),
        instagramUsername: json["instagram_username"]??"",
        totalCollections: json["total_collections"],
        totalLikes: json["total_likes"],
        totalPhotos: json["total_photos"],
        acceptedTos: json["accepted_tos"],
      );

  Map<String, dynamic> toJson() => {
        "id": id,
        "updated_at": updatedAt.toIso8601String(),
        "username": username,
        "name": name,
        "first_name": firstName,
        "last_name": lastName,
        "twitter_username": twitterUsername??"",
        "portfolio_url": portfolioUrl??"",
        "bio": bio,
        "location": location??"",
        "links": links.toJson(),
        "profile_image": profileImage.toJson(),
        "instagram_username":
            instagramUsername??"",
        "total_collections": totalCollections,
        "total_likes": totalLikes,
        "total_photos": totalPhotos,
        "accepted_tos": acceptedTos,
      };
}

class UserLinks {
  String self;
  String html;
  String photos;
  String likes;
  String portfolio;
  String following;
  String followers;

  UserLinks({
    required this.self,
    required this.html,
    required this.photos,
    required this.likes,
    required this.portfolio,
    required this.following,
    required this.followers,
  });

  factory UserLinks.fromRawJson(String str) =>
      UserLinks.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory UserLinks.fromJson(Map<String, dynamic> json) => UserLinks(
        self: json["self"],
        html: json["html"],
        photos: json["photos"],
        likes: json["likes"],
        portfolio: json["portfolio"],
        following: json["following"],
        followers: json["followers"],
      );

  Map<String, dynamic> toJson() => {
        "self": self,
        "html": html,
        "photos": photos,
        "likes": likes,
        "portfolio": portfolio,
        "following": following,
        "followers": followers,
      };
}

class ProfileImage {
  String small;
  String medium;
  String large;

  ProfileImage({
    required this.small,
    required this.medium,
    required this.large,
  });

  factory ProfileImage.fromRawJson(String str) =>
      ProfileImage.fromJson(json.decode(str));

  String toRawJson() => json.encode(toJson());

  factory ProfileImage.fromJson(Map<String, dynamic> json) => ProfileImage(
        small: json["small"],
        medium: json["medium"],
        large: json["large"],
      );

  Map<String, dynamic> toJson() => {
        "small": small,
        "medium": medium,
        "large": large,
      };
}