#!/usr/bin/env node
'use strict';

const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const { readPayload, allow, surfaceToModel, extractFileEdit } = require('./_util.cjs');

const PROJECT = process.env.CLAUDE_PROJECT_DIR || path.resolve(__dirname, '..', '..');
const ROOT = path.join(PROJECT, 'frontend');
const LINTED = /^src\/.+\.(t|j)sx?$/;
const ESLINT_BINARIES = ['eslint', 'eslint.cmd', 'eslint.CMD', 'eslint.ps1'];
const OUTPUT_LIMIT = 2000;
const TIMEOUT_MS = 60000;

function relativeToRoot(filePath) {
  const rel = path.relative(ROOT, path.resolve(PROJECT, filePath)).replace(/\\/g, '/');
  return rel.startsWith('..') ? null : rel;
}

function localEslint() {
  const dir = path.join(ROOT, 'node_modules', '.bin');
  const found = ESLINT_BINARIES.find((b) => fs.existsSync(path.join(dir, b)));
  return found ? path.join(dir, found) : null;
}

function eslintCheck(eslint, rel) {
  try {
    execFileSync(eslint, ['--max-warnings=0', rel], {
      cwd: ROOT,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe'],
      timeout: TIMEOUT_MS,
      shell: process.platform === 'win32',
    });
    return null;
  } catch (err) {
    if (err.code === 'ENOENT' || err.killed) return null;
    return ((err.stdout || '') + (err.stderr || '')).trim() || `eslint failed on ${rel}`;
  }
}

(async () => {
  const payload = await readPayload();
  const { filePath } = extractFileEdit(payload);
  if (!filePath) return allow();

  const rel = relativeToRoot(filePath);
  if (!rel || !LINTED.test(rel)) return allow();

  const eslint = localEslint();
  if (!eslint) return allow();

  const finding = eslintCheck(eslint, rel);
  if (!finding) return allow();

  return surfaceToModel(
    `ESLint flagged frontend/${rel} - fix before moving on, because the gate commands run eslint with --max-warnings=0:\n${finding.slice(
      0,
      OUTPUT_LIMIT,
    )}`,
  );
})();
