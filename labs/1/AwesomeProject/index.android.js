/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  ListView,
  View
} from 'react-native';


class MovieList extends Component {
	constructor(prop){
		super(prop);
		this.state={
			dataSource: new ListView.DataSource({
			rowHasChanged: (row1, row2) => row1 !== row2,
		      }),
		      loaded: false,
		}
	}

	componentDidMount(){
		this.fetchData();
	}

	fetchData() {
           setTimeout(()=>{
	    fetch("http://www.cs.ubbcluj.ro/~dan/ma/movies.json")
	      .then((response) => response.json())
	      .then((responseData) => {
		this.setState({
		  dataSource: this.state.dataSource.cloneWithRows(responseData.movies),
		  loaded: true,
		});
	      })
	      .catch((err) => { console.error(err);})
	      .done();
	}, 500);
	  }
	renderMovie(movie){
		return (<View>
			<Text>{movie.releaseYear}</Text>
			<Text>{movie.title}</Text>
			</View>);
	}

	render(){
		if (!this.state.loaded){
			return (<Text> Please wait!! </Text>);
		}
		return (
			<ListView
			dataSource={this.state.dataSource}
			renderRow={this.renderMovie}
			style={styles.listView}
		      />
		);
	}
}

class App extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
	<MovieList />
      </View>
    );
  }
}

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
listView: {
    paddingTop: 20,
    backgroundColor: '#F5FCFF',
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('AwesomeProject', () => App);
