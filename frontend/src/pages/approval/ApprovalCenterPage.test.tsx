import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApprovalCenterPage } from './ApprovalCenterPage';
import { approveApprovalTask, listMyPendingApprovalTasks } from '@/services/approval/approvalService';

vi.mock('@/services/approval/approvalService', () => ({
  approveApprovalTask: vi.fn(),
  listMyPendingApprovalTasks: vi.fn(),
}));

const mockedApproveApprovalTask = vi.mocked(approveApprovalTask);
const mockedListMyPendingApprovalTasks = vi.mocked(listMyPendingApprovalTasks);

describe('ApprovalCenterPage', () => {
  beforeEach(() => {
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
});

function renderPage() {
  render(
    <AntdApp>
      <ApprovalCenterPage />
    </AntdApp>,
  );
}
