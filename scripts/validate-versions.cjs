#!/usr/bin/env node
'use strict';

const fs = require('fs');
const { spawnSync } = require('child_process');

const SEMVER = /^(\d+)\.(\d+)\.(\d+)$/;
const POM = 'backend/pom.xml';
const PACKAGE = 'frontend/package.json';
const CHANGELOG = 'CHANGELOG.md';

function pomVersion(text) {
  const match = /<artifactId>arena<\/artifactId>\s*<version>([^<]+)<\/version>/.exec(text);
  return match ? match[1].trim() : null;
}

function packageVersion(text) {
  return JSON.parse(text).version ?? null;
}

function changelogVersion(text) {
  const match = /^## \[([^\]]+)\]/m.exec(text);
  return match ? match[1] : null;
}

function readAt(ref, file) {
  if (!ref) {
    return fs.readFileSync(file, 'utf8');
  }
  const result = spawnSync('git', ['show', `${ref}:${file}`], { encoding: 'utf8' });
  if (result.status !== 0) {
    throw new Error(`cannot read ${file} at ${ref}: ${result.stderr.trim()}`);
  }
  return result.stdout;
}

function currentVersions() {
  return {
    [POM]: pomVersion(readAt(null, POM)),
    [PACKAGE]: packageVersion(readAt(null, PACKAGE)),
    [CHANGELOG]: changelogVersion(readAt(null, CHANGELOG)),
  };
}

function parts(version) {
  return SEMVER.exec(version).slice(1).map(Number);
}

function isSingleIncrement(base, next) {
  const from = parts(base);
  const to = parts(next);
  const changed = to.findIndex((n, i) => n !== from[i]);
  if (changed < 0 || to[changed] - from[changed] !== 1) {
    return false;
  }
  return to.slice(changed + 1).every((n) => n === 0);
}

function validate(baseRef) {
  const versions = currentVersions();
  const errors = [];
  for (const [file, version] of Object.entries(versions)) {
    if (!version || !SEMVER.test(version)) {
      errors.push(`${file} has version "${version}", which is not X.Y.Z.`);
    }
  }
  if (errors.length === 0 && new Set(Object.values(versions)).size !== 1) {
    const listed = Object.entries(versions).map(([f, v]) => `${f}=${v}`).join(', ');
    errors.push(`The versions disagree: ${listed}. All three must be equal.`);
  }
  if (errors.length === 0 && baseRef) {
    const base = changelogVersion(readAt(baseRef, CHANGELOG));
    const next = versions[CHANGELOG];
    if (!base || !SEMVER.test(base)) {
      errors.push(`Could not read a version from ${CHANGELOG} at ${baseRef}.`);
    } else if (!isSingleIncrement(base, next)) {
      errors.push(
        `The version must move from ${base} by exactly one +1 increment (patch, minor or major, ` +
          `resetting the parts after it), but it is ${next}.`,
      );
    } else {
      process.stdout.write(`Version bump OK: ${base} -> ${next}\n`);
    }
  }
  return { versions, errors };
}

if (require.main === module) {
  let outcome;
  try {
    outcome = validate(process.argv[2]);
  } catch (e) {
    process.stderr.write(`${e.message}\n`);
    process.exit(2);
  }
  if (outcome.errors.length > 0) {
    process.stderr.write(outcome.errors.join('\n') + '\n');
    process.exit(1);
  }
  process.stdout.write(`All versions agree: ${Object.values(outcome.versions)[0]}\n`);
}

module.exports = { isSingleIncrement, pomVersion, packageVersion, changelogVersion, validate };
