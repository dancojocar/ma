import * as SQLite from "expo-sqlite";

const MIGRATIONS = [
  `CREATE TABLE spots (
     id TEXT PRIMARY KEY NOT NULL,
     name TEXT NOT NULL,
     category TEXT NOT NULL,
     rating REAL NOT NULL,
     priceLevel INTEGER NOT NULL,
     lat REAL NOT NULL,
     lng REAL NOT NULL,
     openNow INTEGER NOT NULL,
     photoUrl TEXT NOT NULL,
     description TEXT NOT NULL,
     updatedAt INTEGER NOT NULL,
     serverUpdatedAt INTEGER NOT NULL,
     pendingSync INTEGER NOT NULL DEFAULT 0
   );
   CREATE TABLE reviews (
     id TEXT PRIMARY KEY NOT NULL,
     spotId TEXT NOT NULL,
     author TEXT NOT NULL,
     stars INTEGER NOT NULL,
     text TEXT NOT NULL,
     createdAt INTEGER NOT NULL
   );
   CREATE INDEX reviews_by_spot ON reviews (spotId);
   CREATE TABLE outbox (
     seq INTEGER PRIMARY KEY AUTOINCREMENT,
     opId TEXT NOT NULL UNIQUE,
     type TEXT NOT NULL,
     entityId TEXT NOT NULL,
     payload TEXT NOT NULL,
     createdAt INTEGER NOT NULL
   );`,
  // v2: a pending row keeps the server version it edited in `updatedAt` itself (CONTRACT §3).
  `ALTER TABLE spots DROP COLUMN serverUpdatedAt;`,
  `CREATE TABLE settings (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL);`,
];

let db: SQLite.SQLiteDatabase | null = null;

export function getDb(): SQLite.SQLiteDatabase {
  if (!db) {
    db = SQLite.openDatabaseSync("unieats.db");
    migrate(db);
  }
  return db;
}

function migrate(database: SQLite.SQLiteDatabase) {
  database.execSync("PRAGMA journal_mode = WAL");
  const version = database.getFirstSync<{ user_version: number }>("PRAGMA user_version")?.user_version ?? 0;
  for (let v = version; v < MIGRATIONS.length; v++) {
    database.withTransactionSync(() => {
      database.execSync(MIGRATIONS[v]);
      database.execSync(`PRAGMA user_version = ${v + 1}`);
    });
  }
}
