import React from 'react';
import {
  View,
  ListView,
  StyleSheet,
  Navigator,
  TouchableOpacity,
  Text
} from 'react-native';

import * as Progress from 'react-native-progress';

import InfiniteScrollView from 'react-native-infinite-scroll-view';

var DomParser = require('xmldom').DOMParser;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginTop: 20,
  },
  separator: {
    flex: 1,
    height: StyleSheet.hairlineWidth,
    backgroundColor: '#8E8E8E',
  },
  progress: {
    marginTop: 80,
  },
});

class ListScreen extends React.Component {
  constructor(props){
    super(props);
    
    const ds = new ListView.DataSource({
        rowHasChanged: (r1, r2) => r1 !== r2
    });

    this.state = {
      dataSource: ds.cloneWithRows(['row 1', 'row 2']),
      currencies: [],
      loaded: false,
    };
  }
  componentDidMount(){
    this.fetchData();
  }
  fetchData(){
  setTimeout(() =>{
     fetch("http://bnr.ro/nbrfxrates10days.xml")
       .then((response) => response.text())
       .then((responseText) => {
           this.state.currencies = new Array();
           var parser = new DomParser();
           var doc = parser.parseFromString(responseText, "text/xml").documentElement;
           var cubes = doc.getElementsByTagName("Cube");
           for (var i=0;i<cubes.length;i++){
             var cube = cubes.item(i);
             var rates = cube.getElementsByTagName("Rate");
             for (var r=0;r<rates.length;r++){
               var rate = rates.item(r);
               var rateSymbol = rate.getAttribute("currency");
               var rateValue = rate.firstChild.data;
               var rateDate = cube.getAttribute("date");
               var currency = this.state.currencies.filter((c) => c.symbol === rateSymbol)[0]; 
               if (currency == null){
                 currency = {
                   symbol: rateSymbol,
                   values: [{date: rateDate, val: rateValue}],
	         };
                 this.state.currencies.push(currency);
               } else {
                 currency.values.push({date: rateDate, val: rateValue});
	       }
               //console.log('currency: '+currency.values[0].val);
	     }
           }
           this.setState({
              dataSource: this.state.dataSource.cloneWithRows(this.state.currencies),
              loaded: true,
	   });
       })
       .catch((err) => console.error(err))
       .done();
	}, 1000);
  }
  render(){
    if (!this.state.loaded){
      return (<View style={styles.progress}>
             <Text>Please wait ... </Text>
             <Progress.Bar progress={0.3} width={200} indeterminate={true} />
          </View>
     );
    }
    return (
      	<ListView style={styles.container}
          enableEmptySections={true}
          dataSource={this.state.dataSource}
          renderRow={(data) => 
            <TouchableOpacity onPress={()=> this.props.navigator.push({index: 1,
               passProps:{
                   symbol: data.symbol, 
                   date: data.values[0].date,
                   val: data.values[0].val,
                }})}>
               <View>
                 <Text style={styles.symbol}>{data.symbol}</Text>
               </View>
            </TouchableOpacity>
	  }
          renderSeparator={(sectionID, rowID, adjacentRowHighlighted) =>
            <View key={rowID} style={{height:1, backgroundColor: 'lightgray'}}/>
          }
       	/>
    );
  }
}

export default ListScreen;

