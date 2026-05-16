import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApprovalCenterPage } from './ApprovalCenterPage';
import { approveApprovalTask, listApprovalRecords, listMyPendingApprovalTasks } from '@/services/approval/approvalService';

vi.mock('@/services/approval/approvalService', () => ({
  approveApprovalTask: vi.fn(),
  listApprovalRecords: vi.fn(),
  listMyPendingApprovalTasks: vi.fn(),
}));

const mockedApproveApprovalTask = vi.mocked(approveApprovalTask);
const mockedListApprovalRecords = vi.mocked(listApprovalRecords);
const mockedListMyPendingApprovalTasks = vi.mocked(listMyPendingApprovalTasks);

describe('ApprovalCenterPage', () => {
  beforeEach(() => {
    mockedListApprovalRecords.mockReset();
    mockedListApprovalRecords.mockResolvedValue([
      {
        id: '60002',
        businessType: 'SALES_ORDER',
        businessId: '50001',
        businessNo: 'SO-202605-0001',
        approvalMode: 'SINGLE',
        status: 'APPROVED',
        opinion: '同意发货',
        approvedAt: '2026-05-15 10:00:00',
      },
    ]);
    mockedApproveApprovalTask.mockReset();
    mockedApproveApprovalTask.mockResolvedValue(undefined);
    mockedListMyPendingApprovalTasks.mockReset();
    mockedListMyPendingApprovalTasks.mockResolvedValue([
      {
        id: '60001',
        businessType: 'SALES_ORDER',
        businessId: '50001',
        businessNo: 'SO-202605-0001',
        approvalMode: 'SINGLE',
        status: 'PENDING',
      },
    ]);
  });

  it('loads pending tasks and approves selected task with opinion', async () => {
    renderPage();

    expect(await screen.findByText('SO-202605-0001')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '审批 60001' }));
    const dialog = await screen.findByRole('dialog');
    fireEvent.change(within(dialog).getByLabelText('审批意见'), { target: { value: '同意' } });
    fireEvent.click(screen.getByRole('button', { name: /通过/ }));

    await waitFor(() =>
      expect(mockedApproveApprovalTask).toHaveBeenCalledWith({
        taskId: '60001',
        approved: true,
        opinion: '同意',
      }),
    );
  });

  it('shows approval records for the selected business document', async () => {
    renderPage();

    expect(await screen.findByText('SO-202605-0001')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '审批历史 60001' }));

    const dialog = await screen.findByRole('dialog', { name: '审批历史' });
    expect(await within(dialog).findByText('同意发货')).toBeInTheDocument();
    expect(within(dialog).getByText('APPROVED')).toBeInTheDocument();
    expect(mockedListApprovalRecords).toHaveBeenCalledWith({
      businessType: 'SALES_ORDER',
      businessId: '50001',
      businessNo: 'SO-202605-0001',
    });
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ApprovalCenterPage />
    </AntdApp>,
  );
}
