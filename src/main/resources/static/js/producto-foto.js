(() => {
  const form = document.getElementById('newProductForm');
  if (!form) return;
  const el = id => document.getElementById(id);
  const video = el('productCameraVideo');
  const status = el('productPhotoStatus');
  const picker = el('productPhotoFile');
  const url = form.elements.imagenUrl;
  let stream, photo, previewUrl, uploadedUrl, busy = false, generation = 0;
  function stop() {
    generation++;
    stream?.getTracks().forEach(track => track.stop());
    stream = null;
    video.srcObject = null;
    el('productCameraView').hidden = true;
  }
  function clear() {
    photo = null;
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = null;
    el('productPhotoImage').removeAttribute('src');
    el('productPhotoPreview').hidden = true;
    picker.value = '';
  }
  function preview(blob) {
    stop();
    clear();
    if (!blob || blob.size > 5 * 1024 * 1024) {
      status.textContent = 'La foto debe pesar hasta 5 MB.';
      return;
    }
    photo = blob;
    previewUrl = URL.createObjectURL(blob);
    el('productPhotoImage').src = previewUrl;
    el('productPhotoPreview').hidden = false;
    status.textContent = 'Revisá la foto: tocá el tachito para repetirla o el OK para subirla.';
  }
  async function open() {
    if (busy) return;
    if (uploadedUrl && url.value === uploadedUrl) url.value = '';
    uploadedUrl = null;
    stop();
    clear();
    status.textContent = '';
    if (!navigator.mediaDevices?.getUserMedia) {
      picker.click();
      return;
    }
    const current = generation;
    try {
      const camera = await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'environment' } }, audio: false });
      if (current !== generation) { camera.getTracks().forEach(track => track.stop()); return; }
      stream = camera;
      video.srcObject = stream;
      el('productCameraView').hidden = false;
      await video.play();
    } catch (_) {
      if (current !== generation) return;
      stop();
      status.textContent = 'No se pudo abrir la cámara. Podés usar la cámara del teléfono o elegir una foto.';
      picker.click();
    }
  }
  el('openProductCamera').addEventListener('click', open);
  el('discardProductPhoto').addEventListener('click', open);
  el('cancelProductCamera').addEventListener('click', stop);
  picker.addEventListener('change', () => { if (picker.files[0]) preview(picker.files[0]); });
  el('takeProductPhoto').addEventListener('click', () => {
    if (!video.videoWidth) return;
    const canvas = document.createElement('canvas');
    const scale = Math.min(1, 1600 / Math.max(video.videoWidth, video.videoHeight));
    canvas.width = Math.round(video.videoWidth * scale);
    canvas.height = Math.round(video.videoHeight * scale);
    canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
    const current = generation;
    canvas.toBlob(blob => { if (current === generation) preview(blob); }, 'image/jpeg', 0.88);
  });
  el('uploadProductPhoto').addEventListener('click', async () => {
    if (!photo || busy) return;
    busy = true;
    const buttons = [...form.querySelectorAll('button')];
    buttons.forEach(button => { button.disabled = true; });
    status.textContent = 'Subiendo foto…';
    const data = new FormData();
    data.append('foto', photo, 'producto.jpg');
    try {
      const token = document.querySelector('meta[name="csrf-token"]')?.content;
      const response = await fetch(el('productCamera').dataset.uploadUrl, {
        method: 'POST', body: data, headers: token ? { 'X-CSRF-TOKEN': token } : {}
      });
      const result = await response.json();
      if (!response.ok || !result.url) throw new Error(result.error || 'No se pudo subir la foto.');
      url.value = result.url;
      uploadedUrl = result.url;
      photo = null;
      el('uploadProductPhoto').hidden = true;
      status.textContent = 'Foto subida. Ya podés crear el producto.';
    } catch (error) {
      status.textContent = error.message || 'No se pudo subir la foto. Probá de nuevo.';
    } finally {
      busy = false;
      buttons.forEach(button => { button.disabled = false; });
    }
  });
  form.addEventListener('submit', event => {
    if (busy || photo) {
      event.preventDefault();
      status.textContent = busy ? 'Esperá a que termine la subida.' : 'Confirmá la foto con OK o descartala antes de crear el producto.';
    }
  });
  form.addEventListener('reset', () => {
    uploadedUrl = null;
    stop(); clear(); status.textContent = ''; el('uploadProductPhoto').hidden = false;
  });
  el('newProductModal').addEventListener('hide.bs.modal', event => {
    if (busy) event.preventDefault();
    else { stop(); clear(); el('uploadProductPhoto').hidden = false; }
  });
  el('openProductCamera').addEventListener('click', () => { el('uploadProductPhoto').hidden = false; });
  el('discardProductPhoto').addEventListener('click', () => { el('uploadProductPhoto').hidden = false; });
  window.addEventListener('pagehide', stop);
})();
