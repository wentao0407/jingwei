import { render, screen } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApprovalConfigPage } from './ApprovalConfigPage';
import { listApprovalConfigs } from '@/services/approval/approvalService';

vi.mock('@/services/approval/approvalService', () => ({
  createApprovalConfig: vi.fn(),
  deleteApprovalConfig: vi.fn(),
  listApprovalConfigs: vi.fn(),
  updateApprovalConfig: vi.fn(),
}));

const mockedListApprovalConfigs = vi.mocked(listApprovalConfigs);

describe('ApprovalConfigPage', () => {
  beforeEach(() => {
    mockedListApprovalConfigs.mockReset();
    mockedListApprovalConfigs.mockResolvedValue([
      { id: '1', businessType: 'PROCUREMENT_ORDER', configName: '采购审批', approvalMode: 'SINGLE', enabled: true },
    ]);
  });

  it('loads approval configs', async () => {
    render(
      <AntdApp>
        <ApprovalConfigPage />
      </AntdApp>,
    );

    expect(await screen.findByText('采购审批')).toBeInTheDocument();
    expect(mockedListApprovalConfigs).toHaveBeenCalled();
  });
});
