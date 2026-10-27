window.mostrarSeguimientoPedido = function (pedido, referencia) {
  const ancla = referencia.closest('table') || referencia;
  let seccion = ancla.nextElementSibling;
  if (!seccion || !seccion.classList.contains('seguimiento-pedido')) {
    seccion = document.createElement('section');
    seccion.className = 'seguimiento-pedido';
    ancla.after(seccion);
  }
  seccion.replaceChildren();
  const titulo = document.createElement('h3'); titulo.textContent = 'Seguimiento del pedido'; seccion.append(titulo);
  const nombres = {PENDIENTE:'Pendiente',PREPARANDO:'En preparación',ENVIADO:'Enviado',ENTREGADO:'Entregado',CANCELACION_SOLICITADA:'Cancelación solicitada',CANCELADO:'Cancelado'};
  const eventos = pedido.historial || [];
  if (!eventos.length) {
    const texto = document.createElement('p');
    texto.textContent = 'Estado actual: ' + (nombres[pedido.estado] || pedido.estado) + '. Los cambios anteriores no tienen historial registrado.';
    seccion.append(texto); return;
  }
  const lista = document.createElement('ol');
  eventos.forEach((evento, indice) => {
    const item = document.createElement('li');
    const estado = document.createElement('strong'); estado.textContent = nombres[evento.estado] || evento.estado;
    const fecha = document.createElement('span');
    fecha.textContent = evento.fecha ? new Date(evento.fecha).toLocaleString('es-AR', {timeZone:'America/Argentina/Buenos_Aires', hour12:false}) : 'Estado previo; fecha no registrada';
    if (indice === eventos.length - 1) { item.className = 'actual'; item.setAttribute('aria-current', 'step'); }
    item.append(estado, fecha); lista.append(item);
  });
  seccion.append(lista);
};
