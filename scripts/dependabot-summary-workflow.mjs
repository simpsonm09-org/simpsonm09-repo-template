import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));
export const WORKFLOW_PATH = resolve(HERE, '../.github/workflows/dependabot-summary.yml');

const BEGIN = '// dependabot-summary renderer: begin';
const END = '// dependabot-summary renderer: end';

/**
 * Extract the renderer that the workflow actually runs from the workflow YAML.
 * The renderer is inline so the job needs no checkout; the tests execute this
 * exact source rather than a copy.
 */
export function extractRenderer(workflowText) {
  const beginIndex = workflowText.indexOf(BEGIN);
  const endIndex = workflowText.indexOf(END);
  if (beginIndex === -1 || endIndex === -1 || endIndex < beginIndex) {
    throw new Error('the dependabot-summary renderer markers are missing or out of order');
  }

  const bodyStart = workflowText.indexOf('\n', beginIndex) + 1;
  const lines = workflowText.slice(bodyStart, endIndex).split('\n');
  const indents = lines.filter((line) => line.trim() !== '').map((line) => line.match(/^ */)[0].length);
  const indent = indents.length === 0 ? 0 : Math.min(...indents);
  return `${lines.map((line) => line.slice(indent)).join('\n').trimEnd()}\n`;
}

export function readRenderer() {
  return extractRenderer(readFileSync(WORKFLOW_PATH, 'utf8'));
}
