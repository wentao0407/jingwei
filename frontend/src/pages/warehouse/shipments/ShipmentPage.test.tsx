import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ShipmentPage } from './ShipmentPage';
import { confirmShipment } from '@/services/warehouse/shipmentService';

vi.mock('@/services/warehouse/shipmentService', () => ({
  confirmShipment: vi.fn(),
}));

const mockedConfirmShipment = vi.mocked(confirmShipment);

describe('ShipmentPage', () => {
  beforeEach(() => {
    mockedConfirmShipment.mockReset();
    mockedConfirmShipment.mockResolvedValue(undefined);
  });

  it('confirms shipment with outbound order id', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('出库单 ID'), { target: { value: '80001' } });
    fireEvent.change(screen.getByLabelText('销售订单 ID'), { target: { value: '50001' } });
    fireEvent.click(screen.getByRole('button', { name: /确认发运/ }));

    await waitFor(() =>
      expect(mockedConfirmShipment).toHaveBeenCalledWith({
        outboundId: '80001',
        salesOrderId: '50001',
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
