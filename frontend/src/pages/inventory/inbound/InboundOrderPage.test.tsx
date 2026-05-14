import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { InboundOrderPage } from './InboundOrderPage';
import { confirmInbound, getInboundDetail, pageInboundOrders } from '@/services/inventory/inventoryService';

vi.mock('@/services/inventory/inventoryService', () => ({
  confirmInbound: vi.fn(),
  getInboundDetail: vi.fn(),
  pageInboundOrders: vi.fn(),
}));

const mockedPage = vi.mocked(pageInboundOrders);
const mockedDetail = vi.mocked(getInboundDetail);
const mockedConfirm = vi.mocked(confirmInbound);

describe('InboundOrderPage', () => {
  beforeEach(() => {
    mockedPage.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: [{ id: '10001', inboundNo: 'IN-001', warehouseName: '主仓', status: 'DRAFT', statusLabel: '草稿' }] });
    mockedDetail.mockResolvedValue({ id: '10001', inboundNo: 'IN-001', lines: [{ id: '1', inventoryType: 'MATERIAL', materialName: '高支棉', plannedQty: 10, actualQty: 10 }] });
    mockedConfirm.mockResolvedValue(undefined);
  });

  it('loads filters opens detail and confirms inbound', async () => {
    renderPage();
    expect(await screen.findByText('IN-001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('入库单号'), { target: { value: ' IN-001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));
    await waitFor(() => expect(mockedPage).toHaveBeenLastCalledWith(expect.objectContaining({ inboundNo: 'IN-001' })));
    fireEvent.click(screen.getByRole('button', { name: '详情 IN-001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '确认 IN-001' }));
    await waitFor(() => expect(mockedConfirm).toHaveBeenCalledWith('10001'));
  });
});

function renderPage() {
  render(<AntdApp><InboundOrderPage /></AntdApp>);
}
