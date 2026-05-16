import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReportCenterPage } from './ReportCenterPage';
import {
  exportInventoryLedger,
  queryInventoryLedgerMatrix,
  pageInventoryLedger,
  pageOperationFlows,
  queryInventoryAge,
  queryTurnoverAnalysis,
} from '@/services/report/reportService';

vi.mock('@/services/report/reportService', () => ({
  exportInventoryAge: vi.fn(),
  exportInventoryLedger: vi.fn(),
  exportOperationFlows: vi.fn(),
  exportTurnoverAnalysis: vi.fn(),
  pageInventoryLedger: vi.fn(),
  pageOperationFlows: vi.fn(),
  queryInventoryLedgerMatrix: vi.fn(),
  queryInventoryAge: vi.fn(),
  queryTurnoverAnalysis: vi.fn(),
}));

const mockedExportLedger = vi.mocked(exportInventoryLedger);
const mockedQueryLedgerMatrix = vi.mocked(queryInventoryLedgerMatrix);
const mockedPageLedger = vi.mocked(pageInventoryLedger);
const mockedPageFlows = vi.mocked(pageOperationFlows);
const mockedQueryAge = vi.mocked(queryInventoryAge);
const mockedQueryTurnover = vi.mocked(queryTurnoverAnalysis);

describe('ReportCenterPage', () => {
  beforeEach(() => {
    mockedExportLedger.mockReset();
    mockedExportLedger.mockResolvedValue(new Blob(['ledger']));
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
    mockedQueryLedgerMatrix.mockReset();
    mockedQueryLedgerMatrix.mockResolvedValue({
      spuId: '80001',
      warehouseId: '30001',
      spuName: '经典 Polo',
      sizes: ['S', 'M'],
      matrix: { 红色: { S: 10, M: 12 } },
      colorTotals: { 红色: 22 },
      sizeTotals: { S: 10, M: 12 },
      grandTotal: 22,
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

  it('exports the active report with current filters', async () => {
    renderPage();

    expect(await screen.findByText('JW-POLO-RED-M')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('SKU/物料/单号'), { target: { value: ' POLO ' } });
    fireEvent.click(screen.getByRole('button', { name: /导出报表/ }));

    await waitFor(() =>
      expect(mockedExportLedger).toHaveBeenCalledWith({
        current: 1,
        size: 20,
        inventoryType: 'SKU',
        keyword: ' POLO ',
      }),
    );
  });

  it('loads the ledger matrix view', async () => {
    renderPage();

    expect(await screen.findByText('JW-POLO-RED-M')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: /矩阵视图/ }));
    fireEvent.change(screen.getByLabelText('款式 ID'), { target: { value: '80001' } });
    fireEvent.change(screen.getByLabelText('仓库 ID'), { target: { value: '30001' } });
    fireEvent.click(screen.getByRole('button', { name: '查询矩阵' }));

    await waitFor(() => expect(mockedQueryLedgerMatrix).toHaveBeenCalledWith('80001', '30001'));
    expect(await screen.findByText('经典 Polo')).toBeInTheDocument();
    expect(screen.getByText('红色')).toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ReportCenterPage />
    </AntdApp>,
  );
}
