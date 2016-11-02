import React, {Component} from 'react';
import {Text, View, StyleSheet, TouchableHighlight} from 'react-native';

export class SecurityView extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Text style={styles.listItem}>{this.props.symbol.name}</Text>
        );
    }
}

const styles = StyleSheet.create({
    listItem: {
        margin: 10,
    }
});