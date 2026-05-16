import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface SpuQueryParams {
  status?: string;
  seasonId?: string;
  categoryId?: string;
}

export interface ColorItemPayload {
  colorName: string;
  colorCode: string;
  pantoneCode?: string;
  fabricMaterialId?: string;
  colorImage?: string;
}

export interface ColorWayRecord {
  id: string;
  spuId: string;
  colorName: string;
  colorCode: string;
  pantoneCode?: string | null;
  fabricMaterialId?: string | null;
  colorImage?: string | null;
  sortOrder?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SkuRecord {
  id: string;
  code: string;
  barcode?: string | null;
  spuId: string;
  colorWayId: string;
  sizeId: string;
  costPrice?: number | null;
  salePrice?: number | null;
  wholesalePrice?: number | null;
  status: string;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SpuRecord {
  id: string;
  code: string;
  name: string;
  seasonId?: string | null;
  categoryId?: string | null;
  brandId?: string | null;
  sizeGroupId: string;
  designImage?: string | null;
  status: string;
  remark?: string | null;
  colorWays?: ColorWayRecord[] | null;
  skus?: SkuRecord[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateSpuPayload {
  name: string;
  seasonId?: string;
  categoryId?: string;
  brandId?: string;
  sizeGroupId: string;
  designImage?: string;
  remark?: string;
  colors: ColorItemPayload[];
}

export interface UpdateSpuPayload {
  name?: string;
  seasonId?: string;
  categoryId?: string;
  brandId?: string;
  designImage?: string;
  status?: string;
  remark?: string;
}

export interface UpdateSkuPricePayload {
  skuId: string;
  costPrice?: number;
  salePrice?: number;
  wholesalePrice?: number;
}

export interface BatchUpdateSkuPricePayload {
  spuId: string;
  colorWayId?: string;
  costPrice?: number;
  salePrice?: number;
  wholesalePrice?: number;
}

export async function listSpus(params: SpuQueryParams = {}): Promise<SpuRecord[]> {
  const response = await apiClient.post('/master/spu/list', null, {
    params: normalizeOptionalFields(params),
  });
  return unwrapApiResponse<SpuRecord[]>(response.data);
}

export async function getSpuDetail(spuId: string): Promise<SpuRecord> {
  const response = await apiClient.post('/master/spu/detail', null, {
    params: { spuId },
  });
  return unwrapApiResponse<SpuRecord>(response.data);
}

export async function createSpu(payload: CreateSpuPayload): Promise<SpuRecord> {
  const response = await apiClient.post('/master/spu/create', normalizeSpuPayload(payload));
  return unwrapApiResponse<SpuRecord>(response.data);
}

export async function updateSpu(spuId: string, payload: UpdateSpuPayload): Promise<SpuRecord> {
  const response = await apiClient.post('/master/spu/update', normalizeOptionalFields(payload), {
    params: { spuId },
  });
  return unwrapApiResponse<SpuRecord>(response.data);
}

export async function deleteSpu(spuId: string): Promise<void> {
  const response = await apiClient.post('/master/spu/delete', null, {
    params: { spuId },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function addSpuColors(spuId: string, colors: ColorItemPayload[]): Promise<SpuRecord> {
  const response = await apiClient.post('/master/spu/addColor', { colors: colors.map(normalizeColorPayload) }, {
    params: { spuId },
  });
  return unwrapApiResponse<SpuRecord>(response.data);
}

export async function updateSkuPrice(payload: UpdateSkuPricePayload): Promise<SkuRecord> {
  const response = await apiClient.post('/master/sku/updatePrice', normalizeOptionalFields(payload));
  return unwrapApiResponse<SkuRecord>(response.data);
}

export async function batchUpdateSkuPrice(payload: BatchUpdateSkuPricePayload): Promise<number> {
  const response = await apiClient.post('/master/sku/batchUpdatePrice', normalizeOptionalFields(payload));
  return unwrapApiResponse<number>(response.data);
}

export async function deactivateSku(skuId: string): Promise<void> {
  const response = await apiClient.post('/master/sku/deactivate', null, {
    params: { skuId },
  });
  return unwrapApiResponse<void>(response.data);
}

function normalizeSpuPayload(payload: CreateSpuPayload) {
  return normalizeOptionalFields({
    ...payload,
    colors: payload.colors.map(normalizeColorPayload),
  });
}

function normalizeColorPayload(payload: ColorItemPayload): Partial<ColorItemPayload> {
  return normalizeOptionalFields(payload);
}

