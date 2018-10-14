const Koa = require('koa');
const app = new Koa();
const server = require('http').createServer(app.callback());
const WebSocket = require('ws');
const wss = new WebSocket.Server({server});
const Router = require('koa-router');
const cors = require('koa-cors');
const bodyParser = require('koa-bodyparser');
const convert = require('koa-convert');

app.use(bodyParser());

app.use(cors());

app.use(convert(function *(next) {
  const start = new Date();
  yield Promise.all(next);
  const ms = new Date() - start;
  console.log(`${start} ${this.method} ${this.url} - ${ms}ms`);
}));


const books = [{id: 1, title: 'Test Book 1', date: new Date()},
    {id: 2, title: 'Test Book 2', date: new Date()}];
let lastUpdated = books[0].date;

const router = new Router();
router.get('/books', ctx => {
  const ifModifiedSince = ctx.request.get('If-Modified-Since');
  if (ifModifiedSince && new Date(ifModifiedSince).getTime() < lastUpdated.getTime() - lastUpdated.getMilliseconds()) {
    ctx.response.status = 304;
  } else {
    ctx.response.set('Last-Modified', lastUpdated);
    ctx.response.body = books;
    ctx.response.status = 200;
  }
});

const broadcast = (data) =>
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data));
    }
  });

router.post('/book', ctx => {
  const book = ctx.request.body;
  let issue;
  if (!book.title) {
    issue = {title: 'Title is missing'};
  }
  if (issue) {
    ctx.response.body = issue;
    ctx.response.status = 400;
  } else {
    if (!book.date) {
      book.date = new Date();
    }
    lastUpdated = book.date;
    book.id = books.length + 1;
    books.push(book);
    ctx.response.body = book;
    ctx.response.status = 201;
    broadcast(book);
  }
});

app.use(router.routes());
app.use(router.allowedMethods());

server.listen(3000);