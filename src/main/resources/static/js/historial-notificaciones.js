document.addEventListener('DOMContentLoaded', () => {
  const dialogo = document.getElementById('detalle-pedido');
  const resultado = document.getElementById('resultado-pedido');
  const productos = document.getElementById('productos-pedido');
  const pdf = document.getElementById('pdf-pedido');
  let solicitud = 0;
  document.querySelectorAll('.ver-pedido').forEach(boton => boton.addEventListener('click', async () => {
    const actual = ++solicitud;
    resultado.textContent = 'Cargando pedido…'; productos.replaceChildren(); pdf.hidden = true;
    dialogo.showModal();
    try {
      const respuesta = await fetch('/pedido/' + encodeURIComponent(boton.dataset.pedido));
      if (!respuesta.ok) throw new Error('No se pudo abrir el pedido. Revisá tu sesión e intentá nuevamente.');
      const pedido = await respuesta.json();
      if (actual !== solicitud) return;
      document.getElementById('titulo-pedido').textContent = 'Pedido #' + pedido.id;
      resultado.textContent = pedido.estado + ' · Total: $ ' + Number(pedido.total).toLocaleString('es-AR');
      (pedido.items || []).forEach(item => {
        const fila = document.createElement('p');
        fila.textContent = (item.producto || item.nombre || 'Producto') + ' · ' + item.cantidad + ' unidades';
        productos.appendChild(fila);
      });
      pdf.href = '/pedido/' + encodeURIComponent(pedido.id) + '/pdf'; pdf.hidden = false;
    } catch (error) { if(actual === solicitud) resultado.textContent = error.message; }
  }));
  dialogo.addEventListener('close', () => { solicitud++; });
});
