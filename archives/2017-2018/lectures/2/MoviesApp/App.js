import React from 'react';
import {
    StyleSheet,
    Text,
    View,
    ListView,
    ActivityIndicator,
    Button
} from 'react-native';

export default class App extends React.Component {
    render() {
        return (
            <View style={styles.container}>
                <Text>Welcome to the MovieApp!</Text>
                <MovieList/>
            </View>
        );
    }
}

class MovieList extends React.Component {
    constructor(prop) {
        super(prop);
        this.state = {
            dataSource: new ListView.DataSource({
                rowHasChanged: (row1, row2) => row1 !== row2,
            }),
            loaded: 0,
        }
    }

    componentDidMount() {
        this.fetchData();
    }

    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    showRetry() {
        this.setState({
            loaded: 2,
        });
    }

    fetchData() {
        this.sleep(5000).then(() => {
            setTimeout(() => {
                fetch("http://www.cs.ubbcluj.ro/~dan/ma/MMXVII/movies.json")
                    .then((response) => {
                        if (response.status === 200) {
                            try {
                                return response.json();
                            } catch (e) {
                                console.log("Unable to parse response: " + response, e);
                                this.showRetry();
                                return null;
                            }
                        }
                        console.log("response: " + JSON.stringify(response));
                        this.showRetry();
                        return null;
                    })
                    .then((responseData) => {
                        if (responseData !== null) {
                            this.setState({
                                dataSource: this.state.dataSource.cloneWithRows(responseData.movies),
                                loaded: 1,
                            });
                        } else {
                            this.showRetry();
                        }
                    })
                    .catch((err) => {
                        console.error(err);
                        this.showRetry();
                    })
                    .done();
            }, 500);
        });
    }

    renderMovie(movie) {
        return (<View>
            <Text>{movie.releaseYear} - {movie.title}</Text>
        </View>);
    }

    render() {
        if (this.state.loaded === 0) {
            return (
                <View>
                    <Text> Please wait!! </Text>
                    <ActivityIndicator/>
                </View>);
        } else if (this.state.loaded === 2) {
            return (
                <View>
                    <Text> The content is not available </Text>
                    <Button title="Retry" onPress={() => {
                        this.setState({loaded: 0});
                        this.fetchData();
                    }}/>
                </View>);
        }
        return (
            <ListView
                dataSource={this.state.dataSource}
                renderRow={this.renderMovie}
                style={styles.listView}
            />
        );
    }
}


const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#F5FCFF',
    },
    welcome: {
        fontSize: 20,
        textAlign: 'center',
        margin: 10,
    },
    listView: {
        paddingTop: 20,
        backgroundColor: '#F5FCFF',
    },
    instructions: {
        textAlign: 'center',
        color: '#333333',
        marginBottom: 5,
    },
});