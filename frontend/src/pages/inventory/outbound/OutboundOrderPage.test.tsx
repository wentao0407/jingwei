import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { OutboundOrderPage } from './OutboundOrderPage';
import { confirmOutbound, getOutboundDetail, pageOutboundOrders } from '@/services/inventory/inventoryService';

vi.mock('@/services/inventory/inventoryService', () => ({
  confirmOutbound: vi.fn(),
  getOutboundDetail: vi.fn(),
  pageOutboundOrders: vi.fn(),
}));

const mockedPage = vi.mocked(pageOutboundOrders);
const mockedDetail = vi.mocked(getOutboundDetail);
const mockedConfirm = vi.mocked(confirmOutbound);

describe('OutboundOrderPage', () => {
  beforeEach(() => {
    mockedPage.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: [{ id: '20001', outboundNo: 'OUT-001', warehouseName: '主仓', status: 'DRAFT', statusLabel: '草稿' }] });
    mockedDetail.mockResolvedValue({ id: '20001', outboundNo: 'OUT-001', lines: [{ id: '1', inventoryType: 'MATERIAL', materialName: '高支棉', plannedQty: 5 }] });
    mockedConfirm.mockResolvedValue(undefined);
  });

  it('loads filters opens detail and confirms outbound', async () => {
    renderPage();
    expect(await screen.findByText('OUT-001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('出库单号'), { target: { value: ' OUT-001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));
    await waitFor(() => expect(mockedPage).toHaveBeenLastCalledWith(expect.objectContaining({ outboundNo: 'OUT-001' })));
    fireEvent.click(screen.getByRole('button', { name: '详情 OUT-001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '确认 OUT-001' }));
    await waitFor(() => expect(mockedConfirm).toHaveBeenCalledWith('20001'));
  });
});

function renderPage() {
  render(<AntdApp><OutboundOrderPage /></AntdApp>);
}
