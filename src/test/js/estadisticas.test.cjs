const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const template = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/paneladministracion.html'), 'utf8');
const inicio = template.indexOf('let graficoVentasObj = null;');
const fin = template.indexOf('function renderPedidosPagination', inicio);

function panel(tipo = 'dia', datos = [{ label: '2026-11-10', valor: 150 }]) {
  const events = {};
  const elements = {
    tipoGrafico: { value: tipo }, ventasDesde: { value: '2026-11-01' },
    ventasHasta: { value: '2026-11-10' }, btnAplicarGrafico: {}, btnExportGrafico: {},
    'estadisticas-contenido': { dataset: { inicial: 'true' } },
    graficoVentas: { getContext: () => ({ createLinearGradient: () => ({ addColorStop() {} }) }) },
    graficoVentasVacio: { style: {}, strong: {}, span: {}, querySelector(name) { return this[name]; } }
  };
  for (const el of Object.values(elements)) el.addEventListener = () => {};
  const state = { elements, requests: [], charts: [], sections: [] };
  const context = vm.createContext({
    document: { getElementById: id => elements[id], addEventListener: (name, fn) => { events[name] = fn; }, body: {} },
    fetch: async url => { state.requests.push(url); return { ok: true, json: async () => datos }; },
    Chart: function(ctx, config) { state.charts.push(config); this.destroy = () => {}; },
    getComputedStyle: () => ({ getPropertyValue: () => '#ad4b16' }),
    showSection: name => state.sections.push(name), console: { error() {} }
  });
  vm.runInContext(template.slice(inicio, fin), context);
  return { state, context, events };
}

test('al ingresar directamente a estadísticas se abre la sección', () => {
  const { events, state } = panel();
  events.DOMContentLoaded();
  assert.deepEqual(state.sections, ['estadisticas']);
});

for (const tipo of ['dia', 'mes', 'anio']) {
  test(`el gráfico ${tipo} usa etiquetas del servidor y exporta la misma consulta`, async () => {
    const { state, context } = panel(tipo);
    await vm.runInContext('cargarGraficoVentas()', context);
    assert.equal(state.charts[0].data.labels[0], '2026-11-10');
    assert.equal(state.charts[0].data.datasets[0].data[0], 150);
    assert.equal(state.elements.btnExportGrafico.href, state.requests[0].replace('/admin/estadisticas/serie-', '/admin/export/ventas-'));
  });
}

test('un rango invertido muestra el error sin consultar el servidor', async () => {
  const { state, context } = panel();
  state.elements.ventasDesde.value = '2026-12-01';
  await vm.runInContext('cargarGraficoVentas()', context);
  assert.equal(state.requests.length, 0);
  assert.match(state.elements.graficoVentasVacio.span.textContent, /rango de fechas válido/);
});

test('abrir estadísticas desde el menú inicializa fechas y carga el gráfico', () => {
  const start = template.indexOf('function showSection(name)');
  const end = template.indexOf('// Enlaces:', start);
  let cargas = 0;
  let fechas = 0;
  const estadisticas = { style: {} };
  vm.runInNewContext(template.slice(start, end) + ';showSection("estadisticas");', {
    document: { querySelectorAll: () => [] }, welcome: null, productos: null,
    usuarios: null, pedidos: null, estadisticas,
    configurarFechasIniciales: () => fechas++, cargarGraficoVentas: () => cargas++,
    marcarLinkActivo() {}, actualizarFab() {}
  });
  assert.equal(estadisticas.style.display, 'block');
  assert.equal(fechas, 1);
  assert.equal(cargas, 1);
});
