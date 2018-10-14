import React, {Component} from 'react';
import {Text, View, StyleSheet, TouchableHighlight} from 'react-native';

export class PortfolioView extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <TouchableHighlight onPress={() => this.props.onPress(this.props.portfolio)}>
                <View>
                    <Text style={styles.listItem}>{this.props.portfolio.name}</Text>
                </View>
            </TouchableHighlight>
        );
    }
}

const styles = StyleSheet.create({
    listItem: {
        margin: 10,
    }
});