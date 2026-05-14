import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReportCenterPage } from './ReportCenterPage';
import { pageInventoryLedger, pageOperationFlows, queryInventoryAge, queryTurnoverAnalysis } from '@/services/report/reportService';

vi.mock('@/services/report/reportService', () => ({
  pageInventoryLedger: vi.fn(),
  pageOperationFlows: vi.fn(),
  queryInventoryAge: vi.fn(),
  queryTurnoverAnalysis: vi.fn(),
}));

const mockedPageLedger = vi.mocked(pageInventoryLedger);
const mockedPageFlows = vi.mocked(pageOperationFlows);
const mockedQueryAge = vi.mocked(queryInventoryAge);
const mockedQueryTurnover = vi.mocked(queryTurnoverAnalysis);

describe('ReportCenterPage', () => {
  beforeEach(() => {
    mockedPageLedger.mockReset();
    mockedPageLedger.mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      pages: 1,
      records: [{ inventoryId: '1', skuCode: 'JW-POLO-RED-M', warehouseName: '主仓', totalQty: 80, totalAmount: 4000 }],
    });
    mockedPageFlows.mockReset();
    mockedPageFlows.mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      pages: 1,
      records: [{ id: '2', operationNo: 'OP-202605-0001', operationTypeLabel: '采购到货', changeQty: 100 }],
    });
    mockedQueryAge.mockReset();
    mockedQueryAge.mockResolvedValue({
      totalCount: 1,
      totalQty: 80,
      totalAmount: 4000,
      overdueCount: 0,
      overdueQty: 0,
      details: [{ inventoryId: '1', skuCode: 'JW-POLO-RED-M', ageRange: '0-30天', ageDays: 10, totalQty: 80 }],
    });
    mockedQueryTurnover.mockReset();
    mockedQueryTurnover.mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      pages: 1,
      records: [{ skuCode: 'JW-POLO-RED-M', turnoverGradeLabel: '正常', turnoverDays: 30 }],
    });
  });

  it('loads all report tabs and refreshes the active report', async () => {
    renderPage();

    expect(await screen.findByText('JW-POLO-RED-M')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('tab', { name: '出入库流水' }));
    expect(await screen.findByText('OP-202605-0001')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('tab', { name: '库龄分析' }));
    expect(await screen.findByText('0-30天')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('tab', { name: '畅滞销分析' }));
    expect(await screen.findByText('正常')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /查询报表/ }));
    await waitFor(() => expect(mockedQueryTurnover).toHaveBeenCalledTimes(2));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ReportCenterPage />
    </AntdApp>,
  );
}
