import React, {Component} from 'react';
import {Text, View, TextInput, ActivityIndicator, ListView} from 'react-native';
import {savePortfolios, cancelSavePortfolio} from './service';
import {registerRightAction, issueText, getLogger} from '../core/utils';
import {SecurityView} from './SecurityView';

import styles from '../core/styles';

const log = getLogger('PortfolioDetails');
const PORTFOLIO_EDIT_ROUTE = 'portfolio/edit';

export class PortfolioDetails extends Component {
    static get routeName() {
        return PORTFOLIO_EDIT_ROUTE;
    }

    static get route() {
        return {name: PORTFOLIO_EDIT_ROUTE, title: 'Portfolio Details', rightText: 'Save'};
    }

    constructor(props) {
        log('constructor');
        super(props);
        const nav = this.props.navigator;
        const currentRoutes = nav.getCurrentRoutes();
        const currentRoute = currentRoutes[currentRoutes.length - 1];

        let port = currentRoute.data;
        if (port.symbols == undefined)
            port = {...currentRoute.data, symbols: [{name: "S1"}, {name: "S2"}]};
        this.ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1.id !== r2.id});
        log(`symbols: ${JSON.stringify(currentRoute.data.symbols)}`)
        if (currentRoute.data) {
            this.state = {
                portfolio: {...currentRoute.data},
                isSaving: false,
                dataSource: this.ds.cloneWithRows(port.symbols),
                loaded: true
            };
        } else {
            this.state = {portfolio: {name: ''}, isSaving: false, dataSource: null, loaded: false};
        }

        registerRightAction(this.props.navigator, this.onSave.bind(this));
    }

    render() {
        log('render');
        const state = this.state;
        let message = issueText(state.issue);
        return (
            <View style={styles.content}>
                { state.isSaving &&
                <ActivityIndicator animating={true} style={styles.activityIndicator} size="large"/>
                }
                <Text>Name:</Text>
                <TextInput value={state.portfolio.name}
                           onChangeText={(text) => this.updatePortfolioText(text)}></TextInput>

                { state.loaded &&
                <ListView
                    dataSource={this.state.dataSource}
                    enableEmptySections={true}
                    renderRow={symbol => (<SecurityView symbol={symbol}/>)}/>
                }
                {message && <Text>{message}</Text>}
            </View>
        );
    }

    componentDidMount() {
        log('componentDidMount');
        const store = this.props.store;
        this.unsubscribe = store.subscribe(() => {
            log('setState');
            const state = this.state;
            const portfolioState = store.getState().portfolio;
            this.setState({...state, issue: portfolioState.issue});
        });
    }

    componentWillUnmount() {
        log('componentWillUnmount');
        this.unsubscribe();
        this.props.store.dispatch(cancelSavePortfolio());
    }

    updatePortfolioText(text) {
        let newState = {...this.state};
        newState.portfolio.name = text;
        this.setState(newState);
    }

    onSave() {
        log('onSave');
        this.props.store.dispatch(savePortfolios(this.state.portfolio)).then(() => {
            log('onPortfolioSaved');
            if (!this.state.issue) {
                this.props.navigator.pop();
            }
        });
    }
}