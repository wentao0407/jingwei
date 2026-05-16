import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface CategoryRecord {
  id: string;
  parentId?: string | null;
  code: string;
  name: string;
  level: number;
  sortOrder?: number | null;
  status: string;
  createdAt?: string | null;
  updatedAt?: string | null;
  children?: CategoryRecord[];
}

export interface CreateCategoryPayload {
  parentId?: string | null;
  code: string;
  name: string;
  sortOrder?: number;
}

export interface UpdateCategoryPayload {
  code?: string;
  name?: string;
  sortOrder?: number;
  status?: string;
}

export async function listCategoryTree(): Promise<CategoryRecord[]> {
  const response = await apiClient.post('/master/category/tree');
  return unwrapApiResponse<CategoryRecord[]>(response.data);
}

export async function createCategory(payload: CreateCategoryPayload): Promise<CategoryRecord> {
  const response = await apiClient.post('/master/category/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<CategoryRecord>(response.data);
}

export async function updateCategory(categoryId: string, payload: UpdateCategoryPayload): Promise<CategoryRecord> {
  const response = await apiClient.post('/master/category/update', normalizeOptionalFields(payload), {
    params: { categoryId },
  });
  return unwrapApiResponse<CategoryRecord>(response.data);
}

export async function deleteCategory(categoryId: string): Promise<void> {
  const response = await apiClient.post('/master/category/delete', null, { params: { categoryId } });
  return unwrapApiResponse<void>(response.data);
}

