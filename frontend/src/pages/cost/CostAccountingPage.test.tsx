import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CostAccountingPage } from './CostAccountingPage';
import { getCostDetail, getCostIssueDetails } from '@/services/cost/costService';

vi.mock('@/services/cost/costService', () => ({
  getCostDetail: vi.fn(),
  getCostIssueDetails: vi.fn(),
}));

const mockedGetCostDetail = vi.mocked(getCostDetail);
const mockedGetIssueDetails = vi.mocked(getCostIssueDetails);

describe('CostAccountingPage', () => {
  beforeEach(() => {
    mockedGetCostDetail.mockReset();
    mockedGetCostDetail.mockResolvedValue({
      id: '1',
      productionOrderId: '50001',
      productionLineId: '51001',
      materialCost: 90031.5,
      trimCost: 38250,
      packagingCost: 6300,
      totalCost: 134581.5,
      completedQty: 900,
      unitCost: 149.54,
    });
    mockedGetIssueDetails.mockReset();
    mockedGetIssueDetails.mockResolvedValue([
      {
        id: '2',
        productionOrderId: '50001',
        productionLineId: '51001',
        materialTypeLabel: '面料',
        issueQty: 1895.4,
        unitCost: 47.5,
        costAmount: 90031.5,
      },
    ]);
  });

  it('queries production cost detail and material issue costs', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('生产订单 ID'), { target: { value: '50001' } });
    fireEvent.change(screen.getByLabelText('生产行 ID'), { target: { value: '51001' } });
    fireEvent.click(screen.getByRole('button', { name: /查询成本/ }));

    await waitFor(() =>
      expect(mockedGetCostDetail).toHaveBeenCalledWith({
        productionOrderId: '50001',
        productionLineId: '51001',
      }),
    );
    expect(await screen.findByText('134581.5')).toBeInTheDocument();
    expect(screen.getByText('面料')).toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <CostAccountingPage />
    </AntdApp>,
  );
}
