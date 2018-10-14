import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  Button,
  TextInput,
  ListView
} from 'react-native';

let ws;

export default class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      text: [],
      message: "",
      disabledSend: true,
      disabledOpen: false,
      disabledClose: true,
      dataSource: new ListView.DataSource({
        rowHasChanged: (row1, row2) => {
          row1 !== row2;
        }
      })
    }
  }

  sendMessage(message) {
    ws.send(message)
  }

  closeConnection() {
    ws.close(1000, "from RNClient");
    this.setState({
      disabledSend: true,
      disabledOpen: false,
      disabledClose: true
    })
  }

  openConnection() {
    ws = new WebSocket('ws://192.168.43.137:8080/echo');
    ws.onopen = () => {
      // connection opened
      // send a message
      ws.send('Something from RN');
      console.log("connection opened");
    };
    ws.onmessage = (e) => {
      // a message was received
      console.log("received: " + e.data);
      this.state.text.push(e.data);
      this.setState(this.state)
    };
    ws.onerror = (e) => {
      // an error occurred
      console.log("received error: " + e.message);
    };
    ws.onclose = (e) => {
      // connection closed
      console.log("closed connection with code: " + e.code, e.reason);
    };
    this.setState({
      disabledSend: false,
      disabledOpen: true,
      disabledClose: false
    });
  }

  renderEntry(entry) {
    return (<View style={listStyles.li}>
      <View>
        <Text style={listStyles.liText}>{entry}</Text>
      </View>
    </View>)
  }

  render() {
    return (
      <View style={styles.container}>
        <TextInput
          style={{height: 40, width: 200}}
          placeholder="Type here to send!"
          onChangeText={(message) => this.setState({message})}
        />
        <Button title="Send"
                onPress={() => this.sendMessage(this.state.message)}
                disabled={this.state.disabledSend}
        />
        <Button title="Connect"
                onPress={() => this.openConnection()}
                disabled={this.state.disabledOpen}
        />
        <Button
          title="Disconnect"
          onPress={() => this.closeConnection()}
          disabled={this.state.disabledClose}
        />
        <Text>Received from server:</Text>
        <ListView
          enableEmptySections={true}
          dataSource={this.state.dataSource.cloneWithRows(this.state.text)}
          renderRow={this.renderEntry}
          style={listStyles.liContainer}/>
      </View>
    );
  }
}

const
  styles = StyleSheet.create({
    container: {
      paddingTop: 15,
      flex: 1,
      backgroundColor: '#fff',
      alignItems: 'center',
      justifyContent: 'center',
    },
  });

const
  listStyles = StyleSheet.create({
    li: {
      borderBottomColor: '#c8c7cc',
      borderBottomWidth: 0.5,
      paddingTop: 15,
      paddingRight: 15,
      paddingBottom: 15,
    },
    liContainer: {
      backgroundColor: '#fff',
      paddingLeft: 15,
    },
    liText: {
      color: '#333',
      fontSize: 17,
      fontWeight: '400',
      marginBottom: -3.5,
      marginTop: -3.5,
    },
  });
