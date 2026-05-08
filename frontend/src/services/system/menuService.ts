import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface MenuRecord {
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
  createdAt?: string | null;
  updatedAt?: string | null;
  children?: MenuRecord[] | null;
}

export interface CreateMenuPayload {
  parentId: string;
  name: string;
  type: string;
  path?: string;
  component?: string;
  permission?: string;
  icon?: string;
  sortOrder?: number;
  visible?: boolean;
}

export interface UpdateMenuPayload {
  parentId?: string;
  name?: string;
  type?: string;
  path?: string;
  component?: string;
  permission?: string;
  icon?: string;
  sortOrder?: number;
  visible?: boolean;
  status?: string;
}

export interface AssignMenuPermissionsPayload {
  roleId: string;
  menuIds: string[];
}

export async function listMenus(): Promise<MenuRecord[]> {
  const response = await apiClient.post('/system/menu/tree');
  return unwrapApiResponse<MenuRecord[]>(response.data);
}

export async function createMenu(payload: CreateMenuPayload): Promise<MenuRecord> {
  const response = await apiClient.post('/system/menu/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<MenuRecord>(response.data);
}

export async function updateMenu(menuId: string, payload: UpdateMenuPayload): Promise<MenuRecord> {
  const response = await apiClient.post('/system/menu/update', normalizeOptionalFields(payload), {
    params: { menuId },
  });
  return unwrapApiResponse<MenuRecord>(response.data);
}

export async function deleteMenu(menuId: string): Promise<void> {
  const response = await apiClient.post('/system/menu/delete', null, {
    params: { menuId },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function getRoleMenuIds(roleId: string): Promise<string[]> {
  const response = await apiClient.post('/system/menu/roleMenuIds', null, {
    params: { roleId },
  });
  return unwrapApiResponse<string[]>(response.data);
}

export async function assignMenuPermissions(payload: AssignMenuPermissionsPayload): Promise<void> {
  const response = await apiClient.post('/system/menu/assign', payload);
  return unwrapApiResponse<void>(response.data);
}

function normalizeOptionalFields<T extends object>(payload: T): T {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;
}
