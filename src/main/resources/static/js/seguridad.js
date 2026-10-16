(() => {
  window.peqEscapeHtml = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#039;'}[c]));
  const token = document.querySelector('meta[name="csrf-token"]')?.content;
  if (!token) return;
  const originalFetch = window.fetch.bind(window);
  window.fetch = (input, init = {}) => {
    const url = new URL(input instanceof Request ? input.url : input, location.href);
    const method = (init.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
    if (url.origin === location.origin && !['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const headers = new Headers(init.headers || (input instanceof Request ? input.headers : undefined));
      headers.set('X-CSRF-TOKEN', token);
      init = { ...init, headers };
    }
    return originalFetch(input, init);
  };
  const proteger = form => {
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
