const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function panel(desde, hasta) {
  const template = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/paneladministracion.html'), 'utf8');
  const inicio = template.indexOf('window.cargarPedidos = async function');
  const fin = template.indexOf('function actualizarResumenPedidos', inicio);
  assert.ok(inicio >= 0 && fin > inicio);
  const elementos = {
    pedidosDesde: { value: desde }, pedidosHasta: { value: hasta },
    filtroEstado: { value: 'PENDIENTE' }, pedidosUsuario: { value: ' cliente ' },
    pedidosSize: { value: '10' }, tbodyPedidos: { innerHTML: '' }
  };
  const urls = [];
  const contexto = {
    window: {}, URLSearchParams, console,
    document: { getElementById: id => elementos[id] },
    fetch: async url => {
      urls.push(url);
      return { ok: true, json: async () => ({ content: [], page: 0, totalPages: 0 }) };
    },
    actualizarResumenPedidos() {}, actualizarResumenEstadoPedidos() {},
    renderPedidosPagination() {}, actualizarContadorPedidos() {}
  };
  vm.runInNewContext(template.slice(inicio, fin), contexto);
  return { cargar: contexto.window.cargarPedidos, urls, elementos };
}

test('envía las fechas junto con los otros filtros y vuelve a la primera página', async () => {
  const p = panel('2026-11-01', '2026-11-02');
  await p.cargar(0);
  const params = new URL(p.urls[0], 'http://localhost').searchParams;
  assert.equal(params.get('desde'), '2026-11-01');
  assert.equal(params.get('hasta'), '2026-11-02');
  assert.equal(params.get('estado'), 'PENDIENTE');
  assert.equal(params.get('usuario'), 'cliente');
  assert.equal(params.get('page'), '0');
});

test('acepta una sola fecha y omite el límite vacío', async () => {
  for (const [desde, hasta] of [['2026-11-01', ''], ['', '2026-11-02']]) {
    const p = panel(desde, hasta);
    await p.cargar();
    const params = new URL(p.urls[0], 'http://localhost').searchParams;
    assert.equal(params.get('desde'), desde || null);
    assert.equal(params.get('hasta'), hasta || null);
  }
});

test('muestra un aviso y evita consultar un rango invertido', async () => {
  const p = panel('2026-11-03', '2026-11-01');
  await p.cargar();
  assert.equal(p.urls.length, 0);
  assert.match(p.elementos.tbodyPedidos.innerHTML, /Desde debe ser anterior o igual a Hasta/);
});
