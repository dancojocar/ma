import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  Button
} from 'react-native';

import FileSystem from 'react-native-filesystem';

var fs = FileSystem;

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      content: "Foo test"
    }
  }

  writeContent() {
    console.log("Write content called");
    const fileContents = this.state.content;
    console.log("Write content: " + fileContents);
    if (fileContents !== "undefined") {
      fs.writeToFile('my-directory/my-file.txt', fileContents)
        .then(() => {
          console.log('file is written');
        })
        .catch((e) => {
          console.log("Error writing content: " + e);
        });
    }
  }

  readContent() {
    console.log("Read content");
    fs.readFile('my-directory/my-file.txt')
      .then((fileContents) => {
        if (fileContents !== "undefined") {
          console.log(`read from file: ${fileContents}`);
          this.setState({
            content: fileContents
          });
        }
      })
      .catch((e) => {
        console.log("Error reading content: " + e);
      });
  }

  render() {
    return (
      <View style={styles.container}>
        <Button title="Load" onPress={() => {
          this.readContent();
          console.log("content: " + this.state.content);
        }}/>
        <Button title="Write" onPress={() => {
          this.writeContent();
          console.log("content: " + this.state.content);
        }}/>
        <Text>File content:</Text>
        <TextInput style={{height: 200, width: 300, borderColor: 'gray', borderWidth: 1}}
                   placeholder={this.state.content}
        />
      </View>
    );
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
