#!/usr/bin/env node
// Converts a JaCoCo XML report to lcov so scripts/patch-coverage.mjs can read it.
// JaCoCo reports coverage per source line as covered instructions (ci) and missed
// instructions (mi); a line is covered when ci > 0.
//
// usage:
//   node scripts/jacoco-to-lcov.mjs [input.xml] [output.info] [--source-root src/main/kotlin]

import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';
import { pathToFileURL } from 'node:url';
import { EXIT } from './lib/exit.mjs';

const DEFAULT_INPUT = 'build/reports/jacoco/test/jacocoTestReport.xml';
const DEFAULT_OUTPUT = 'coverage/lcov.info';
const DEFAULT_SOURCE_ROOT = 'src/main/kotlin';

const PACKAGE_RE = /<package\b[^>]*\bname="([^"]*)"[^>]*>([\s\S]*?)<\/package>/g;
const SOURCEFILE_RE = /<sourcefile\b[^>]*\bname="([^"]*)"[^>]*>([\s\S]*?)<\/sourcefile>/g;
const LINE_RE = /<line\b([^>]*?)\/?>/g;

function attribute(attributes, name) {
  const match = new RegExp(`\\b${name}="([^"]*)"`).exec(attributes);
  return match ? match[1] : null;
}

function linesOf(sourceBody) {
  const lines = [];
  for (const match of sourceBody.matchAll(LINE_RE)) {
    const attributes = match[1];
    const number = Number(attribute(attributes, 'nr'));
    const covered = Number(attribute(attributes, 'ci'));
    if (!Number.isFinite(number) || !Number.isFinite(covered)) continue;
    lines.push({ number, hits: covered > 0 ? 1 : 0, covered });
  }
  return lines;
}

/**
 * Renders lcov text from a JaCoCo XML report.
 * @param {string} xml the JaCoCo report contents
 * @param {{ sourceRoot?: string }} [options]
 * @returns {string} lcov text
 */
export function jacocoToLcov(xml, options = {}) {
  const sourceRoot = (options.sourceRoot ?? DEFAULT_SOURCE_ROOT).replace(/\\/g, '/').replace(/\/+$/, '');
  const records = [];

  for (const packageMatch of xml.matchAll(PACKAGE_RE)) {
    const packageName = packageMatch[1].replace(/^\/|\/$/g, '');
    for (const sourceMatch of packageMatch[2].matchAll(SOURCEFILE_RE)) {
      const sourceName = sourceMatch[1];
      const lines = linesOf(sourceMatch[2]);
      if (lines.length === 0) continue;

      const prefix = packageName ? `${sourceRoot}/${packageName}` : sourceRoot;
      const path = `${prefix}/${sourceName}`;
      const covered = lines.filter((line) => line.hits > 0).length;

      const record = [`SF:${path}`];
      for (const line of lines) record.push(`DA:${line.number},${line.hits}`);
      record.push(`LF:${lines.length}`);
      record.push(`LH:${covered}`);
      record.push('end_of_record');
      records.push(record.join('\n'));
    }
  }

  return records.length === 0 ? '' : `${records.join('\n')}\n`;
}

function parseArgs(argv) {
  const options = {
    input: DEFAULT_INPUT,
    output: DEFAULT_OUTPUT,
    sourceRoot: DEFAULT_SOURCE_ROOT,
    help: false,
  };
  const positional = [];
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '-h' || arg === '--help') options.help = true;
    else if (arg === '--input') { options.input = argv[i + 1]; i += 1; }
    else if (arg === '--output' || arg === '--lcov') { options.output = argv[i + 1]; i += 1; }
    else if (arg === '--source-root') { options.sourceRoot = argv[i + 1]; i += 1; }
    else positional.push(arg);
  }
  if (positional[0]) options.input = positional[0];
  if (positional[1]) options.output = positional[1];
  return options;
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  if (options.help) {
    process.stdout.write('usage: node scripts/jacoco-to-lcov.mjs [input.xml] [output.info] [--source-root <dir>]\n');
    process.exit(EXIT.OK);
  }
  if (!existsSync(options.input)) {
    process.stderr.write(`jacoco-to-lcov: no JaCoCo report at ${options.input}\n`);
    process.exit(EXIT.FAIL);
  }

  const lcov = jacocoToLcov(readFileSync(options.input, 'utf8'), { sourceRoot: options.sourceRoot });
  if (lcov === '') {
    process.stderr.write(`jacoco-to-lcov: ${options.input} contained no source lines\n`);
    process.exit(EXIT.FAIL);
  }

  mkdirSync(dirname(options.output), { recursive: true });
  writeFileSync(options.output, lcov);
  const records = lcov.split('end_of_record').length - 1;
  process.stdout.write(`jacoco-to-lcov: wrote ${records} record(s) to ${options.output}\n`);
  process.exit(EXIT.OK);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main();
}
