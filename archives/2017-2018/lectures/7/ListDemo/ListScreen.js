import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  Button,
  ListView
} from 'react-native';

export default class ListScreen extends React.Component {
  static navigationOptions = {
    title: 'List of items',
  };

  myList = [
    {
      id: 1,
      name: 'Test1',
      value: 'Val1'
    },
    {
      id: 2,
      name: 'Test2',
      value: 'Val2'
    },
    {
      id: 3,
      name: 'Test3',
      value: 'Val3'
    },
    {
      id: 4,
      name: 'Test4',
      value: 'Val4'
    },
  ];


  constructor(props) {
    super(props);
    const ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});
    this.state = {
      dataSource: ds.cloneWithRows(this.myList),
    };
  }

  renderItem(item) {
    const {navigate} = this.props.navigation;
    return (
      <View>
        <Text>{item.name}</Text>
        <Button
          title='Update'
          onPress={() => navigate('Details', {item, onSelect: this.onSelect})}/>
      </View>
    );
  }

  onSelect = item => {
    let objIndex = this.myList.findIndex((obj => obj.id === item.id));
    if (objIndex >= 0) {
      this.myList[objIndex].name = item.title;
      this.myList[objIndex].value = item.value;
      const ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});
      this.setState({dataSource: ds.cloneWithRows(this.myList)});
    }
  };

  render() {
    return (
      <View>
        <ListView
          dataSource={this.state.dataSource}
          renderRow={(rowData) => this.renderItem(rowData)}
        />
      </View>
    );
  }
}