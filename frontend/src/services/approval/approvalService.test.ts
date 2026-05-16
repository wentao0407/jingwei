import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  approveApprovalTask,
  createApprovalConfig,
  deleteApprovalConfig,
  getApprovalConfig,
  listApprovalRecords,
  listApprovalConfigs,
  listMyPendingApprovalTasks,
  submitApproval,
  updateApprovalConfig,
} from './approvalService';
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

  it('loads approval records by business identity', async () => {
    mockedPost.mockResolvedValueOnce({
      data: ok([{ id: '60002', businessType: 'SALES_ORDER', businessId: '50001', status: 'APPROVED' }]),
    });

    await expect(
      listApprovalRecords({
        businessType: ' SALES_ORDER ',
        businessId: ' 50001 ',
        businessNo: ' SO-202605-0001 ',
      }),
    ).resolves.toEqual([
      { id: '60002', businessType: 'SALES_ORDER', businessId: '50001', status: 'APPROVED' },
    ]);

    expect(mockedPost).toHaveBeenCalledWith('/approval/task/records', {
      businessType: 'SALES_ORDER',
      businessId: '50001',
      businessNo: 'SO-202605-0001',
    });
  });

  it('submits a business document to approval engine', async () => {
    mockedPost.mockResolvedValueOnce({ data: ok(true) });

    await expect(
      submitApproval({
        businessType: ' SALES_ORDER ',
        businessId: ' 50001 ',
        businessNo: ' SO-202605-0001 ',
      }),
    ).resolves.toBe(true);

    expect(mockedPost).toHaveBeenCalledWith('/approval/submit', {
      businessType: 'SALES_ORDER',
      businessId: '50001',
      businessNo: 'SO-202605-0001',
    });
  });

  it('manages approval configs', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok([{ id: '30001', businessType: 'SALES_ORDER' }]) })
      .mockResolvedValueOnce({ data: ok({ id: '30001', businessType: 'SALES_ORDER' }) })
      .mockResolvedValueOnce({ data: ok({ id: '30002', businessType: 'PROCUREMENT_ORDER' }) })
      .mockResolvedValueOnce({ data: ok({ id: '30002', businessType: 'PROCUREMENT_ORDER' }) })
      .mockResolvedValueOnce({ data: ok(null) });

    await listApprovalConfigs();
    await getApprovalConfig(' 30001 ');
    await createApprovalConfig({
      businessType: ' PROCUREMENT_ORDER ',
      configName: ' 采购审批 ',
      approvalMode: ' OR_SIGN ',
      approverRoleIds: [' 1 ', '2'],
      enabled: true,
    });
    await updateApprovalConfig({
      id: ' 30002 ',
      configName: ' 采购审批 V2 ',
      approvalMode: '',
      approverRoleIds: ['2'],
      enabled: false,
    });
    await deleteApprovalConfig(' 30002 ');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/approval/config/list');
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/approval/config/detail', '30001');
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/approval/config/create', {
      businessType: 'PROCUREMENT_ORDER',
      configName: '采购审批',
      approvalMode: 'OR_SIGN',
      approverRoleIds: ['1', '2'],
      enabled: true,
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/approval/config/update', {
      id: '30002',
      configName: '采购审批 V2',
      approverRoleIds: ['2'],
      enabled: false,
    });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/approval/config/delete', '30002');
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
