package com.example.ma.sm.database;


import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {

  @Override
  public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
    // During a migration, a DynamicRealm is exposed. A DynamicRealm is an untyped variant of a normal Realm, but
    // with the same object creation and query capabilities.
    // A DynamicRealm uses Strings instead of Class references because the Classes might not even exist or have been
    // renamed.

    // Access the Realm schema in order to create, modify or delete classes and their fields.
    RealmSchema schema = realm.getSchema();

    /************************************************
     // Version 0
     class Symbol
     @Required long id;
     @Required String name;
     @Required Date acquisitionDate;
     @Required long quantity;
     @Required double acquisitionPrice;
     // Version 1
     class Symbol
     @Required long portfolioId; //a link to the portfoliol
     ************************************************/
    // Migrate from version 0 to version 1
    if (oldVersion == 0) {
      RealmObjectSchema personSchema = schema.get("Symbol");

      personSchema
          .addField("portfolioId", Long.class, FieldAttribute.REQUIRED)
          .transform(new RealmObjectSchema.Function() {
            @Override
            public void apply(DynamicRealmObject obj) {
              obj.set("portfolioId", 0);
            }
          });
      oldVersion++;
    }
  }
}