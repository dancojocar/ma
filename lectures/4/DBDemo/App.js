import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  ListView
} from 'react-native';

import SQLite from 'react-native-sqlite-storage';

SQLite.DEBUG(true);
SQLite.enablePromise(true);

const database_name = "test5.db";
const database_version = "1.0";
const database_displayname = "SQLite Test Database";
const database_size = 200000;
let db;

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      progress: [],
      dataSource: new ListView.DataSource({
        rowHasChanged: (row1, row2) => {
          row1 !== row2;
        },
      })
    }
  }

  componentWillUnmount() {
    this.closeDatabase();
  }

  errorCB(err) {
    console.log("error: ", err);
    this.state.progress.push("Error " + (err.message || err));
    this.setState(this.state);
  }

  populateDatabase(db) {
    let that = this;
    that.state.progress.push("Database integrity check");
    that.setState(that.state);
    db.executeSql('SELECT 1 FROM Employees LIMIT 1').then(() => {
      console.log("in populateDatabase");
      that.state.progress.push("Database is ready ... executing query ...");
      that.setState(that.state);
      db.transaction((tx) => that.queryEmployees(tx)).then(() => {
        that.state.progress.push("Processing completed");
        that.setState(that.state);
      });
    }).catch((error) => {
      console.log("Received error: ", error);
      that.state.progress.push("Database not yet ready ... populating data");
      that.setState(that.state);
      db.transaction((tx) => that.populateDB(tx)).then(() => {
        that.state.progress.push("Database populated ... executing query ...");
        that.setState(that.state);
        db.transaction((tx) => that.queryEmployees(tx)).then(() => {
          console.log("Transaction is now finished");
          that.state.progress.push("Processing completed");
          that.setState(that.state);
          that.closeDatabase()
        });
      }).catch((error) => {
        console.log("Unable to populate " + error);
      });
    });
  }

  populateDB(tx) {
    let that = this;
    that.state.progress.push("Executing DROP stmts");
    that.setState(that.state);

    tx.executeSql('DROP TABLE IF EXISTS Employees;');
    tx.executeSql('DROP TABLE IF EXISTS Offices;');
    tx.executeSql('DROP TABLE IF EXISTS Departments;');

    that.state.progress.push("Executing CREATE stmts");
    that.setState(that.state);

    tx.executeSql('CREATE TABLE IF NOT EXISTS Version( '
      + 'version_id INTEGER PRIMARY KEY NOT NULL); ').catch((error) => {
      that.errorCB(error)
    });

    tx.executeSql('CREATE TABLE IF NOT EXISTS Departments( '
      + 'department_id INTEGER PRIMARY KEY NOT NULL, '
      + 'name VARCHAR(30) ); ').catch((error) => {
      that.errorCB(error)
    });

    tx.executeSql('CREATE TABLE IF NOT EXISTS Offices( '
      + 'office_id INTEGER PRIMARY KEY NOT NULL, '
      + 'name VARCHAR(20), '
      + 'longtitude FLOAT, '
      + 'latitude FLOAT ) ; ').catch((error) => {
      that.errorCB(error)
    });

    tx.executeSql('CREATE TABLE IF NOT EXISTS Employees( '
      + 'employe_id INTEGER PRIMARY KEY NOT NULL, '
      + 'name VARCHAR(55), '
      + 'office INTEGER, '
      + 'department INTEGER, '
      + 'FOREIGN KEY ( office ) REFERENCES Offices ( office_id ) '
      + 'FOREIGN KEY ( department ) REFERENCES Departments ( department_id ));').catch((error) => {
      that.errorCB(error)
    });

    that.state.progress.push("Executing INSERT stmts");
    that.setState(that.state);


    tx.executeSql('INSERT INTO Departments (name) VALUES ("Client Services");');
    tx.executeSql('INSERT INTO Departments (name) VALUES ("Investor Services");');
    tx.executeSql('INSERT INTO Departments (name) VALUES ("Shipping");');
    tx.executeSql('INSERT INTO Departments (name) VALUES ("Direct Sales");');

    tx.executeSql('INSERT INTO Offices (name, longtitude, latitude) VALUES ("Denver", 59.8,  34.1);');
    tx.executeSql('INSERT INTO Offices (name, longtitude, latitude) VALUES ("Warsaw", 15.7, 54.1);');
    tx.executeSql('INSERT INTO Offices (name, longtitude, latitude) VALUES ("Berlin", 35.3, 12.1);');
    tx.executeSql('INSERT INTO Offices (name, longtitude, latitude) VALUES ("Paris", 10.7, 14.1);');

    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Sylvester Stallone", 2,  4);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Elvis Presley", 2, 4);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Leslie Nelson", 3,  4);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Fidel Castro", 3, 3);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Bill Clinton", 1, 3);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Margaret Thatcher", 1, 3);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Donald Trump", 1, 3);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Dr DRE", 2, 2);');
    tx.executeSql('INSERT INTO Employees (name, office, department) VALUES ("Samantha Fox", 2, 1);');
    console.log("all config SQL done");
  }

  queryEmployees(tx) {
    let that = this;
    console.log("Executing employee query");
    tx.executeSql('SELECT a.name, b.name as deptName FROM Employees a, Departments b WHERE a.department = b.department_id').then(([tx, results]) => {
      that.state.progress.push("Query completed");
      that.setState(that.state);
      let len = results.rows.length;
      for (let i = 0; i < len; i++) {
        let row = results.rows.item(i);
        that.state.progress.push(`Empl Name: ${row.name}, Dept Name: ${row.deptName}`);
      }
      that.setState(that.state);
    }).catch((error) => {
      console.log(error);
    });
  }

  loadAndQueryDB() {
    let that = this;
    that.state.progress.push("Plugin integrity check ...");
    that.setState(that.state);
    SQLite.echoTest().then(() => {
      that.state.progress.push("Integrity check passed ...");
      that.setState(that.state);
      that.state.progress.push("Opening database ...");
      that.setState(that.state);
      SQLite.openDatabase({name: database_name, createFromLocation: 1}).then((DB) => {
        db = DB;
        that.state.progress.push("Database OPEN");
        that.setState(that.state);
        console.log("Before calling populateDatabase");
        that.populateDatabase(DB);
      }).catch((error) => {
        console.log("Not able to open " + database_name + " error: " + error);
      });
    }).catch(error => {
      that.state.progress.push("echoTest failed - plugin not functional error: " + error);
      that.setState(that.state);
    });
  }

  closeDatabase() {
    let that = this;
    if (db) {
      console.log("Closing database ...");
      that.state.progress = ["Closing DB"];
      that.setState(that.state);
      db.close().then((status) => {
        that.state.progress.push("Database CLOSED");
        that.setState(that.state);
      }).catch((error) => {
        console.log("error while closing: " + error);
        that.errorCB(error);
      });
    } else {
      that.state.progress.push("Database was not OPENED");
      that.setState(that.state);
    }
  }

  deleteDatabase() {
    let that = this;
    that.state.progress = ["Deleting database"];
    that.setState(that.state);
    SQLite.deleteDatabase(database_name).then(() => {
      console.log("Database DELETED");
      that.state.progress.push("Database DELETED");
      that.setState(that.state);
    }).catch((error) => {
      that.errorCB(error);
    });
  }

  runDemo() {
    let that = this;
    that.state.progress = ["Starting SQLite Promise Demo"];
    that.setState(that.state);
    that.loadAndQueryDB();
  }

  renderProgressEntry(entry) {
    return (<View style={listStyles.li}>
      <View>
        <Text style={listStyles.liText}>{entry}</Text>
      </View>
    </View>)
  }


  render() {
    var ds = new ListView.DataSource({
      rowHasChanged: (row1, row2) => {
        row1 !== row2;
      }
    });
    return (<View style={styles.mainContainer}>
      <View style={styles.toolbar}>
        <Text style={styles.toolbarButton} onPress={() => this.runDemo()}>
          Run Demo
        </Text>
        <Text style={styles.toolbarButton} onPress={() => this.closeDatabase()}>
          Close DB
        </Text>
        <Text style={styles.toolbarButton} onPress={() => this.deleteDatabase()}>
          Delete DB
        </Text>
      </View>
      <ListView
        enableEmptySections={true}
        dataSource={ds.cloneWithRows(this.state.progress)}
        renderRow={this.renderProgressEntry}
        style={listStyles.liContainer}/>
    </View>);
  }
}

const listStyles = StyleSheet.create({
  li: {
    borderBottomColor: '#c8c7cc',
    borderBottomWidth: 0.5,
    paddingTop: 15,
    paddingRight: 15,
    paddingBottom: 15,
  },
  liContainer: {
    backgroundColor: '#fff',
    flex: 1,
    paddingLeft: 15,
  },
  liIndent: {
    flex: 1,
  },
  liText: {
    color: '#333',
    fontSize: 17,
    fontWeight: '400',
    marginBottom: -3.5,
    marginTop: -3.5,
  },
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  toolbar: {
    backgroundColor: '#51c04d',
    paddingTop: 30,
    paddingBottom: 10,
    flexDirection: 'row'
  },
  toolbarButton: {
    color: 'blue',
    textAlign: 'center',
    flex: 1
  },
  mainContainer: {
    flex: 1
  }
});
