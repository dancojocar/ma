import React, {Component} from "react";
import {StyleSheet, Text, View, Button, ScrollView} from "react-native";
import {
  AdMobBanner,
  AdMobInterstitial,
  AdMobRewarded,
  PublisherBanner,
} from "expo";

const BANNER_ID = `ca-app-pub-2645035868897971/8377342840`;
const INTERSTITIAL_ID = `ca-app-pub-2645035868897971/5444057741`;
const REWARDED_ID = `ca-app-pub-2645035868897971/9610533465`;

AdMobInterstitial.setAdUnitID(INTERSTITIAL_ID);
AdMobInterstitial.setTestDeviceID("EMULATOR");
AdMobRewarded.setAdUnitID(REWARDED_ID);
console.disableYellowBox = true;

class App extends Component {
  _openInterstitial = () => {
    console.log("Interstitial");
    AdMobInterstitial.requestAd(() => AdMobInterstitial.showAd());
  };

  _openRewarded = () => {
    console.log("Rewarded");
    AdMobRewarded.requestAd(() => AdMobRewarded.showAd());
  };

  componentDidMount(){
    console.log("componentDidMount");
    AdMobRewarded.addEventListener('rewardedVideoDidRewardUser',
      (reward) => console.log('AdMobRewarded => rewarded', reward)
    );
  }

  render() {
    return (
      <ScrollView>
        <View style={styles.container}>
          <Text style={styles.title}>GOOGLE ADMOB DEMO</Text>
          <Text style={styles.title}>BANNER AD</Text>
          <AdMobBanner
            bannerSize="fullBanner"
            adUnitID={BANNER_ID}
            didFailToReceiveAdWithError={this.bannerError}
          />
          <Text style={styles.title}>PUBLISHER BANNER</Text>
          <PublisherBanner
            bannerSize="banner"
            adUnitID={BANNER_ID}
            didFailToReceiveAdWithError={this.bannerError}
            admobDispatchAppEvent={this.adMobEvent}
          />
          <View style={styles.btn}>
            <Text style={styles.title}>INTERSTITIAL AD</Text>
            <Button
              title="OPEN"
              color="steelblue"
              onPress={this._openInterstitial}
            />
          </View>
          <View style={styles.btn}>
            <Text style={styles.title}>REWARDED AD</Text>
            <Button title="OPEN" color="green" onPress={this._openRewarded}/>
          </View>
        </View>
      </ScrollView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    marginTop: 30
  },
  btn: {
    margin: 7
  },
  text: {
    textAlign: "center",
    fontSize: 24,
    color: "#000",
    backgroundColor: "#eee333",
    padding: 10,
    margin: 10
  },
  title: {
    marginTop: 10,
    fontSize: 30,
    color: "#333"
  }
});

export default App;
