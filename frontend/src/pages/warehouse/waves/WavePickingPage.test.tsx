import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { WavePickingPage } from './WavePickingPage';
import { cancelWave, completePickList, confirmPick, createWave } from '@/services/warehouse/waveService';

vi.mock('@/services/warehouse/waveService', () => ({
  cancelWave: vi.fn(),
  completePickList: vi.fn(),
  confirmPick: vi.fn(),
  createWave: vi.fn(),
}));

const mockedCancelWave = vi.mocked(cancelWave);
const mockedCompletePickList = vi.mocked(completePickList);
const mockedConfirmPick = vi.mocked(confirmPick);
const mockedCreateWave = vi.mocked(createWave);

describe('WavePickingPage', () => {
  beforeEach(() => {
    mockedCancelWave.mockReset();
    mockedCancelWave.mockResolvedValue(undefined);
    mockedCompletePickList.mockReset();
    mockedCompletePickList.mockResolvedValue(undefined);
    mockedConfirmPick.mockReset();
    mockedConfirmPick.mockResolvedValue(undefined);
    mockedCreateWave.mockReset();
    mockedCreateWave.mockResolvedValue('70001');
  });

  it('creates a wave and submits picking operations', async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText('仓库 ID'), { target: { value: '30001' } });
    fireEvent.change(screen.getByLabelText('出库单 ID'), { target: { value: '80001, 80002' } });
    fireEvent.click(screen.getByRole('button', { name: /创建波次/ }));

    await waitFor(() =>
      expect(mockedCreateWave).toHaveBeenCalledWith({
        warehouseId: '30001',
        strategy: 'BY_CUSTOMER',
        outboundOrderIds: ['80001', '80002'],
        remark: undefined,
      }),
    );
    expect(await screen.findByText('新波次 ID：70001')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('拣货项 ID'), { target: { value: '71001' } });
    fireEvent.change(screen.getByLabelText('实拣数量'), { target: { value: '12' } });
    fireEvent.click(screen.getByRole('button', { name: /确认拣货/ }));
    await waitFor(() => expect(mockedConfirmPick).toHaveBeenCalledWith({ pickItemId: '71001', actualQty: 12 }));

    fireEvent.change(screen.getByLabelText('拣货单 ID'), { target: { value: '72001' } });
    fireEvent.click(screen.getByRole('button', { name: /完成拣货单/ }));
    await waitFor(() => expect(mockedCompletePickList).toHaveBeenCalledWith('72001'));

    fireEvent.change(screen.getByLabelText('波次 ID'), { target: { value: '70001' } });
    fireEvent.click(screen.getByRole('button', { name: /取消波次/ }));
    await waitFor(() => expect(mockedCancelWave).toHaveBeenCalledWith('70001'));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <WavePickingPage />
    </AntdApp>,
  );
}
