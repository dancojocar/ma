var koa = require('koa');
var app = module.exports = new koa();
const server = require('http').createServer(app.callback());
const WebSocket = require('ws');
const wss = new WebSocket.Server({server});
const Router = require('koa-router');
const cors = require('@koa/cors');
const bodyParser = require('koa-bodyparser');

app.use(bodyParser());

app.use(cors());

app.use(middleware);

function middleware(ctx, next) {
  const start = new Date();
  return next().then(() => {
    const ms = new Date() - start;
    console.log(`${start.toLocaleTimeString()} ${ctx.request.method} ${ctx.request.url} - ${ms}ms`);
  });
}

const movies = [{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BYWQ2NzQ1NjktMzNkNS00MGY1LTgwMmMtYTllYTI5YzNmMmE0XkEyXkFqcGdeQXVyMjM4NTM5NDY@._V1_SY139_CR1,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt2382320",
  "name": "No Time to Die",
  "desc": "James Bond has left active service. His peace is short-lived when Felix Leiter, an old friend from the CIA, turns up asking for help, leading Bond onto the trail of a mysterious villain armed with dangerous new technology."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BYTc3ZTAwYTgtMmM4ZS00MDRiLWI2Y2EtYmRiZmE0YjkzMGY1XkEyXkFqcGdeQXVyMDA4NzMyOA@@._V1_SY139_CR1,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt7097896",
  "name": "Venom: Let There Be Carnage",
  "desc": "Eddie Brock attempts to reignite his career by interviewing serial killer Cletus Kasady, who becomes the host of the symbiote Carnage and escapes prison after a failed execution."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BNzQ5NzJjMDgtNzhjMC00NTQ2LTgzOTUtZThiMWMwYmYwMGYxXkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_SY139_CR1,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt11125620",
  "name": "The Addams Family 2",
  "desc": "The Addams get tangled up in more wacky adventures and find themselves involved in hilarious run-ins with all sorts of unsuspecting characters. Sequel to the 2019 animated film, The Addams Family."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BNTliYjlkNDQtMjFlNS00NjgzLWFmMWEtYmM2Mzc2Zjg3ZjEyXkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_SY139_CR1,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt9376612",
  "name": "Shang-Chi and the Legend of the Ten Rings",
  "desc": "Shang-Chi, the master of weaponry-based Kung Fu, is forced to confront his past after being drawn into the Ten Rings organization."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BYmQzNmY3YzItOTE3OC00NGZjLTkwZDYtOWVmM2QyMzhiYTgwXkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_SY278_CR2,0,184,278_.jpg",
  "link": "https://www.imdb.com/title/tt8110232",
  "name": "The Many Saints of Newark",
  "desc": "Witness the making of Tony Soprano. The story that reveals the humanity behind Tony's struggles and the influence his family - especially his uncle, Dickie Moltisanti - had over him becoming the most iconic mob boss of all time."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BOTY2NzFjODctOWUzMC00MGZhLTlhNjMtM2Y2ODBiNGY1ZWRiXkEyXkFqcGdeQXVyMDM2NDM2MQ@@._V1_SY139_CR2,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt6264654",
  "name": "Free Guy",
  "desc": "A bank teller discovers that he's actually an NPC inside a brutal, open world video game."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BYzMwZTZhY2UtNWRjYy00MDc3LWE2MWUtZTg5NjVmZGY1NTdhXkEyXkFqcGdeQXVyMTEyMjM2NDc2._V1_SY139_CR1,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt9357050",
  "name": "Dear Evan Hansen",
  "desc": "Film adaptation of the Tony and Grammy Award-winning musical about Evan Hansen, a high school senior with Social Anxiety disorder and his journey of self-discovery and acceptance following the suicide of a fellow classmate."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BNzYzZTI2YmQtYmZlOS00NDI0LTg5MTktODJkNzc2Yzg0ZmMxXkEyXkFqcGdeQXVyNTQwOTY1MTg@._V1_SY139_CR2,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt9812474",
  "name": "Lamb",
  "desc": "A childless couple, María and Ingvar discover a mysterious newborn on their farm in Iceland. The unexpected prospect of family life brings them much joy, before ultimately destroying them."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BOWEzNDAxYmEtYWU0Zi00ZjZjLTkxY2QtMGY1MjY5ZjVhNDdjXkEyXkFqcGdeQXVyMDM2NDM2MQ@@._V1_SX92_CR0,0,92,139_.jpg",
  "link": "https://www.imdb.com/title/tt9347730",
  "name": "Candyman",
  "desc": "A sequel to the horror film Candyman (1992) that returns to the now-gentrified Chicago neighborhood where the legend began."
},
{
  "category": "Latest",
  "imageUrl": "https://m.media-amazon.com/images/M/MV5BNjRkYjlhMjEtYzIwOC00ZWYzLTgyMmQtYjI5M2UzNDJkNTU2XkEyXkFqcGdeQXVyMTkxNjUyNQ@@._V1_SY278_CR2,0,184,278_.jpg",
  "link": "https://www.imdb.com/title/tt5180504",
  "name": "The Witcher",
  "desc": "Geralt of Rivia, a solitary monster hunter, struggles to find his place in a world where people often prove more wicked than beasts."
}
];

const router = new Router();
router.get('/movies', ctx => {
    ctx.response.body = movies;
    ctx.response.status = 200;
});

const broadcast = (data) =>
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data));
    }
  });

router.post('/movie', ctx => {
  const movie = ctx.request.body;
  let issue;
  if (!movie.name) {
    issue = {name: 'Name is missing'};
  }
  if (issue) {
    ctx.response.body = issue;
    ctx.response.status = 400;
  } else {
    movie.id = movies.length + 1;
    movies.push(movie);
    ctx.response.body = movie;
    ctx.response.status = 201;
    broadcast(movie);
  }
});

app.use(router.routes());
app.use(router.allowedMethods());

server.listen(3000);