import React from 'react';
import {StyleSheet, Text, View, Button} from 'react-native';
import {apiUrl, headers, authHeaders} from './api';

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      text: "Empty",
      auth: false,
      token: ""
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <Button title="Login" onPress={() => this.login()}/>
        <Button title="Fetch" onPress={() => this.retrieveData()}/>
        <Button title="Clear" onPress={() => this.clearToken()}/>
        <Text>{this.state.text}</Text>
      </View>
    );
  }

  login() {
    console.log("Login")
    fetch(`${apiUrl}/token-auth`, {
      method: 'POST',
      headers,
      body: JSON.stringify({Username: 'test', Password: 'test1'})
    })
      .then(res => {
        console.log("rez: " + res);
        return res.json();
      })
      .then(json => {
        console.log("json: " + JSON.stringify(json));
        this.setState({
          auth: true,
          token: json.token
        });
      })
      .catch(err => {
        this.setState({
          auth: false
        });
        console.log(`login err = ${err.message}`);
      });
    this.setState({text: "Login"})
  }

  retrieveData() {
    console.log("retrieveData")
    this.setState({text: "retrieveData"})
    return fetch(`${apiUrl}/p`, {
      method: 'GET',
      headers: authHeaders(this.state.token)
    })
      .then(res => {
        return res.json();
      })
      .then(json => {
        this.setState({
          text: JSON.stringify(json)
        });
        console.log(`loadPortfolios json: ${JSON.stringify(json)}`);
      })
      .catch(err => {
        console.log(`loadPortfolios err = ${err.message}`);
      });
  }

  clearToken() {
    this.setState({token:"", text:"Token removed"});
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
