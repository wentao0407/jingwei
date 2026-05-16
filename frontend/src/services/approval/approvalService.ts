import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface ApprovalTaskRecord {
  id: string;
  businessType?: string | null;
  businessId?: string | null;
  businessNo?: string | null;
  approvalMode?: string | null;
  status?: string | null;
  approverId?: string | null;
  approverRoleId?: string | null;
  opinion?: string | null;
  approvedAt?: string | null;
  createdAt?: string | null;
}

export interface ApproveApprovalTaskPayload {
  taskId: string;
  approved: boolean;
  opinion: string;
}

export interface ApprovalRecordsQuery {
  businessType: string;
  businessId: string;
  businessNo?: string;
}

export interface SubmitApprovalPayload {
  businessType: string;
  businessId: string;
  businessNo: string;
}

export interface ApprovalConfigRecord {
  id: string;
  businessType?: string | null;
  configName?: string | null;
  approvalMode?: string | null;
  approverRoleIds?: string[] | null;
  enabled?: boolean | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateApprovalConfigPayload {
  businessType: string;
  configName: string;
  approvalMode: string;
  approverRoleIds: string[];
  enabled?: boolean;
}

export interface UpdateApprovalConfigPayload {
  id: string;
  configName?: string;
  approvalMode?: string;
  approverRoleIds?: string[];
  enabled?: boolean;
}

export async function listMyPendingApprovalTasks(): Promise<ApprovalTaskRecord[]> {
  const response = await apiClient.post('/approval/task/myPending');
  return unwrapApiResponse<ApprovalTaskRecord[]>(response.data);
}

export async function submitApproval(payload: SubmitApprovalPayload): Promise<boolean> {
  const response = await apiClient.post('/approval/submit', normalizeOptionalFields(payload));
  return unwrapApiResponse<boolean>(response.data);
}

export async function listApprovalConfigs(): Promise<ApprovalConfigRecord[]> {
  const response = await apiClient.post('/approval/config/list');
  return unwrapApiResponse<ApprovalConfigRecord[]>(response.data);
}

export async function getApprovalConfig(configId: string): Promise<ApprovalConfigRecord> {
  const response = await apiClient.post('/approval/config/detail', configId.trim());
  return unwrapApiResponse<ApprovalConfigRecord>(response.data);
}

export async function createApprovalConfig(
  payload: CreateApprovalConfigPayload,
): Promise<ApprovalConfigRecord> {
  const response = await apiClient.post('/approval/config/create', normalizeApprovalConfigPayload(payload));
  return unwrapApiResponse<ApprovalConfigRecord>(response.data);
}

export async function updateApprovalConfig(
  payload: UpdateApprovalConfigPayload,
): Promise<ApprovalConfigRecord> {
  const response = await apiClient.post('/approval/config/update', normalizeApprovalConfigPayload(payload));
  return unwrapApiResponse<ApprovalConfigRecord>(response.data);
}

export async function deleteApprovalConfig(configId: string): Promise<void> {
  const response = await apiClient.post('/approval/config/delete', configId.trim());
  return unwrapApiResponse<void>(response.data);
}

export async function listApprovalRecords(payload: ApprovalRecordsQuery): Promise<ApprovalTaskRecord[]> {
  const response = await apiClient.post('/approval/task/records', normalizeOptionalFields(payload));
  return unwrapApiResponse<ApprovalTaskRecord[]>(response.data);
}

export async function approveApprovalTask(payload: ApproveApprovalTaskPayload): Promise<void> {
  const response = await apiClient.post('/approval/approve', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

function normalizeApprovalConfigPayload<T extends { approverRoleIds?: string[] }>(payload: T): Record<string, unknown> {
  return normalizeOptionalFields({
    ...payload,
    approverRoleIds: payload.approverRoleIds?.map((id) => id.trim()).filter(Boolean),
  }) as Record<string, unknown>;
}
