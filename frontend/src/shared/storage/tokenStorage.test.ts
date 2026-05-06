import { describe, expect, it, beforeEach } from 'vitest';
import { clearAccessToken, getAccessToken, setAccessToken } from './tokenStorage';

describe('tokenStorage', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('stores and reads access token', () => {
    setAccessToken('jwt-token');

    expect(getAccessToken()).toBe('jwt-token');
  });

  it('clears access token', () => {
    setAccessToken('jwt-token');
    clearAccessToken();

    expect(getAccessToken()).toBeNull();
  });
});
