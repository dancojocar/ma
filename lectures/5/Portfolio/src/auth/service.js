import {getLogger} from '../core/utils';
import {apiUrl, headers} from '../core/api';

const log = getLogger('auth/service');
const AUTH_STARTED = 'auth/started';
const AUTH_SUCCEEDED = 'auth/succeeded';
const AUTH_FAILED = 'auth/failed';
const action = (type, payload) => ({type, payload});

export const login = (user) => (dispatch, getState) => {
    if (getState().inprogress) {
        log(`login already in progress`);
        return;
    }
    log(`starting login`);
    dispatch(action(AUTH_STARTED));
    let ok = false;
    log(`starting fetch ${apiUrl}/token-auth ${JSON.stringify(user)}`);
    return fetch(`${apiUrl}/token-auth`, {method: 'POST', headers, body: JSON.stringify(user)})
        .then(res => {
            ok = res.ok;
            log(`login res ok = ${ok} res: ${JSON.stringify(res)}`);
            return res.json();
        })
        .then(json => {
            log(`login json = ${json}`);
            dispatch(action(ok? AUTH_SUCCEEDED: AUTH_FAILED, json));
        })
        .catch(err => {
            log(`login err = ${err.message}`);
            dispatch(action(AUTH_FAILED, {issue: [{error: err.message}]}));
        });
};

export const authReducer = (state = {token: null, inprogress: false}, action) => {
    log(`${action.type} ${JSON.stringify(state)}`)
    switch (action.type) {
        case AUTH_STARTED:
            return {token: null, inprogress: true};
        case AUTH_SUCCEEDED:
            return {token: action.payload.token, inprogress: false};
        case AUTH_FAILED:
            return {issue: action.payload.issue, inprogress: false};
        default:
            return state;
    }
};