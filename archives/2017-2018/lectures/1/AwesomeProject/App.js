import React from 'react';
import {StyleSheet, Text, View, TextInput} from 'react-native';

export default class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            inputName: "Test"
        };
    }

    render() {
        return (
            <View style={styles.container}>
                <MyHello name={this.state.inputName}/>
                <TextInput
                    style={{
                        width: 80,
                        height: 40,
                        borderColor: 'gray',
                        borderWidth: 1
                    }}
                    onChangeText={(text) => {
                        this.setState({
                            inputName: text
                        });
                    }}/>
            </View>
        );
    }


}

class MyHello extends React.Component {
    constructor(props) {
        super(props);
        console.log(this.constructor.name+" constructor props: " + JSON.stringify(props));
        this.state = {
            name: this.props.name
        };
    }

    componentWillMount() {
        console.log(this.constructor.name+" componentWillMount")
    }

    render() {
        console.log(this.constructor.name+" render");
        return (<Text>Hello {this.props.name}</Text>);
    }

    componentWillReceiveProps(nextProps) {
        console.log(this.constructor.name+" componentWillReceiveProps nextProps: " + JSON.stringify(nextProps))
    }

    shouldComponentUpdate(nextProps, nextState) {
        console.log(this.constructor.name+" shouldComponentUpdate nextProps:" + JSON.stringify(nextProps) + ", nextState: " + JSON.stringify(nextState));
        return true
    }

    componentWillUpdate(nextProps, nextState) {
        console.log(this.constructor.name+" componentWillUpdate nextProps:" + JSON.stringify(nextProps) + ", nextState: " + JSON.stringify(nextState))
    }

    componentDidUpdate(prevProps, prevState) {
        console.log(this.constructor.name+" componentDidUpdate prevProps:" + JSON.stringify(prevProps) + ", prevState: " + JSON.stringify(prevState))
    }

    componentWillUnmount() {
        console.log(this.constructor.name+" componentWillUnmount");
    }
    componentDidMount() {
        console.log(this.constructor.name+" componentDidMount")
    }

    componentDidCatch(error, info) {
        console.log(this.constructor.name+" componentDidCatch error: " + JSON.stringify(error) + " info: " + JSON.stringify(info))
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
