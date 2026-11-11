// [Doc] Comprueba la presentación de estados y acciones de pedidos.
// [Doc] Referencia de instrucciones: docs/documentacion/REFERENCIA_COMPLETA.html.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

test('la tabla administrativa representa ENVIADO sin convertirlo en PENDIENTE', () => {
  const template = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/paneladministracion.html'), 'utf8');
  const inicio = template.indexOf('function renderBadgeEstadoPedido(estado)');
  const fin = template.indexOf('function formatearFechaPedido', inicio);
  assert.ok(inicio >= 0 && fin > inicio, 'No se encontro el renderizador de estado');
  const html = vm.runInNewContext(template.slice(inicio, fin) + ';renderBadgeEstadoPedido("ENVIADO");');
  assert.match(html, /ENVIADO/, 'El pedido ENVIADO se presenta con otro estado');
  assert.doesNotMatch(html, /PENDIENTE/);
});

test('el aviso de ENVIADO indica que el pedido está en camino', () => {
  const template = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/paneladministracion.html'), 'utf8');
  const inicio = template.indexOf('function actualizarAvisoEstadoPedido(estado)');
  const fin = template.indexOf('async function ', inicio + 10);
  const aviso = { className: '', innerHTML: '' };
  vm.runInNewContext(template.slice(inicio, fin) + ';actualizarAvisoEstadoPedido("ENVIADO");', {
    document: { getElementById: () => aviso }
  });
  assert.match(aviso.innerHTML, /enviado/);
  assert.match(aviso.innerHTML, /en camino/);
});
