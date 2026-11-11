// Reloj compartido por los encabezados de Pequelandia.
(() => {
  const iniciar = () => {
    const relojes = document.querySelectorAll('#clock');
    if (!relojes.length) return;
    const actualizar = () => {
      const ahora = new Date();
      const hora = [ahora.getHours(), ahora.getMinutes(), ahora.getSeconds()]
        .map(valor => String(valor).padStart(2, '0')).join(':');
      relojes.forEach(reloj => { reloj.textContent = hora; });
    };
    actualizar();
    window.setInterval(actualizar, 1000);
  };
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', iniciar, { once: true });
  } else {
    iniciar();
  }
})();
