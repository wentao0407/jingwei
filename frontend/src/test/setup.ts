import '@testing-library/jest-dom/vitest';

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

const originalGetComputedStyle = window.getComputedStyle;

window.getComputedStyle = (element: Element, pseudoElement?: string | null) => {
  if (pseudoElement) {
    return originalGetComputedStyle(element);
  }

  return originalGetComputedStyle(element);
};
