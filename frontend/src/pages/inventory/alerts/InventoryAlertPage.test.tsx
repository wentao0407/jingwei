import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { InventoryAlertPage } from './InventoryAlertPage';
import { acknowledgeAlert, listInventoryAlerts, scanInventoryAlerts } from '@/services/inventory/alertService';

vi.mock('@/services/inventory/alertService', () => ({
  acknowledgeAlert: vi.fn(),
  listInventoryAlerts: vi.fn(),
  scanInventoryAlerts: vi.fn(),
}));

const mockedAcknowledgeAlert = vi.mocked(acknowledgeAlert);
const mockedListInventoryAlerts = vi.mocked(listInventoryAlerts);
const mockedScanInventoryAlerts = vi.mocked(scanInventoryAlerts);

describe('InventoryAlertPage', () => {
  beforeEach(() => {
    mockedAcknowledgeAlert.mockReset();
    mockedAcknowledgeAlert.mockResolvedValue(undefined);
    mockedListInventoryAlerts.mockReset();
    mockedListInventoryAlerts.mockResolvedValue([
      {
        id: '90001',
        ruleName: '低库存预警',
        alertTypeLabel: '低库存',
        inventoryType: 'SKU',
        skuCode: 'JW-POLO-RED-M',
        warehouseName: '主仓',
        currentValue: 5,
        thresholdValue: 10,
        status: 'ACTIVE',
        statusLabel: '生效中',
      },
    ]);
    mockedScanInventoryAlerts.mockReset();
    mockedScanInventoryAlerts.mockResolvedValue({ created: 1 });
  });

  it('loads alerts, scans inventory and acknowledges active alerts', async () => {
    renderPage();

    expect(await screen.findByText('低库存预警')).toBeInTheDocument();
    expect(screen.getByText('JW-POLO-RED-M')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /扫描库存预警/ }));
    await waitFor(() => expect(mockedScanInventoryAlerts).toHaveBeenCalled());
    await waitFor(() => expect(mockedListInventoryAlerts).toHaveBeenCalledTimes(2));

    fireEvent.click(screen.getByRole('button', { name: '确认预警 90001' }));
    await waitFor(() => expect(mockedAcknowledgeAlert).toHaveBeenCalledWith('90001'));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <InventoryAlertPage />
    </AntdApp>,
  );
}
