import React from 'react';
import {StyleSheet, Text, View, Button} from 'react-native';

import {StackNavigator} from 'react-navigation';
import {TabNavigator} from "react-navigation";

import ChatScreen from './ChatScreen';
import RecentChatsScreen from './RecentChartsScreen';
import AllContactsScreen from './AllContactsScreen';

const MainScreenNavigator = TabNavigator({
  Recent: {screen: RecentChatsScreen},
  All: {screen: AllContactsScreen},
});

const SimpleApp = StackNavigator({
  Home: {
    screen: MainScreenNavigator,
    navigationOptions: {
      title: 'Chats',
    },
  },
  Chat: {screen: ChatScreen},
});

export default class App extends React.Component {
  render() {
    return <SimpleApp/>;
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
