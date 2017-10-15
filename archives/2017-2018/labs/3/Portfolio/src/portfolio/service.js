import {getLogger} from '../core/utils';
import {apiUrl, authHeaders} from '../core/api';
const log = getLogger('portfolio/service');
const action = (type, payload) => ({type, payload});

const SAVE_PORTFOLIO_STARTED = 'portfolio/saveStarted';
const SAVE_PORTFOLIO_SUCCEEDED = 'portfolio/saveSucceeded';
const SAVE_PORTFOLIO_FAILED = 'portfolio/saveFailed';
const CANCEL_SAVE_PORTFOLIO = 'portfolio/cancelSave';

const LOAD_PORTFOLIO_STARTED = 'portfolio/loadStarted';
const LOAD_PORTFOLIO_SUCCEEDED = 'portfolio/loadSucceeded';
const LOAD_PORTFOLIO_FAILED = 'portfolio/loadFailed';
const CANCEL_LOAD_PORTFOLIO = 'portfolio/cancelLoad';

export const loadPortfolios = () => (dispatch, getState) => {
    log(`loadPortfolios started`);
    dispatch(action(LOAD_PORTFOLIO_STARTED));
    let ok = false;
    return fetch(`${apiUrl}/p`, {method: 'GET', headers: authHeaders(getState().auth.token)})
        .then(res => {
            ok = res.ok;
            return res.json();
        })
        .then(json => {
            log(`loadPortfolios ok: ${ok}, json: ${JSON.stringify(json)}`);
            if (!getState().portfolio.isLoadingCancelled) {
                dispatch(action(ok ? LOAD_PORTFOLIO_SUCCEEDED : LOAD_PORTFOLIO_FAILED, json));
            }
        })
        .catch(err => {
            log(`loadPortfolios err = ${err.message}`);
            if (!getState().portfolio.isLoadingCancelled) {
                dispatch(action(LOAD_PORTFOLIO_FAILED, {issue: [{error: err.message}]}));
            }
        });
};
export const cancelLoadPortfolios = () => action(CANCEL_LOAD_PORTFOLIO);

export const savePortfolios = (portfolio) => (dispatch, getState) => {

    const body = JSON.stringify(Object.assign({},portfolio,{symbols:[{name:"S1"},{name:"S2"}]}));
    log(`savePortfolios started`);
    dispatch(action(SAVE_PORTFOLIO_STARTED));
    let ok = false;
    const url = portfolio._id ? `${apiUrl}/p/${portfolio._id}` : `${apiUrl}/p`;
    const method = portfolio._id ? `PUT` : `POST`;
    return fetch(url, {method, headers: authHeaders(getState().auth.token), body})
        .then(res => {
            ok = res.ok;
            return res.json();
        })
        .then(json => {
            log(`savePortfolios ok: ${ok}, json: ${JSON.stringify(json)}`);
            if (!getState().portfolio.isSavingCancelled) {
                dispatch(action(ok ? SAVE_PORTFOLIO_SUCCEEDED : SAVE_PORTFOLIO_FAILED, json));
            }
        })
        .catch(err => {
            log(`savePortfolios err = ${err.message}`);
            if (!getState().isSavingCancelled) {
                dispatch(action(SAVE_PORTFOLIO_FAILED, {issue: [{error: err.message}]}));
            }
        });
};
export const cancelSavePortfolio = () => action(CANCEL_SAVE_PORTFOLIO);

export const portfolioReducer = (state = {items: [], isLoading: false, isSaving: false}, action) => { //newState (new object)
    switch(action.type) {
        case LOAD_PORTFOLIO_STARTED:
            return {...state, isLoading: true, isLoadingCancelled: false, issue: null};
        case LOAD_PORTFOLIO_SUCCEEDED:
            return {...state, items: action.payload, isLoading: false};
        case LOAD_PORTFOLIO_FAILED:
            return {...state, issue: action.payload.issue, isLoading: false};
        case CANCEL_LOAD_PORTFOLIO:
            return {...state, isLoading: false, isLoadingCancelled: true};
        case SAVE_PORTFOLIO_STARTED:
            return {...state, isSaving: true, isSavingCancelled: false, issue: null};
        case SAVE_PORTFOLIO_SUCCEEDED:
            let items = [...state.items];
            let index = items.findIndex((i) => i._id == action.payload._id);
            if (index != -1) {
                items.splice(index, 1, action.payload);
            } else {
                items.push(action.payload);
            }
            return {...state, items, isSaving: false};
        case SAVE_PORTFOLIO_FAILED:
            return {...state, issue: action.payload.issue, isSaving: false};
        case CANCEL_SAVE_PORTFOLIO:
            return {...state, isSaving: false, isSavingCancelled: true};
        default:
            return state;
    }
};

