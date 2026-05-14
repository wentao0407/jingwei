import { beforeEach, describe, expect, it, vi } from 'vitest';
import { approveApprovalTask, listMyPendingApprovalTasks } from './approvalService';
import { apiClient } from '@/services/http/apiClient';

vi.mock('@/services/http/apiClient', async () => {
  const actual = await vi.importActual<typeof import('@/services/http/apiClient')>('@/services/http/apiClient');
  return {
    ...actual,
    apiClient: {
      post: vi.fn(),
    },
  };
});

const mockedPost = vi.mocked(apiClient.post);

describe('approvalService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('loads pending approval tasks and submits an approval decision', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok([{ id: '60001', status: 'PENDING' }]) })
      .mockResolvedValueOnce({ data: ok(null) });

    await listMyPendingApprovalTasks();
    await approveApprovalTask({ taskId: ' 60001 ', approved: true, opinion: ' 同意 ' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/approval/task/myPending');
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/approval/approve', {
      taskId: '60001',
      approved: true,
      opinion: '同意',
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
