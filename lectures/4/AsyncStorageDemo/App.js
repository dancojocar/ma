import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  AsyncStorage,
  Button
} from 'react-native';

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      text: "Empty"
    }
  }

  persistContent() {
    console.log("Before persisting content");
    AsyncStorage.setItem('@MyStore:key',
      'I like to save it.').then(() => {
      console.log("Yay! content persisted successfully!");
      this.setState({
        text: "Content persisted successfully!"
      });
    }).catch((error) => {
      console.log("Unable to persist the content" + error);
    });
  }

  retrieveContent() {
    console.log("Before retrieving content");
    AsyncStorage.getItem('@MyStore:key').then((value) => {
      // We have data!!
      console.log("Retrieved from storage:" + value);
      this.setState({
        text: value
      });
    }).catch((error) => {
      console.log("Unable to retrieve the content" + error);
    });
  }

  clearContent() {
    console.log("Before deleting content");
    AsyncStorage.removeItem('@MyStore:key').then(() => {
      console.log("Yay! deleted!");
      this.setState({
        text: "Yay! deleted!"
      });
    }).catch((error) => {
      console.log("Unable to remove the content" + error);
    });
  }

  render() {
    return (
      <View style={styles.container}>
        <View style={{
          flexDirection: 'row',
          justifyContent: 'space-between'
        }}>
          <Button style={{width: 50, height: 50, backgroundColor: 'powderblue'}}
                  title="Store"
                  onPress={() => this.persistContent()}/>
          <Button style={{width: 50, height: 50, backgroundColor: 'skyblue'}}
                  title="Load"
                  onPress={() => this.retrieveContent()}/>
          <Button
            style={{width: 50, height: 50, backgroundColor: 'red'}}
            title="Clear"
            onPress={() => this.clearContent()}/>
        </View>
        <Text>{this.state.text}</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
