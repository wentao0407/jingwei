import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

describe('vite chunk splitting', () => {
  it('defines vendor manual chunks for large shared dependencies', () => {
    const configSource = readFileSync(resolve(__dirname, '../../vite.config.js'), 'utf8');

    expect(configSource).toContain('manualChunks');
    expect(configSource).toContain('vendor-antd');
    expect(configSource).toContain('vendor-pro');
    expect(configSource).toContain('chunkSizeWarningLimit');
  });
});
