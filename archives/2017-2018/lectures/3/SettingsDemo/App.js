import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  Alert
} from 'react-native';
import SettingsList from 'react-native-settings-list';

export default class App extends React.Component {
  constructor(){
    super();
    this.onValueChange = this.onValueChange.bind(this);
    this.state = {switchValue: false};
  }
  render() {
    var bgColor = '#DCE3F4';
    return (
      <View style={{backgroundColor:'#EFEFF4',flex:1}}>
        <View style={{borderBottomWidth:1, backgroundColor:'#f7f7f8',borderColor:'#c8c7cc'}}>
          <Text style={{alignSelf:'center',marginTop:30,marginBottom:10,fontWeight:'bold',fontSize:16}}>Settings</Text>
        </View>
        <View style={{backgroundColor:'#EFEFF4',flex:1}}>
          <SettingsList borderColor='#c8c7cc' defaultItemSize={50}>
            <SettingsList.Header headerStyle={{marginTop:15}}/>
            <SettingsList.Item
              icon={
                <Image style={styles.imageStyle} source={require('./images/airplane.png')}/>
              }
              hasSwitch={true}
              switchState={this.state.switchValue}
              switchOnValueChange={this.onValueChange}
              hasNavArrow={false}
              title='Airplane Mode'
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/wifi.png')}/>}
              title='Wi-Fi'
              titleInfo='Bill Wi The Science Fi'
              titleInfoStyle={styles.titleInfoStyle}
              onPress={() => Alert.alert('Route to Wifi Page')}
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/blutooth.png')}/>}
              title='Blutooth'
              titleInfo='Off'
              titleInfoStyle={styles.titleInfoStyle}
              onPress={() => Alert.alert('Route to Blutooth Page')}
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/cellular.png')}/>}
              title='Cellular'
              onPress={() => Alert.alert('Route To Cellular Page')}
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/hotspot.png')}/>}
              title='Personal Hotspot'
              titleInfo='Off'
              titleInfoStyle={styles.titleInfoStyle}
              onPress={() => Alert.alert('Route To Hotspot Page')}
            />
            <SettingsList.Header headerStyle={{marginTop:15}}/>
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/notifications.png')}/>}
              title='Notifications'
              onPress={() => Alert.alert('Route To Notifications Page')}
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/control.png')}/>}
              title='Control Center'
              onPress={() => Alert.alert('Route To Control Center Page')}
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/dnd.png')}/>}
              title='Do Not Disturb'
              onPress={() => Alert.alert('Route To Do Not Disturb Page')}
            />
            <SettingsList.Header headerStyle={{marginTop:15}}/>
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/general.png')}/>}
              title='General'
              onPress={() => Alert.alert('Route To General Page')}
            />
            <SettingsList.Item
              icon={<Image style={styles.imageStyle} source={require('./images/display.png')}/>}
              title='Display & Brightness'
              onPress={() => Alert.alert('Route To Display Page')}
            />
          </SettingsList>
        </View>
      </View>
    );
  }
  onValueChange(value){
    this.setState({switchValue: value});
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
