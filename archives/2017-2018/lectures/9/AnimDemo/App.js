import React from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {TabNavigator} from 'react-navigation';

import SpinScreen from './SpinScreen'
import SequenceScreen from "./SequenceScreen";
import ParallelScreen from "./ParallelScreen";
import StaggerScreen from "./StaggerScreen";
import MultipleScreen from "./MultipleScreen";

const MyTabs = TabNavigator({
  Spin: {
    screen: SpinScreen,
  },
  Sequence: {
    screen: SequenceScreen,
  },
  Stagger:{
    screen: StaggerScreen,
  },
  Parallel:{
    screen: ParallelScreen,
  },
  Multiple:{
    screen: MultipleScreen,
  },
}, {
  tabBarPosition: 'top',
  animationEnabled: true,
  tabBarOptions: {
    activeTintColor: '#e91e63',
    labelStyle: {
      fontSize: 12,
    },
  },
});

export default class App extends React.Component {
  render() {
    return (<MyTabs/>);
  }
}