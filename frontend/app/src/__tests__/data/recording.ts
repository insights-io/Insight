export const BOOTSTRAP_SCRIPT = `((s, e, t) => {
  s._i_debug = !1;
  s._i_host = 'rebrowse.dev';
  s._i_org = '000000';
  s._i_ns = 'IS';
  const n = e.createElement(t);
  n.async = true;
  n.crossOrigin = 'anonymous';
  n.src = 'https://static.rebrowse.dev/s/localhost.rebrowse.js';
  const i = e.getElementsByTagName(t)[0];
  i.parentNode.insertBefore(n, i);
})(window, document, 'script');`;
