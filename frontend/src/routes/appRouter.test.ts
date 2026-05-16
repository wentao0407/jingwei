import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

describe('appRouter lazy loading', () => {
  it('keeps feature pages behind React lazy imports', () => {
    const routerSource = readFileSync(resolve(__dirname, 'appRouter.tsx'), 'utf8');

    expect(routerSource).toContain('lazy(() => import(');
    expect(routerSource).not.toMatch(/import \{ .*Page.* \} from '@\/pages\//);
  });
});
