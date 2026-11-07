// Simulador de vecinos (ADR-013). Sirve, para cada operación de contracts/neighbors/*.openapi.yaml,
// la primera respuesta 2xx con su ejemplo (o la respuesta indicada con ?simulate=<código HTTP>).
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { parse } from 'yaml';

const DIR = process.env.CONTRACTS_DIR || path.resolve('../../contracts/neighbors');
const PORT = Number(process.env.PORT || 8090);

function loadRoutes() {
  const routes = [];
  if (!fs.existsSync(DIR)) return routes;
  for (const f of fs.readdirSync(DIR).filter((n) => /\.ya?ml$/.test(n))) {
    const doc = parse(fs.readFileSync(path.join(DIR, f), 'utf8'));
    for (const [p, ops] of Object.entries(doc.paths || {})) {
      for (const [method, op] of Object.entries(ops)) {
        if (!['get', 'post', 'put', 'patch', 'delete'].includes(method)) continue;
        const regex = new RegExp('^' + p.replace(/\{[^}]+\}/g, '[^/]+') + '$');
        routes.push({ file: f, method: method.toUpperCase(), path: p, regex, responses: op.responses || {} });
      }
    }
  }
  return routes;
}

function pickExample(resp) {
  const content = resp?.content || {};
  const [ctype, media] = Object.entries(content)[0] || [];
  if (!media) return { ctype: 'application/json', body: {} };
  if (media.example !== undefined) return { ctype, body: media.example };
  const ex = media.examples && Object.values(media.examples)[0];
  if (ex && ex.value !== undefined) return { ctype, body: ex.value };
  return { ctype, body: {} };
}

const routes = loadRoutes();
const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://localhost');
  if (url.pathname === '/__routes') {
    res.writeHead(200, { 'content-type': 'application/json' });
    return res.end(JSON.stringify(routes.map((r) => ({ method: r.method, path: r.path, file: r.file })), null, 2));
  }
  if (url.pathname === '/__health') {
    res.writeHead(200, { 'content-type': 'text/plain' });
    return res.end('ok');
  }
  const route = routes.find((r) => r.method === req.method && r.regex.test(url.pathname));
  if (!route) {
    res.writeHead(404, { 'content-type': 'application/problem+json' });
    return res.end(JSON.stringify({ title: 'Operación no definida en los contratos de vecinos', status: 404, code: 'SIM-0404' }));
  }
  const wanted = url.searchParams.get('simulate');
  const code = wanted && route.responses[wanted] ? wanted : Object.keys(route.responses).find((c) => c.startsWith('2')) || '200';
  const { ctype, body } = pickExample(route.responses[code]);
  res.writeHead(Number(code), { 'content-type': ctype, 'x-simulated-by': 'kit-simulator' });
  res.end(JSON.stringify(body));
});
server.listen(PORT, '0.0.0.0', () => console.log(`Simulador de vecinos en :${PORT} (${routes.length} operaciones desde ${DIR})`));
