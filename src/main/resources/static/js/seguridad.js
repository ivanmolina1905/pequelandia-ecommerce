// [Doc] Agrega el token CSRF a formularios y peticiones del navegador.
// [Doc] Referencia de instrucciones: docs/documentacion/REFERENCIA_COMPLETA.html.
(() => {
  window.peqEscapeHtml = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#039;'}[c]));
  const inicial = Number(document.querySelector('meta[name="session-expires"]')?.content);
  const login = document.querySelector('meta[name="login-url"]')?.content || '/auth/login?sesionVencida=true';
  const clave = 'peq-session-expires';
  let vence = 0;
  let temporizador;
  let comprobacion;
  let ultimaActividadEnviada = -Infinity;
  const sessionUrl = new URL('session', new URL(login, location.href));
  const programar = valor => {
    if (!Number.isFinite(valor) || valor <= 0) return;
    vence = Math.max(vence, valor);
    window.clearTimeout(temporizador);
    temporizador = window.setTimeout(comprobar, Math.max(0, vence - Date.now()));
  };
  const confirmar = () => {
    if (comprobacion) return comprobacion;
    ultimaActividadEnviada = Date.now();
    comprobacion = window.fetch(sessionUrl.href, { cache: 'no-store', credentials: 'same-origin' })
      .then(response => {
        if (response.status !== 401 && !Number(response.headers?.get('X-Session-Expires'))) {
          window.clearTimeout(temporizador);
          temporizador = window.setTimeout(comprobar, 60000);
        }
      })
      .catch(() => {
        // Una interrupción de red no demuestra que haya vencido la sesión.
        window.clearTimeout(temporizador);
        temporizador = window.setTimeout(comprobar, 60000);
      }).finally(() => { comprobacion = undefined; });
    return comprobacion;
  };
  const comprobar = () => {
    if (vence && Date.now() >= vence) return confirmar();
  };
  const renovar = valor => {
    programar(valor);
    try { window.localStorage.setItem(clave, String(valor)); } catch (_) { /* Almacenamiento opcional. */ }
  };
  if (Number.isFinite(inicial) && inicial > 0) {
    renovar(inicial);
    window.addEventListener('storage', event => {
      if (event.key === clave) programar(Number(event.newValue));
    });
    window.addEventListener('pageshow', comprobar);
    document.addEventListener('visibilitychange', comprobar);
    const actividad = () => {
      if (Date.now() - ultimaActividadEnviada >= 60000) confirmar();
    };
    ['pointerdown', 'keydown', 'input', 'scroll'].forEach(nombre =>
      document.addEventListener(nombre, actividad, { passive: true }));
  }
  const token = document.querySelector('meta[name="csrf-token"]')?.content;
  const originalFetch = window.fetch.bind(window);
  window.fetch = (input, init = {}) => {
    const url = new URL(input instanceof Request ? input.url : input, location.href);
    const method = (init.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
    if (token && url.origin === location.origin && !['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const headers = new Headers(init.headers || (input instanceof Request ? input.headers : undefined));
      headers.set('X-CSRF-TOKEN', token);
      init = { ...init, headers };
    }
    return originalFetch(input, init).then(response => {
      if (url.origin === location.origin) {
        const proximo = Number(response.headers?.get('X-Session-Expires'));
        if (proximo > 0) renovar(proximo);
        if (response.status === 401) location.replace(login);
      }
      return response;
    });
  };
  const proteger = form => {
    if (!token) return;
    if (form.method.toLowerCase() === 'get' || new URL(form.action, location.href).origin !== location.origin) return;
    let field = form.querySelector('input[name="_csrf"]');
    if (!field) {
      field = document.createElement('input');
      field.type = 'hidden'; field.name = '_csrf'; form.appendChild(field);
    }
    field.value = token;
  };
  document.addEventListener('DOMContentLoaded', () => document.querySelectorAll('form').forEach(proteger));
  document.addEventListener('submit', event => { if (event.target instanceof HTMLFormElement) proteger(event.target); }, true);
})();
