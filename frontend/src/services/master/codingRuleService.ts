import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface CodingRuleSegment {
  id?: string;
  segmentType: string;
  segmentValue?: string | null;
  seqLength?: number | null;
  seqResetType?: string | null;
  connector?: string | null;
  sortOrder: number;
}

export interface CodingRuleRecord {
  id: string;
  code: string;
  name: string;
  businessType?: string | null;
  description?: string | null;
  status: string;
  used: boolean;
  segments?: CodingRuleSegment[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateCodingRulePayload {
  code: string;
  name: string;
  businessType?: string;
  description?: string;
  segments: CodingRuleSegment[];
}

export interface UpdateCodingRulePayload {
  name?: string;
  businessType?: string;
  description?: string;
  status?: string;
}

export interface GenerateCodePayload {
  ruleCode: string;
  context?: Record<string, string>;
}

export interface CodePreviewRecord {
  previewCode: string;
}

export async function listCodingRules(): Promise<CodingRuleRecord[]> {
  const response = await apiClient.post('/master/codingRule/list');
  return unwrapApiResponse<CodingRuleRecord[]>(response.data);
}

export async function getCodingRuleDetail(code: string): Promise<CodingRuleRecord> {
  const response = await apiClient.post('/master/codingRule/detail', null, {
    params: { code: code.trim() },
  });
  return unwrapApiResponse<CodingRuleRecord>(response.data);
}

export async function createCodingRule(payload: CreateCodingRulePayload): Promise<CodingRuleRecord> {
  const response = await apiClient.post('/master/codingRule/create', normalizeCodingRulePayload(payload));
  return unwrapApiResponse<CodingRuleRecord>(response.data);
}

export async function updateCodingRule(
  ruleId: string,
  payload: UpdateCodingRulePayload,
): Promise<CodingRuleRecord> {
  const response = await apiClient.post('/master/codingRule/update', normalizeOptionalFields(payload), {
    params: { ruleId },
  });
  return unwrapApiResponse<CodingRuleRecord>(response.data);
}

export async function deleteCodingRule(ruleId: string): Promise<void> {
  const response = await apiClient.post('/master/codingRule/delete', null, { params: { ruleId } });
  return unwrapApiResponse<void>(response.data);
}

export async function previewCode(payload: GenerateCodePayload): Promise<CodePreviewRecord> {
  const response = await apiClient.post('/master/codingRule/preview', normalizeGeneratePayload(payload));
  return unwrapApiResponse<CodePreviewRecord>(response.data);
}

export async function generateCode(payload: GenerateCodePayload): Promise<string> {
  const response = await apiClient.post('/master/codingRule/generate', normalizeGeneratePayload(payload));
  return unwrapApiResponse<string>(response.data);
}

function normalizeCodingRulePayload(payload: CreateCodingRulePayload): CreateCodingRulePayload {
  return {
    ...(normalizeOptionalFields({
      code: payload.code,
      name: payload.name,
      businessType: payload.businessType,
      description: payload.description,
    }) as Omit<CreateCodingRulePayload, 'segments'>),
    segments: payload.segments.map(normalizeSegment),
  };
}

function normalizeSegment(segment: CodingRuleSegment): CodingRuleSegment {
  return normalizeOptionalFields({
    segmentType: segment.segmentType,
    segmentValue: segment.segmentValue,
    seqLength: segment.seqLength,
    seqResetType: segment.seqResetType,
    connector: segment.connector,
    sortOrder: segment.sortOrder,
  }) as CodingRuleSegment;
}

function normalizeGeneratePayload(payload: GenerateCodePayload): GenerateCodePayload {
  const context = normalizeOptionalFields(payload.context ?? {}) as Record<string, string>;
  return normalizeOptionalFields({
    ruleCode: payload.ruleCode,
    context: Object.keys(context).length > 0 ? context : undefined,
  }) as GenerateCodePayload;
}

