import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { StocktakingPage } from './StocktakingPage';
import { getStocktakingDetail, pageStocktakingOrders, recordStocktakingCount, startStocktaking } from '@/services/inventory/inventoryService';

vi.mock('@/services/inventory/inventoryService', () => ({
  getStocktakingDetail: vi.fn(),
  pageStocktakingOrders: vi.fn(),
  recordStocktakingCount: vi.fn(),
  startStocktaking: vi.fn(),
}));

const mockedPage = vi.mocked(pageStocktakingOrders);
const mockedDetail = vi.mocked(getStocktakingDetail);
const mockedRecord = vi.mocked(recordStocktakingCount);
const mockedStart = vi.mocked(startStocktaking);

describe('StocktakingPage', () => {
  beforeEach(() => {
    mockedPage.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: [{ id: '30001', stocktakingNo: 'ST-001', warehouseName: '主仓', status: 'DRAFT', statusLabel: '草稿' }] });
    mockedDetail.mockResolvedValue({ id: '30001', stocktakingNo: 'ST-001', lines: [{ id: '31001', inventoryType: 'MATERIAL', materialName: '高支棉', systemQty: 10 }] });
    mockedRecord.mockResolvedValue(undefined);
    mockedStart.mockResolvedValue(undefined);
  });

  it('loads detail starts stocktaking and records count', async () => {
    renderPage();
    expect(await screen.findByText('ST-001')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '详情 ST-001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '开始 ST-001' }));
    await waitFor(() => expect(mockedStart).toHaveBeenCalledWith('30001'));
    fireEvent.click(await screen.findByRole('button', { name: '实盘 31001' }));
    fireEvent.change(screen.getByLabelText('实盘数量'), { target: { value: '8' } });
    fireEvent.click(screen.getByRole('button', { name: /保存实盘/ }));
    await waitFor(() => expect(mockedRecord).toHaveBeenCalledWith({ stocktakingId: '30001', lineId: '31001', actualQty: 8 }));
  });
});

function renderPage() {
  render(<AntdApp><StocktakingPage /></AntdApp>);
}
