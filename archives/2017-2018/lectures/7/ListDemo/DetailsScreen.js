import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  Button,
  ListView,
  TextInput
} from 'react-native';

import {NavigationActions} from 'react-navigation';

export default class DetailsScreen extends React.Component {
  static navigationOptions = {
    title: 'Details',
  };


  constructor(props) {
    super(props);
    this.state = {
      id: this.props.navigation.state.params.item.id,
      title: this.props.navigation.state.params.item.name,
      value: this.props.navigation.state.params.item.value,
    };
  }


  save(id, title, value) {
    const {navigation} = this.props;
    navigation.goBack();
    navigation.state.params.onSelect({id: id, title: title, value: value});
  }

  render() {
    return (
      <View>
        <Text>Details of {this.props.navigation.state.params.item.name}</Text>
        <TextInput
          style={{height: 40, borderColor: 'gray', borderWidth: 1}}
          onChangeText={(text) => this.setState({title: text})}
          value={this.state.title}
        />
        <TextInput
          style={{height: 40, borderColor: 'gray', borderWidth: 1}}
          onChangeText={(text) => this.setState({value: text})}

          value={this.state.value}
        />
        <Button
          title="Save"
          onPress={() => this.save(this.state.id, this.state.title, this.state.value)}/>
      </View>
    );
  }
}