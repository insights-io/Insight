export const BOOTSTRAP_SCRIPT = `((e, s, r) => {
  e._i_debug = !1;
  e._i_host = 'rebrowse.dev';
  e._i_org = '<ORG>';
  e._i_ns = 'IS';
  const t = s.createElement(r);
  t.async = true;
  t.crossOrigin = 'anonymous';
  t.src = 'https://static.rebrowse.dev/s/rebrowse.js';
  const o = s.getElementsByTagName(r)[0];
  o.parentNode.insertBefore(t, o);
})(window, document, 'script');`;
