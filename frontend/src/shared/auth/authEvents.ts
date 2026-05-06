export const UNAUTHORIZED_EVENT_NAME = 'jingwei:unauthorized';

export function emitUnauthorized(): void {
  window.dispatchEvent(new CustomEvent(UNAUTHORIZED_EVENT_NAME));
}

export function onUnauthorized(listener: () => void): () => void {
  window.addEventListener(UNAUTHORIZED_EVENT_NAME, listener);

  return () => {
    window.removeEventListener(UNAUTHORIZED_EVENT_NAME, listener);
  };
}
