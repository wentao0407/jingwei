import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

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

export async function listMyPendingApprovalTasks(): Promise<ApprovalTaskRecord[]> {
  const response = await apiClient.post('/approval/task/myPending');
  return unwrapApiResponse<ApprovalTaskRecord[]>(response.data);
}

export async function approveApprovalTask(payload: ApproveApprovalTaskPayload): Promise<void> {
  const response = await apiClient.post('/approval/approve', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

function normalizeOptionalFields<T extends object>(value: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(value)
      .map(([key, fieldValue]) => [key, typeof fieldValue === 'string' ? fieldValue.trim() : fieldValue])
      .filter(([, fieldValue]) => fieldValue !== undefined && fieldValue !== null && fieldValue !== ''),
  ) as Partial<T>;
}
