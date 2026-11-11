(() => {
  const modal = document.getElementById('detalleProductoModal');
  if (!modal) return;
  const contenido = modal.querySelector('.peq-detalle-contenido');
  const cerrar = modal.querySelector('.peq-detalle-cerrar');
  let origen;
  let posicion;
  let estiloBody;
  let carga;

  const mensaje = texto => {
    const p = document.createElement('p');
    p.id = 'detalleProductoTitulo';
    p.textContent = texto;
    contenido.replaceChildren(p);
  };

  document.addEventListener('click', async event => {
    const enlace = event.target.closest('.js-ver-producto');
    if (!enlace || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey || event.button !== 0) return;
    event.preventDefault();
    origen = enlace;
    posicion = { left: window.scrollX, top: window.scrollY };
    estiloBody = document.body.style.cssText;
    const ancho = document.body.getBoundingClientRect().width;
    Object.assign(document.body.style, {
      position: 'fixed', top: `-${posicion.top}px`, left: `-${posicion.left}px`, width: `${ancho}px`
    });
    carga?.abort();
    const peticion = new AbortController();
    carga = peticion;
    mensaje('Cargando producto…');
    modal.showModal();
    try {
      const respuesta = await fetch(enlace.href, { signal: peticion.signal });
      if (!respuesta.ok) throw new Error('detalle');
      const documento = new DOMParser().parseFromString(await respuesta.text(), 'text/html');
      const producto = documento.querySelector('article.producto');
      if (!producto) throw new Error('detalle');
      if (peticion.signal.aborted || !modal.open) return;
      producto.querySelector('h1').id = 'detalleProductoTitulo';
      const form = producto.querySelector('form');
      if (form) {
        const estado = document.createElement('p');
        estado.className = 'peq-detalle-estado';
        estado.setAttribute('role', 'status');
        form.append(estado);
        form.addEventListener('submit', async evento => {
          evento.preventDefault();
          const boton = form.querySelector('button[type="submit"]');
          boton.disabled = true;
          estado.textContent = 'Agregando al carrito…';
          try {
            const url = new URL(form.getAttribute('action'), location.href);
            url.pathname = url.pathname.replace(/\/agregar$/, '/api/agregar');
            const respuesta = await fetch(url.href, {
              method: 'POST', body: new URLSearchParams(new FormData(form))
            });
            if (!respuesta.ok) throw new Error('No se pudo agregar al carrito. Intentá nuevamente.');
            const datos = await respuesta.json();
            if (!datos.ok) throw new Error(datos.error || 'No se pudo agregar al carrito.');
            estado.textContent = 'Producto agregado al carrito.';
            const contador = document.querySelector('.js-cart-count');
            if (contador) contador.textContent = datos.items;
          } catch (error) {
            estado.textContent = error.message;
          } finally {
            boton.disabled = false;
          }
        });
      }
      contenido.replaceChildren(producto);
    } catch (error) {
      if (!peticion.signal.aborted) mensaje('No se pudo cargar el producto. Cerrá esta ventana e intentá nuevamente.');
    }
  });

  cerrar.addEventListener('click', () => modal.close());
  modal.addEventListener('close', () => {
    carga?.abort();
    contenido.replaceChildren();
    document.body.style.cssText = estiloBody;
    origen?.focus({ preventScroll: true });
    if (posicion) window.scrollTo({ ...posicion, behavior: 'instant' });
  });
})();
