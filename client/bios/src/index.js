/* eslint-disable @typescript-eslint/camelcase */
/* eslint-disable no-underscore-dangle */
window._i_debug = !1;
window._i_host = 'insight.com';
window._i_script = 'cdn.insight.com/s/is.js';
window._i_org = '<ORG>';
window._i_ns = 'IS';

((doc, scriptTag) => {
  const scriptElement = doc.createElement(scriptTag);
  scriptElement.async = true;
  scriptElement.crossOrigin = 'anonymous';
  scriptElement.src = `https://${window._i_script}`;
  const firstScript = doc.getElementsByTagName(scriptTag)[0];
  firstScript.parentNode.insertBefore(scriptElement, firstScript);
})(document, 'script');
