import React, {Component} from 'react';
import {
   TouchableHighlight, 
   Image, 
   AppRegistry,
   StyleSheet, 
   Text, 
   View
} from 'react-native';

class DetailScreen extends Component {
  constructor(props){
    super(props);
    this.state = {};
  }


  render() {
    return (
      <View style={styles.container}>
         <Text> Symbol: {this.props.symbol}</Text>
	 <Text> Rate: {this.props.val} RON </Text>
	 <Text> Date: {this.props.date} </Text>
      </View>
    );
  }
}

var styles = StyleSheet.create({
  container:{
    flex:1,
    padding: 10,
    paddingTop:70,
  },
});

export default DetailScreen;
