import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReceivingManagementPage } from './ReceivingManagementPage';
import {
  confirmReceive,
  createReceivingFromAsn,
  getReceivingDetail,
} from '@/services/warehouse/receivingService';

vi.mock('@/services/warehouse/receivingService', () => ({
  confirmReceive: vi.fn(),
  createReceivingFromAsn: vi.fn(),
  getReceivingDetail: vi.fn(),
}));

const mockedConfirmReceive = vi.mocked(confirmReceive);
const mockedCreateReceivingFromAsn = vi.mocked(createReceivingFromAsn);
const mockedGetReceivingDetail = vi.mocked(getReceivingDetail);

const receivingDetail = {
  id: '10001',
  receivingNo: 'RCV-202605-00001',
  asnId: '81001',
  asnNo: 'ASN-202605-00001',
  warehouseId: '30001',
  warehouseName: '主仓',
  receivingDate: '2026-05-11',
  status: 'IN_PROGRESS',
  statusLabel: '收货中',
  dockNo: 'A-01',
  lines: [
    {
      id: '10002',
      asnLineId: '82001',
      materialId: '80001',
      materialName: '高支棉',
      expectedQty: 120,
      receivedQty: 20,
      rollCount: 1,
      qcStatus: 'PENDING',
      qcStatusLabel: '待检',
      putawayStatus: 'PENDING',
      putawayStatusLabel: '待上架',
    },
  ],
};

describe('ReceivingManagementPage', () => {
  beforeEach(() => {
    mockedConfirmReceive.mockReset();
    mockedConfirmReceive.mockResolvedValue(undefined);
    mockedCreateReceivingFromAsn.mockReset();
    mockedCreateReceivingFromAsn.mockResolvedValue(receivingDetail);
    mockedGetReceivingDetail.mockReset();
    mockedGetReceivingDetail.mockResolvedValue(receivingDetail);
  });

  it('creates a receiving order from ASN and queries detail', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('ASN ID'), { target: { value: ' 81001 ' } });
    fireEvent.change(screen.getByLabelText('仓库 ID'), { target: { value: ' 30001 ' } });
    fireEvent.change(screen.getByLabelText('月台号'), { target: { value: ' A-01 ' } });
    fireEvent.click(screen.getByRole('button', { name: /创建收货单/ }));

    await waitFor(() => expect(mockedCreateReceivingFromAsn).toHaveBeenCalledWith({
      asnId: '81001',
      warehouseId: '30001',
      dockNo: 'A-01',
    }));
    expect(await screen.findByText('RCV-202605-00001')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('收货单 ID'), { target: { value: ' 10001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /查询收货单/ }));

    await waitFor(() => expect(mockedGetReceivingDetail).toHaveBeenCalledWith('10001'));
    expect(screen.getByText('高支棉')).toBeInTheDocument();
  });

  it('confirms receiving quantity for a line', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('收货单 ID'), { target: { value: '10001' } });
    fireEvent.click(screen.getByRole('button', { name: /查询收货单/ }));
    fireEvent.click(await screen.findByRole('button', { name: '确认收货 10002' }));
    fireEvent.change(screen.getByLabelText('本次实收数量'), { target: { value: '80' } });
    fireEvent.change(screen.getByLabelText('实收卷数'), { target: { value: '3' } });
    fireEvent.click(screen.getByRole('button', { name: /提交收货/ }));

    await waitFor(() => expect(mockedConfirmReceive).toHaveBeenCalledWith({
      receivingLineId: '10002',
      receivedQty: 80,
      rollCount: 3,
    }));
  });

  it('validates required receiving quantity before submitting', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('收货单 ID'), { target: { value: '10001' } });
    fireEvent.click(screen.getByRole('button', { name: /查询收货单/ }));
    fireEvent.click(await screen.findByRole('button', { name: '确认收货 10002' }));
    fireEvent.click(screen.getByRole('button', { name: /提交收货/ }));

    expect(await screen.findByText('请输入大于 0 的实收数量')).toBeInTheDocument();
    expect(mockedConfirmReceive).not.toHaveBeenCalled();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ReceivingManagementPage />
    </AntdApp>,
  );
}
