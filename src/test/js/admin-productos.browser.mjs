// Prueba de la plantilla administrativa sin modificar datos de la tienda.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const source = await readFile('src/main/resources/templates/paneladministracion.html', 'utf8');
const navigation = [...source.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/g)]
  .map(match => match[1]).find(script => script.includes('window.showSection = showSection'));
const browser = await chromium.launch({ headless: true,
  ...(process.env.CHROME_PATH ? { executablePath: process.env.CHROME_PATH } : {}) });
try {
  for (const viewport of [{ width: 1440, height: 900 }, { width: 390, height: 844 }]) {
    const page = await browser.newPage({ viewport });
    await page.route('**/*', route => route.fulfill({ body: '', contentType: 'text/html' }));
    await page.goto('http://pequelandia.test/admin/productos');
    await page.setContent(source.replace(/<script\b[^>]*>[\s\S]*?<\/script>/g, ''));
    await page.addStyleTag({ path: 'src/main/resources/static/bootstrap-5.3.5-dist/css/bootstrap.min.css' });
    await page.addStyleTag({ path: 'src/main/resources/static/css/paneles.css' });
    await page.addScriptTag({ path: 'src/main/resources/static/bootstrap-5.3.5-dist/js/bootstrap.bundle.min.js' });
    await page.addScriptTag({ content: navigation });
    await page.evaluate(() => window.showSection('productos'));
    await page.waitForTimeout(350);
    const button = page.locator('#fabProductos');
    assert.equal(await button.evaluate(el => el.closest('main')), null);
    assert.equal(await page.locator('#agregarProducto').count(), 0);
    for (const scroll of [0, 500]) {
      await page.evaluate(y => window.scrollTo(0, y), scroll);
      const box = await button.boundingBox();
      assert.ok(box && Math.abs(viewport.height - box.y - box.height - 24) < 2,
        'El botón debe permanecer a 24px del borde inferior');
    }
    await button.click();
    await page.waitForSelector('#newProductModal.show');
    assert.equal(await page.locator('#newProductForm input[name="nombre_producto"]').isVisible(), true);
    await page.locator('#newProductModal [data-bs-dismiss="modal"]').first().click();
    await page.waitForSelector('#newProductModal.show', { state: 'hidden' });
    await page.evaluate(() => window.showSection('usuarios'));
    assert.equal(await button.isVisible(), false);
    await page.evaluate(() => window.showSection('productos'));
    assert.equal(await button.isVisible(), true);
    await page.close();
  }
  console.log('OK: flotante fijo, apertura del formulario y cambio de sección en escritorio y móvil.');
} finally { await browser.close(); }
