// Ejecutar después de ProductoControllerTest con -Dproducto.modal.fixtures=true.
// PLAYWRIGHT_MODULE permite indicar una instalación local de Playwright.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { resolve, extname } from 'node:path';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const browser = await chromium.launch({
  headless: true,
  ...(process.env.CHROME_PATH ? { executablePath: process.env.CHROME_PATH } : {})
});
try {
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
  const errores = [];
  page.on('pageerror', error => errores.push(error.message));
  let modo = 'detalle';
  let compras = 0;
  await page.route('http://pequelandia.test/**', async route => {
    const url = new URL(route.request().url());
    if (url.pathname === '/productos/api/agregar') {
      assert.equal(route.request().headers()['x-csrf-token'], 'token-prueba');
      assert.equal(new URLSearchParams(route.request().postData()).get('cantidad'), '2');
      compras++;
      return route.fulfill({ json: { ok: true, items: 2, total: 30000 } });
    }
    if (url.pathname === '/producto/7') {
      if (modo === 'lento') await new Promise(done => setTimeout(done, 300));
      if (modo === 'error') return route.fulfill({ status: 404, body: 'No encontrado' });
      return route.fulfill({ contentType: 'text/html', body: await readFile(
        `target/producto-modal-fixtures/${modo === 'sin-stock' ? 'sin-stock' : 'detalle'}.html`) });
    }
    if (url.pathname === '/productos') return route.fulfill({ contentType: 'text/html',
      body: await readFile('target/producto-modal-fixtures/catalogo.html') });
    const raiz = resolve('src/main/resources/static');
    const archivo = resolve(raiz, '.' + url.pathname);
    if (!archivo.startsWith(raiz + '\\') && !archivo.startsWith(raiz + '/')) return route.abort();
    try {
      const tipo = { '.css': 'text/css', '.js': 'text/javascript', '.png': 'image/png',
        '.jpg': 'image/jpeg', '.svg': 'image/svg+xml' }[extname(archivo)] || 'application/octet-stream';
      return route.fulfill({ contentType: tipo, body: await readFile(archivo) });
    } catch { return route.fulfill({ status: 404 }); }
  });

  await page.goto('http://pequelandia.test/productos?q=remera&ordenar=precio-asc');
  const direccion = page.url();
  await page.evaluate(() => document.addEventListener('click', event => {
    if (event.target.closest('.js-ver-producto')) window.posicionAlAbrir = scrollY;
  }, true));
  const modal = page.locator('#detalleProductoModal');
  const enlace = page.locator('.js-ver-producto').nth(8);
  const abrir = async () => {
    await enlace.scrollIntoViewIfNeeded();
    await enlace.locator('..').locator('..').hover();
    await enlace.hover();
    await enlace.click();
    await modal.waitFor({ state: 'visible' });
    return page.evaluate(() => window.posicionAlAbrir);
  };
  const cerrar = async (y, tecla = true) => {
    if (tecla) await page.keyboard.press('Escape');
    else await modal.getByRole('button', { name: 'Cerrar detalle del producto' }).click();
    await modal.waitFor({ state: 'hidden' });
    await page.waitForFunction(() => document.body.style.position !== 'fixed');
    assert.equal(page.url(), direccion);
    assert.equal(await page.evaluate(() => scrollY), y);
    assert.equal(await enlace.evaluate(el => el === document.activeElement), true);
  };

  let y = await abrir();
  await modal.getByRole('heading', { name: 'Remera <infantil>' }).waitFor();
  let caja = await modal.boundingBox();
  assert.ok(Math.abs(caja.x + caja.width / 2 - 720) < 10, 'Modal centrado horizontalmente');
  assert.ok(Math.abs(caja.y + caja.height / 2 - 500) < 2, 'Modal centrado verticalmente');
  await page.screenshot({ path: 'target/producto-modal-desktop.png' });
  await cerrar(y);

  y = await abrir();
  await modal.locator('[name=cantidad]').fill('2');
  await modal.getByRole('button', { name: 'Agregar al carrito' }).click();
  await modal.getByRole('status').filter({ hasText: 'Producto agregado al carrito.' }).waitFor();
  assert.equal(compras, 1);
  await cerrar(y, false);

  modo = 'sin-stock';
  y = await abrir();
  await modal.getByText('Sin stock', { exact: true }).waitFor();
  assert.equal(await modal.getByRole('button', { name: 'Agregar al carrito' }).count(), 0);
  await cerrar(y);

  modo = 'error';
  y = await abrir();
  await modal.getByText('No se pudo cargar el producto.', { exact: false }).waitFor();
  await cerrar(y, false);

  modo = 'lento';
  y = await abrir();
  await cerrar(y);
  modo = 'detalle';
  y = await abrir();
  await modal.getByRole('heading').waitFor();
  await cerrar(y);

  await page.setViewportSize({ width: 390, height: 844 });
  y = await abrir();
  await modal.getByRole('heading').waitFor();
  assert.equal(await modal.evaluate(el => el.scrollWidth > el.clientWidth), false);
  await modal.evaluate(el => { el.scrollTop = el.scrollHeight; });
  await page.screenshot({ path: 'target/producto-modal-mobile.png' });
  await cerrar(y, false);
  assert.deepEqual(errores, []);
  console.log('OK: centrado, Escape, X, posición, foco, URL, compra, sin stock, errores, carga cancelada y móvil.');
} finally {
  await browser.close();
}
