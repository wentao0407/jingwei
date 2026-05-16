import { describe, expect, it } from 'vitest';
import { normalizeOptionalFields, normalizeRequiredFields } from './normalize';

describe('normalize helpers', () => {
  it('trims strings and removes optional empty fields', () => {
    expect(
      normalizeOptionalFields({
        keyword: ' 春季 ',
        warehouseId: '',
        categoryId: null,
        current: 0,
      }),
    ).toEqual({
      keyword: '春季',
      current: 0,
    });
  });

  it('trims strings while preserving required empty fields', () => {
    expect(
      normalizeRequiredFields({
        username: ' admin ',
        password: '',
      }),
    ).toEqual({
      username: 'admin',
      password: '',
    });
  });
});
