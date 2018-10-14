import React from 'react';
import { StyleSheet, Text, View, Button } from 'react-native';

import {
  StackNavigator,
} from 'react-navigation';

import ListScreen from './ListScreen';
import DetailsScreen from './DetailsScreen';

const MyNavigator = StackNavigator({
  List: { screen: ListScreen },
  Details: { screen: DetailsScreen },
});

export default class App extends React.Component {
  render() {
    return (
      <MyNavigator/>
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
