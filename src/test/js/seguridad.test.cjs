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
