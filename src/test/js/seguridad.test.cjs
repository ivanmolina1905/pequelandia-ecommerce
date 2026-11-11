// [Doc] Comprueba el agregado del token CSRF desde el navegador simulado.
// [Doc] Referencia de instrucciones: docs/documentacion/REFERENCIA_COMPLETA.html.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function navegador() {
  const llamadas = [];
  const document = {
    querySelector: () => ({ content: 'token-de-prueba' }),
    addEventListener: () => {}
  };
  const window = { fetch: async (input, init) => { llamadas.push({ input, init }); return { ok: true }; } };
  const contexto = vm.createContext({ window, document, location: { href: 'https://pequelandia.test/panel', origin: 'https://pequelandia.test' }, Request, Headers, URL });
  vm.runInContext(fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/seguridad.js'), 'utf8'), contexto);
  return { window, llamadas };
}

test('confirma el vencimiento con el servidor antes de volver al login', async () => {
  let ahora = 1000;
  let callback;
  let destino;
  let espera;
  let status = 200;
  let plazo = '9000';
  let sinRed = false;
  const window = {
    fetch: async () => {
      if (sinRed) throw new Error('Sin conexión');
      return { status, headers: new Headers({ 'X-Session-Expires': plazo }) };
    },
    setTimeout: (fn, ms) => { callback = fn; espera = ms; }, clearTimeout: () => {},
    addEventListener: () => {}, localStorage: { setItem: () => {} }
  };
  const document = {
    querySelector: selector => selector.includes('csrf-token') ? null : ({ content: selector.includes('session-expires') ? '5000' : '/auth/login?sesionVencida=true' }),
    addEventListener: () => {}
  };
  const location = { href: 'https://pequelandia.test/panel', origin: 'https://pequelandia.test', replace: url => { destino = url; } };
  vm.runInContext(fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/seguridad.js'), 'utf8'),
    vm.createContext({ window, document, location, Date: { now: () => ahora }, Request, Headers, URL }));
  assert.equal(espera, 4000);
  await window.fetch('/notificaciones/mias');
  assert.equal(espera, 8000);
  ahora = 5000;
  callback();
  assert.equal(destino, undefined);
  ahora = 9000;
  plazo = '17000';
  await callback();
  assert.equal(destino, undefined);
  assert.equal(espera, 8000);
  ahora = 17000;
  sinRed = true;
  await callback();
  assert.equal(destino, undefined);
  assert.equal(espera, 60000);
  sinRed = false;
  ahora += 60000;
  status = 401;
  plazo = '';
  await callback();
  assert.equal(destino, '/auth/login?sesionVencida=true');
});

test('renueva por actividad con limite de una peticion por minuto y no sale por un 403', async () => {
  let ahora = 1000;
  let llamadas = 0;
  let destino;
  const eventos = {};
  const window = {
    fetch: async () => { llamadas++; return { status: 403, headers: new Headers() }; },
    setTimeout: () => {}, clearTimeout: () => {}, addEventListener: () => {},
    localStorage: { setItem: () => {} }
  };
  const document = {
    querySelector: selector => ({ content: selector.includes('session-expires') ? '90000' : selector.includes('login-url') ? '/auth/login' : 'csrf' }),
    addEventListener: (nombre, fn) => { eventos[nombre] = fn; }
  };
  const location = { href: 'https://pequelandia.test/admin', origin: 'https://pequelandia.test', replace: url => { destino = url; } };
  vm.runInContext(fs.readFileSync(path.join(__dirname, '../../main/resources/static/js/seguridad.js'), 'utf8'),
    vm.createContext({ window, document, location, Date: { now: () => ahora }, Request, Headers, URL }));
  eventos.keydown();
  await new Promise(resolve => setImmediate(resolve));
  eventos.input();
  assert.equal(llamadas, 1);
  ahora += 60000;
  eventos.pointerdown();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(llamadas, 2);
  assert.equal(destino, undefined);
});

test('agrega CSRF al POST local conservando las cabeceras del formulario', async () => {
  const { window, llamadas } = navegador();
  await window.fetch('/pedido/7/cancelar', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' } });
  assert.equal(llamadas[0].init.headers.get('X-CSRF-TOKEN'), 'token-de-prueba');
  assert.equal(llamadas[0].init.headers.get('Content-Type'), 'application/x-www-form-urlencoded');
});

test('no envía el token a un sitio externo ni a lecturas GET', async () => {
  const { window, llamadas } = navegador();
  await window.fetch('https://externo.test/', { method: 'POST' });
  await window.fetch('/notificaciones/mias');
  assert.equal(new Headers(llamadas[0].init.headers).has('X-CSRF-TOKEN'), false);
  assert.equal(new Headers(llamadas[1].init.headers).has('X-CSRF-TOKEN'), false);
});

test('protege también un objeto Request y conserva su cabecera original', async () => {
  const { window, llamadas } = navegador();
  const request = new Request('https://pequelandia.test/notificaciones/1/leer', { method: 'POST', headers: { 'X-Prueba': 'valor' } });
  await window.fetch(request);
  assert.equal(llamadas[0].init.headers.get('X-CSRF-TOKEN'), 'token-de-prueba');
  assert.equal(llamadas[0].init.headers.get('X-Prueba'), 'valor');
  assert.equal(llamadas[0].input, request);
});

test('escapa nombres de productos antes de incorporarlos al HTML', () => {
  const { window } = navegador();
  assert.equal(window.peqEscapeHtml('<img src="x" onerror=alert(1)>'), '&lt;img src=&quot;x&quot; onerror=alert(1)&gt;');
});
