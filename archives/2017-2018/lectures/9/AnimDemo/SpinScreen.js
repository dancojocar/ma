import React, {Component} from 'react'
import {
  StyleSheet,
  View,
  Animated,
  Easing
} from 'react-native'

const timing = 4000;

export default class SpinScreen extends Component {

  constructor(props) {
    super(props);
    this.spinValue = new Animated.Value(0)
  }

  componentDidMount() {
    this.spin()
  }

  spin() {
    this.spinValue.setValue(0);
    Animated.timing(
      this.spinValue,
      {
        toValue: 1,
        duration: timing,
        easing: Easing.linear
      }
    ).start(() => this.spin());
  }

  render() {
    /* This also works, to show functions instead of strings */
    // const getStartValue = () => '0deg'
    // const getEndValue = () => '360deg'
    // const spin = this.spinValue.interpolate({
    //   inputRange: [0, 1],
    //   outputRange: [getStartValue(), getEndValue()]
    // })
    const spin = this.spinValue.interpolate({
      inputRange: [0, 1],
      outputRange: ['0deg', '360deg']
    });
    return (
      <View style={styles.container}>
        <Animated.Image
          style={{width: 227, height: 200, transform: [{rotate: spin}]}}
          source={{uri: 'https://s3.amazonaws.com/media-p.slid.es/uploads/alexanderfarennikov/images/1198519/reactjs.png'}}/>
      </View>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  }
})