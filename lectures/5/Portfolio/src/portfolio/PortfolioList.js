import React, {Component} from 'react';
import {ListView, Text, View, StatusBar, ActivityIndicator} from 'react-native';
import {PortfolioDetails} from './PortfolioDetails';
import {PortfolioView} from './PortfolioView';
import {loadPortfolios, cancelLoadPortfolios} from './service';
import {registerRightAction, getLogger, issueText} from '../core/utils';
import styles from '../core/styles';

const log = getLogger('PortfolioList');
const PORTFOLIO_LIST_ROUTE = 'portfolio/list';

export class PortfolioList extends Component {
    static get routeName() {
        return PORTFOLIO_LIST_ROUTE;
    }

    static get route() {
        return {name: PORTFOLIO_LIST_ROUTE, title: 'Portfolio List', rightText: 'New'};
    }

    constructor(props) {
        super(props);
        log('constructor: '+this.props.store);
        this.ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1.id !== r2.id});
        const portfolioState = this.props.store.getState().portfolio;
        this.state = {isLoading: portfolioState.isLoading, dataSource: this.ds.cloneWithRows(portfolioState.items)};
        registerRightAction(this.props.navigator, this.onNewPortfolio.bind(this));
    }

    render() {
        log('render');
        let message = issueText(this.state.issue);
        return (
            <View style={styles.content}>
                { this.state.isLoading &&
                <ActivityIndicator animating={true} style={styles.activityIndicator} size="large"/>
                }
                {message && <Text>{message}</Text>}
                <ListView
                    dataSource={this.state.dataSource}
                    enableEmptySections={true}
                    renderRow={portfolio => (<PortfolioView portfolio={portfolio}
                                                            onPress={(portfolio) => this.onPortfolioPress(portfolio)}/>)}/>
            </View>
        );
    }

    onNewPortfolio() {
        log('onNewPortfolio');
        this.props.navigator.push({...PortfolioDetails.route});
    }

    onPortfolioPress(portfolio) {
        log('onPortfolioPress');
        this.props.navigator.push({...PortfolioDetails.route, data: portfolio});
    }

    componentDidMount() {
        log('componentDidMount');
        const store = this.props.store;
        this.unsubscribe = store.subscribe(() => {
            log('setState');
            const state = this.state;
            const portfolioState = store.getState().portfolio;
            this.setState({dataSource: this.ds.cloneWithRows(portfolioState.items), isLoading: portfolioState.isLoading});
        });
        store.dispatch(loadPortfolios());
    }

    componentWillUnmount() {
        log('componentWillUnmount');
        this.unsubscribe();
        this.props.store.dispatch(cancelLoadPortfolios());
    }
}
