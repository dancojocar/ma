import React, { Component } from 'react'; 
import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import Chart from 'react-native-chart';
import { Dimensions } from 'react-native';

var {height, width} = Dimensions.get('window');

const styles = StyleSheet.create({
    container: {
        height: height/4,
        width: width,
        alignItems: 'center',
        backgroundColor: 'white',
    },
    chart: {
        width: width-50,
        height: height-50,
    },
});

const data = [
    [0, 1],
    [1, 3],
    [3, 7],
    [4, 9],
];
const pieData = [
    [0, 5],
    [1, 2],
];
const pieColors = ["white", "yellow", "blue" ]

export class SimpleChart extends Component {
    getRandomInt(min, max) {
      return Math.floor(Math.random() * (max - min + 1)) + min;
    }
    render() {
        return (
            <View>
            <View style={styles.container}>
                <Chart
                    style={styles.chart}
                    data={data}
                    type="bar"
                    showDataPoint={true}
                    showGrid={false}
                 />
            </View>
            <View style={styles.container}>
                <Chart
                    style={styles.chart}
                    data={pieData}
                    type="pie"
                    sliceColors={pieColors}
                    showDataPoint={true}
                    showAxis={false}
                 />
            </View>
            <View style={styles.container}>
                <Chart
                    style={styles.chart}
                    data={data}
                    type="line"
                    showDataPoint={true}
                 />
            </View>
	    <TouchableOpacity onPress={() => { data.push([data.length-1,this.getRandomInt(1,10)]); this.setState({});} }>
		<Text>Add data point</Text>
	    </TouchableOpacity>
           </View>
        );
    }
}
