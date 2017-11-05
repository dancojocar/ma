/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, {Component} from 'react';
import {AppRegistry} from 'react-native';

import {createStore, applyMiddleware, combineReducers}
    from 'redux';
import thunk from 'redux-thunk';
import createLogger from 'redux-logger';
import {portfolioReducer} from './src/portfolio';
import {authReducer} from './src/auth';
import {Router} from './src/Router'

const reducers = combineReducers({
    portfolio: portfolioReducer,
    auth: authReducer});
const store = createStore(reducers,
    applyMiddleware(thunk, createLogger()));

export default class Portfolio extends Component {
    render() {
        return (
            <Router store={store} />
        );
    }
}

AppRegistry.registerComponent('Portfolio', () => Portfolio);
