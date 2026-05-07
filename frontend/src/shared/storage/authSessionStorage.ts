const AUTH_SESSION_KEY = 'jingwei.authSession';

export interface AuthMenuItem {
  id: string;
  parentId: string;
  name: string;
  type: string;
  path?: string | null;
  component?: string | null;
  permission?: string | null;
  icon?: string | null;
  sortOrder?: number | null;
  visible?: boolean | null;
  status?: string | null;
  children?: AuthMenuItem[] | null;
}

export interface AuthSession {
  userId: string;
  username: string;
  realName: string;
  roleIds: string[];
  permissions: string[];
  menuTree: AuthMenuItem[];
}

export function getAuthSession(): AuthSession | null {
  const rawSession = window.localStorage.getItem(AUTH_SESSION_KEY);

  if (!rawSession) {
    return null;
  }

  try {
    const session = JSON.parse(rawSession) as AuthSession;
    return isValidAuthSession(session) ? session : null;
  } catch {
    clearAuthSession();
    return null;
  }
}

export function setAuthSession(session: AuthSession): void {
  window.localStorage.setItem(AUTH_SESSION_KEY, JSON.stringify(session));
}

export function clearAuthSession(): void {
  window.localStorage.removeItem(AUTH_SESSION_KEY);
}

function isValidAuthSession(value: AuthSession): boolean {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof value.userId === 'string' &&
    typeof value.username === 'string' &&
    typeof value.realName === 'string' &&
    Array.isArray(value.roleIds) &&
    Array.isArray(value.permissions) &&
    Array.isArray(value.menuTree)
  );
}
