import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ShipmentPage } from './ShipmentPage';
import { confirmShipment, getShipmentDetail, pageShipments } from '@/services/warehouse/shipmentService';

vi.mock('@/services/warehouse/shipmentService', () => ({
  confirmShipment: vi.fn(),
  getShipmentDetail: vi.fn(),
  pageShipments: vi.fn(),
}));

const mockedConfirmShipment = vi.mocked(confirmShipment);
const mockedGetShipmentDetail = vi.mocked(getShipmentDetail);
const mockedPageShipments = vi.mocked(pageShipments);

const shipment = {
  id: '80001',
  outboundNo: 'CK-202605-0001',
  outboundTypeLabel: '销售出库',
  warehouseName: '上海仓',
  status: 'CONFIRMED',
  statusLabel: '已确认',
  sourceNo: 'SO-202605-0001',
  carrier: '顺丰',
  trackingNo: 'SF10001',
  lines: [{ id: '1', skuCode: 'SKU-001', plannedQty: 2, actualQty: 2 }],
};

describe('ShipmentPage', () => {
  beforeEach(() => {
    mockedConfirmShipment.mockReset();
    mockedConfirmShipment.mockResolvedValue(undefined);
    mockedGetShipmentDetail.mockReset();
    mockedGetShipmentDetail.mockResolvedValue(shipment);
    mockedPageShipments.mockReset();
    mockedPageShipments.mockResolvedValue({ records: [shipment], total: 1, current: 1, size: 20, pages: 1 });
  });

  it('loads shipment list and opens detail', async () => {
    renderPage();

    expect(await screen.findByText('CK-202605-0001')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '详情 CK-202605-0001' }));

    await waitFor(() => expect(mockedGetShipmentDetail).toHaveBeenCalledWith('80001'));
    expect(await screen.findByText('SKU-001')).toBeInTheDocument();
  });

  it('confirms shipment with outbound order id', async () => {
    renderPage();

    expect(await screen.findByText('CK-202605-0001')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '确认发运 CK-202605-0001' }));

    await waitFor(() =>
      expect(mockedConfirmShipment).toHaveBeenCalledWith({
        outboundId: '80001',
        salesOrderId: undefined,
      }),
    );
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ShipmentPage />
    </AntdApp>,
  );
}
