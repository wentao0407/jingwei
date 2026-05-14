import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PutawayManagementPage } from './PutawayManagementPage';
import {
  confirmPutaway,
  getReceivingDetail,
  suggestReceivingLocations,
} from '@/services/warehouse/receivingService';

vi.mock('@/services/warehouse/receivingService', () => ({
  confirmPutaway: vi.fn(),
  getReceivingDetail: vi.fn(),
  suggestReceivingLocations: vi.fn(),
}));

const mockedConfirmPutaway = vi.mocked(confirmPutaway);
const mockedGetReceivingDetail = vi.mocked(getReceivingDetail);
const mockedSuggestReceivingLocations = vi.mocked(suggestReceivingLocations);

const receivingDetail = {
  id: '10001',
  receivingNo: 'RCV-202605-00001',
  warehouseName: '主仓',
  status: 'COMPLETED',
  statusLabel: '已收货',
  lines: [
    {
      id: '10002',
      materialName: '高支棉',
      expectedQty: 120,
      receivedQty: 120,
      putawayStatus: 'PENDING',
      putawayStatusLabel: '待上架',
    },
  ],
};

describe('PutawayManagementPage', () => {
  beforeEach(() => {
    mockedConfirmPutaway.mockReset();
    mockedConfirmPutaway.mockResolvedValue(undefined);
    mockedGetReceivingDetail.mockReset();
    mockedGetReceivingDetail.mockResolvedValue(receivingDetail);
    mockedSuggestReceivingLocations.mockReset();
    mockedSuggestReceivingLocations.mockResolvedValue([
      {
        locationId: '50001',
        fullCode: 'A-01-01',
        locationType: 'STORAGE',
        capacity: 500,
        usedCapacity: 120,
        remainingCapacity: 380,
      },
    ]);
  });

  it('queries receiving detail and suggests locations for putaway', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('收货单 ID'), { target: { value: ' 10001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /查询待上架明细/ }));

    await waitFor(() => expect(mockedGetReceivingDetail).toHaveBeenCalledWith('10001'));
    expect(await screen.findByText('RCV-202605-00001')).toBeInTheDocument();
    expect(screen.getByText('高支棉')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '上架 10002' }));

    await waitFor(() => expect(mockedSuggestReceivingLocations).toHaveBeenCalledWith('10002'));
    expect(await screen.findByText('A-01-01')).toBeInTheDocument();
  });

  it('confirms putaway with selected location', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('收货单 ID'), { target: { value: '10001' } });
    fireEvent.click(screen.getByRole('button', { name: /查询待上架明细/ }));
    fireEvent.click(await screen.findByRole('button', { name: '上架 10002' }));

    const dialog = await screen.findByRole('dialog');
    fireEvent.click(within(dialog).getByLabelText('A-01-01'));
    fireEvent.click(screen.getByRole('button', { name: /确认上架/ }));

    await waitFor(() => expect(mockedConfirmPutaway).toHaveBeenCalledWith({
      receivingLineId: '10002',
      locationId: '50001',
    }));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <PutawayManagementPage />
    </AntdApp>,
  );
}
