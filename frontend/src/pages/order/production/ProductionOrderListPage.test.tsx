import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ProductionOrderListPage } from './ProductionOrderListPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  fireProductionLineEvent,
  fireProductionOrderEvent,
  getProductionLineAvailableActions,
  getProductionOrderAvailableActions,
  getProductionOrderDetail,
  pageProductionOrders,
} from '@/services/order/productionOrderService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/order/productionOrderService', () => ({
  fireProductionLineEvent: vi.fn(),
  fireProductionOrderEvent: vi.fn(),
  getProductionLineAvailableActions: vi.fn(),
  getProductionOrderAvailableActions: vi.fn(),
  getProductionOrderDetail: vi.fn(),
  pageProductionOrders: vi.fn(),
}));

const mockedFireProductionLineEvent = vi.mocked(fireProductionLineEvent);
const mockedFireProductionOrderEvent = vi.mocked(fireProductionOrderEvent);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedGetProductionLineAvailableActions = vi.mocked(getProductionLineAvailableActions);
const mockedGetProductionOrderAvailableActions = vi.mocked(getProductionOrderAvailableActions);
const mockedGetProductionOrderDetail = vi.mocked(getProductionOrderDetail);
const mockedPageProductionOrders = vi.mocked(pageProductionOrders);

const permissions = ['order:production:fire-event', 'order:production:fire-line-event'];

const productionOrders = [
  {
    id: '50001',
    orderNo: 'MO-202605-00001',
    planDate: '2026-05-12',
    deadlineDate: '2026-06-01',
    status: 'DRAFT',
    statusLabel: '草稿',
    sourceType: 'SALES_ORDER',
    totalQuantity: 140,
    completedQuantity: 0,
    stockedQuantity: 0,
    lines: [],
  },
];

const productionDetail = {
  ...productionOrders[0],
  lines: [
    {
      id: '60001',
      lineNo: 1,
      spuId: '80001',
      spuCode: 'SP20260001',
      spuName: '女士风衣',
      colorWayId: '70001',
      colorName: '黑色',
      colorCode: 'BK',
      bomId: '91001',
      totalQuantity: 140,
      completedQuantity: 0,
      stockedQuantity: 0,
      skipCutting: false,
      status: 'PLANNED',
      statusLabel: '已排产',
      sizeMatrix: {
        sizes: [
          { sizeId: '50001', code: 'S', quantity: 50 },
          { sizeId: '50002', code: 'M', quantity: 90 },
        ],
      },
    },
  ],
};

afterEach(() => {
  cleanup();
  document.body.innerHTML = '';
});

describe('ProductionOrderListPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setAuthSession({ userId: '1', username: 'admin', realName: '系统管理员', roleIds: ['1'], permissions, menuTree: [] });
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ permissions, menuTree: [] });
    mockedPageProductionOrders.mockReset();
    mockedPageProductionOrders.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: productionOrders });
    mockedGetProductionOrderDetail.mockReset();
    mockedGetProductionOrderDetail.mockResolvedValue(productionDetail);
    mockedGetProductionOrderAvailableActions.mockReset();
    mockedGetProductionOrderAvailableActions.mockResolvedValue([{ event: 'RELEASE', label: '下达生产订单', targetStatus: 'RELEASED' }]);
    mockedGetProductionLineAvailableActions.mockReset();
    mockedGetProductionLineAvailableActions.mockResolvedValue([{ event: 'START_CUTTING', label: '开始裁剪', targetStatus: 'CUTTING' }]);
    mockedFireProductionOrderEvent.mockReset();
    mockedFireProductionOrderEvent.mockResolvedValue(undefined);
    mockedFireProductionLineEvent.mockReset();
    mockedFireProductionLineEvent.mockResolvedValue(undefined);
  });

  it('loads and filters production orders', async () => {
    renderPage();

    expect(screen.getByText('正在加载生产订单')).toBeInTheDocument();
    expect(await screen.findByText('MO-202605-00001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('搜索生产单号'), { target: { value: ' MO-202605 ' } });
    fireEvent.change(screen.getByPlaceholderText('计划开始日期'), { target: { value: '2026-05-01' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    await waitFor(() =>
      expect(mockedPageProductionOrders).toHaveBeenLastCalledWith(expect.objectContaining({
        orderNo: 'MO-202605',
        planDateStart: '2026-05-01',
      })),
    );
  });

  it('shows detail and fires order and line actions', async () => {
    renderPage();

    await screen.findByText('MO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 MO-202605-00001' }));

    expect(await screen.findByText('女士风衣')).toBeInTheDocument();
    expect(screen.getByText('S: 50')).toBeInTheDocument();
    fireEvent.click(await screen.findByRole('button', { name: '下达生产订单' }));
    await waitFor(() => expect(mockedFireProductionOrderEvent).toHaveBeenCalledWith({ orderId: '50001', event: 'RELEASE' }));

    fireEvent.click(await screen.findByRole('button', { name: '开始裁剪 60001' }));
    await waitFor(() => expect(mockedFireProductionLineEvent).toHaveBeenCalledWith({
      orderId: '50001',
      lineId: '60001',
      event: 'START_CUTTING',
    }));
  });

  it('validates plan date filters before querying', async () => {
    renderPage();

    await screen.findByText('MO-202605-00001');
    fireEvent.change(screen.getByPlaceholderText('计划开始日期'), { target: { value: '2026/05/01' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    expect(await screen.findByText('计划日期格式必须为 YYYY-MM-DD')).toBeInTheDocument();
    expect(mockedPageProductionOrders).toHaveBeenCalledTimes(1);
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ProductionOrderListPage />
    </AntdApp>,
  );
}
