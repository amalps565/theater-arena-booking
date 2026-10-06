'use strict';

const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const PROJECT = path.resolve(__dirname, '..', '..', '..');
const FRONTEND = path.join(PROJECT, 'frontend');
const HOOK = path.join(__dirname, '..', 'react-lint.cjs');

let passed = 0,
  failed = 0,
  skipped = 0;
function ok(cond, msg) {
  if (cond) passed++;
  else {
    failed++;
    console.error(`  FAIL: ${msg}`);
  }
}
function skip(msg) {
  skipped++;
  console.log(`  SKIP: ${msg}`);
}

function eslintInstalled() {
  const dir = path.join(FRONTEND, 'node_modules', '.bin');
  return ['eslint', 'eslint.cmd', 'eslint.CMD', 'eslint.ps1'].some((b) => fs.existsSync(path.join(dir, b)));
}

function edit(relPath, content) {
  const filePath = relPath ? path.join(PROJECT, relPath) : '';
  const payload = { tool_name: 'Edit', tool_input: { file_path: filePath, new_string: content || '' } };
  const env = { ...process.env, CLAUDE_PROJECT_DIR: PROJECT };
  return spawnSync(process.execPath, [HOOK], { input: JSON.stringify(payload), encoding: 'utf8', cwd: PROJECT, env })
    .status;
}

function withFixture(relPath, content, fn) {
  const abs = path.join(PROJECT, relPath);
  fs.mkdirSync(path.dirname(abs), { recursive: true });
  fs.writeFileSync(abs, content);
  try {
    fn();
  } finally {
    fs.rmSync(abs, { force: true });
  }
}

ok(edit('', '') === 0, 'no file path is allowed');
ok(edit('README.md', '# theater-arena-booking') === 0, 'root README is allowed');
ok(edit('frontend/vite.config.ts', 'export default {}') === 0, 'frontend file outside src is allowed');
ok(edit('backend/src/main/java/A.java', 'class A {}') === 0, 'a backend file is allowed');
ok(edit('src/App.tsx', 'export const A = 1;') === 0, 'a src file outside frontend is allowed');

if (eslintInstalled()) {
  const clean = 'export const sum = (a: number, b: number): number => a + b;\n';
  withFixture('frontend/src/_hook_lint_fixture_clean.ts', clean, () => {
    ok(edit('frontend/src/_hook_lint_fixture_clean.ts', clean) === 0, 'clean frontend src file is allowed');
  });
  const bad = 'export const x = (): null => { const unused = 1; return null; };\n';
  withFixture('frontend/src/_hook_lint_fixture_bad.ts', bad, () => {
    ok(edit('frontend/src/_hook_lint_fixture_bad.ts', bad) === 2, 'frontend src file with unused var is surfaced');
  });
} else {
  ok(edit('frontend/src/_fixture.tsx', 'export const X = () => null;') === 0, 'src file allowed when eslint not installed');
  skip('frontend/node_modules/.bin/eslint absent - eslint lint cases skipped');
}

console.log(`\nreact-lint: ${passed} passed, ${failed} failed, ${skipped} skipped`);
process.exit(failed === 0 ? 0 : 1);
