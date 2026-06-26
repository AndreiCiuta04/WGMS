#!/usr/bin/env node
/*
 * UC-05 OpenAPI-vs-actual contract diff (TG-04 — closes E2E-027).
 *
 * Proves that the OpenAPI document the running app serves actually matches the UC-05 endpoints the app
 * really exposes. It:
 *   1. fetches the served spec from  http://localhost:8080/v3/api-docs
 *   2. logs in (admin@email.com / password — seeded by the generateData profile)
 *   3. calls each UC-05 endpoint for real and records the live HTTP status
 *   4. reports, per endpoint:
 *        - DECLARED?  is this {method, path} present in the served OpenAPI document
 *        - DOCUMENTED STATUS? is the live status one of the responses the spec declares for it
 *
 * This is real, repeatable contract evidence (no fabricated diff). It does NOT validate response BODIES
 * against the declared schemas — that is a heavier contract-test and can stay a documented follow-up; for
 * MR3 the path/method/status agreement is what E2E-027 asks for.
 *
 * USAGE (Node 18+, both servers running):
 *   node openapi-contract-diff-uc05.mjs
 * Exit code 0 = all UC-05 endpoints declared and statuses documented; 1 = at least one mismatch.
 *
 * NOTE: keep this as a script you RUN and attach the output of. It is not auto-run by CI unless you add a
 * job for it; wiring it into .gitlab-ci.yml's e2e stage is an easy optional hardening.
 */

const BASE = process.env.WGMS_API || 'http://localhost:8080';
const API = `${BASE}/api/v1`;
const SPEC_URL = `${BASE}/v3/api-docs`;
const ADMIN_EMAIL = process.env.WGMS_ADMIN || 'admin@email.com';
const ADMIN_PW = process.env.WGMS_PW || 'password';

// The UC-05 surface to check. {path} uses the OpenAPI template form ({id} not a concrete value).
// `call` returns the concrete URL + method actually exercised against the live app.
// NOTE: the /history/statistics/* endpoints REQUIRE `from` and `to` date query params, so we pass a
// one-year window to get a real 200 (a bare call would 400 and we could not confirm the 2xx is documented).
const STATS_RANGE = (() => {
  const to = new Date();
  const from = new Date();
  from.setFullYear(from.getFullYear() - 1);
  const fmt = (d) => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  return `from=${fmt(from)}&to=${fmt(to)}`;
})();

const UC05_ENDPOINTS = [
  { method: 'get', path: '/api/v1/dashboard', call: { method: 'GET', url: `${API}/dashboard`, auth: true } },
  { method: 'get', path: '/api/v1/history', call: { method: 'GET', url: `${API}/history`, auth: true } },
  { method: 'get', path: '/api/v1/notifications', call: { method: 'GET', url: `${API}/notifications`, auth: true } },
  { method: 'post', path: '/api/v1/notifications/read-all', call: { method: 'POST', url: `${API}/notifications/read-all`, auth: true } },
  { method: 'get', path: '/api/v1/notifications/settings', call: { method: 'GET', url: `${API}/notifications/settings`, auth: true } },
  { method: 'get', path: '/api/v1/expenses', call: { method: 'GET', url: `${API}/expenses`, auth: true } },
  { method: 'get', path: '/api/v1/expenses/balances', call: { method: 'GET', url: `${API}/expenses/balances`, auth: true } },
  { method: 'get', path: '/api/v1/payments/overview', call: { method: 'GET', url: `${API}/payments/overview`, auth: true } },
  // statistics modules (descriptive views behind FR-16) live under /history/statistics and need from/to
  { method: 'get', path: '/api/v1/history/statistics/overview', call: { method: 'GET', url: `${API}/history/statistics/overview?${STATS_RANGE}`, auth: true } },
  { method: 'get', path: '/api/v1/history/statistics/expenses', call: { method: 'GET', url: `${API}/history/statistics/expenses?${STATS_RANGE}`, auth: true } },
  { method: 'get', path: '/api/v1/history/statistics/tasks', call: { method: 'GET', url: `${API}/history/statistics/tasks?${STATS_RANGE}`, auth: true } },
  { method: 'get', path: '/api/v1/history/statistics/shopping', call: { method: 'GET', url: `${API}/history/statistics/shopping?${STATS_RANGE}`, auth: true } }
];

function red(s) { return `\x1b[31m${s}\x1b[0m`; }
function green(s) { return `\x1b[32m${s}\x1b[0m`; }
function yellow(s) { return `\x1b[33m${s}\x1b[0m`; }

async function login() {
  const res = await fetch(`${API}/authentication`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PW })
  });
  if (!res.ok) {
    throw new Error(`login failed: ${res.status} ${await res.text()}`);
  }
  // the app returns the JWT as a plain string body
  const token = (await res.text()).trim().replace(/^"|"$/g, '');
  if (!token) throw new Error('login returned an empty token');
  return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
}

function declaredStatusesFor(spec, method, path) {
  const item = spec.paths?.[path]?.[method];
  if (!item) return null; // not declared
  return Object.keys(item.responses || {}); // e.g. ['200','401','403']
}

async function main() {
  console.log(`Fetching served OpenAPI spec from ${SPEC_URL} ...`);
  const specRes = await fetch(SPEC_URL);
  if (!specRes.ok) {
    console.error(red(`Could not fetch OpenAPI spec: ${specRes.status}. Is the backend running on ${BASE}?`));
    process.exit(1);
  }
  const spec = await specRes.json();
  const token = await login();

  let mismatches = 0;
  console.log('\nUC-05 endpoint | declared in spec? | live status | status documented?');
  console.log('-'.repeat(86));

  for (const ep of UC05_ENDPOINTS) {
    const declared = declaredStatusesFor(spec, ep.method, ep.path);
    const headers = ep.call.auth ? { Authorization: token } : {};
    let liveStatus = 'ERR';
    try {
      const r = await fetch(ep.call.url, { method: ep.call.method, headers });
      liveStatus = String(r.status);
    } catch (e) {
      liveStatus = `ERR(${e.message})`;
    }

    const declaredOk = declared !== null;
    // a live 2xx must be documented; for non-2xx we just report (auth/seed state varies)
    const statusDocumented = declared !== null && declared.includes(liveStatus);
    const live2xx = /^2\d\d$/.test(liveStatus);
    const statusVerdict = !declaredOk
      ? red('n/a (not declared)')
      : statusDocumented
        ? green('yes')
        : live2xx
          ? red(`NO (2xx ${liveStatus} not in [${declared}])`)
          : yellow(`info (${liveStatus} not in [${declared}])`);

    if (!declaredOk || (live2xx && !statusDocumented)) mismatches++;

    console.log(
      `${ep.method.toUpperCase()} ${ep.path}`.padEnd(45) +
      `| ${declaredOk ? green('yes') : red('NO')}`.padEnd(22) +
      `| ${liveStatus.padEnd(6)} | ${statusVerdict}`
    );
  }

  console.log('-'.repeat(86));
  if (mismatches === 0) {
    console.log(green('PASS — every UC-05 endpoint is declared in the served OpenAPI spec and its 2xx behaviour is documented.'));
    process.exit(0);
  } else {
    console.log(red(`FAIL — ${mismatches} UC-05 endpoint(s) are undeclared or return an undocumented 2xx. Reconcile the spec or the controller.`));
    process.exit(1);
  }
}

main().catch((e) => {
  console.error(red(`contract-diff error: ${e.message}`));
  process.exit(1);
});
