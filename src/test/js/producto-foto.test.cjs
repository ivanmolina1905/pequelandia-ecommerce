const { test } = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');

test('foto: vista previa, bloqueo sin confirmar, subida, repetir y liberar cámara', async () => {
  const elements = new Map();
  const get = id => {
    if (!elements.has(id)) elements.set(id, {
      hidden: false, value: '', files: [], dataset: { uploadUrl: '/admin/productos/foto' },
      listeners: {}, addEventListener(type, fn) { (this.listeners[type] ||= []).push(fn); },
      async emit(type, event = {}) { for (const fn of this.listeners[type] || []) await fn(event); },
      removeAttribute(name) { delete this[name]; }, querySelectorAll() { return []; },
      click() {}, async play() {}
    });
    return elements.get(id);
  };
  const form = get('newProductForm');
  form.elements = { imagenUrl: { value: 'https://example.com/original.jpg' } };
  let stopped = 0, uploads = 0;
  const context = {
    document: { getElementById: get, querySelector: () => ({ content: 'csrf' }) },
    navigator: { mediaDevices: { getUserMedia: async () => ({ getTracks: () => [{ stop() { stopped++; } }] }) } },
    window: { addEventListener() {} },
    URL: { createObjectURL: () => 'blob:preview', revokeObjectURL() {} },
    FormData: class { append() {} },
    fetch: async (_, options) => {
      uploads++;
      assert.equal(options.headers['X-CSRF-TOKEN'], 'csrf');
      return { ok: true, json: async () => ({ url: 'http://localhost/fotos-productos/test.png' }) };
    }
  };
  vm.runInNewContext(fs.readFileSync('src/main/resources/static/js/producto-foto.js', 'utf8'), context);
  get('productPhotoFile').files = [{ size: 100 }];
  await get('productPhotoFile').emit('change');
  assert.equal(get('productPhotoPreview').hidden, false);
  assert.equal(form.elements.imagenUrl.value, 'https://example.com/original.jpg');
  let prevented = false;
  await form.emit('submit', { preventDefault() { prevented = true; } });
  assert.equal(prevented, true);
  assert.equal(uploads, 0);
  await get('uploadProductPhoto').emit('click');
  assert.equal(uploads, 1);
  assert.equal(form.elements.imagenUrl.value, 'http://localhost/fotos-productos/test.png');
  await get('discardProductPhoto').emit('click');
  assert.equal(form.elements.imagenUrl.value, '');
  assert.equal(get('productPhotoPreview').hidden, true);
  assert.equal(get('productCameraView').hidden, false);
  await get('newProductModal').emit('hide.bs.modal');
  assert.equal(stopped, 1);
});
