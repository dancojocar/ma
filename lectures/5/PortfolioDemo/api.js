export const apiUrl = 'http://192.168.43.137:8080';
export const headers = {'Accept': 'application/json', 'Content-Type': 'application/json'};
export const authHeaders = (token) => ({...headers, 'Authorization': `Bearer ${token}`});
