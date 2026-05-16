export function normalizeOptionalFields<T extends object>(value: T): Partial<T> {
  return normalizeFields(value, true) as Partial<T>;
}

export function normalizeRequiredFields<T extends object>(value: T): T {
  return normalizeFields(value, false) as T;
}

function normalizeFields<T extends object>(value: T, removeEmpty: boolean) {
  return Object.fromEntries(
    Object.entries(value)
      .map(([key, fieldValue]) => [key, typeof fieldValue === 'string' ? fieldValue.trim() : fieldValue])
      .filter(([, fieldValue]) => !removeEmpty || (fieldValue !== undefined && fieldValue !== null && fieldValue !== '')),
  );
}
