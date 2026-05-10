import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SalesOrderListPage } from './SalesOrderListPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  cancelSalesOrder,
  deleteSalesOrder,
  getSalesOrderDetail,
  pageSalesOrders,
  resubmitSalesOrder,
  submitSalesOrder,
} from '@/services/order/salesOrderService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/order/salesOrderService', () => ({
  cancelSalesOrder: vi.fn(),
  deleteSalesOrder: vi.fn(),
  getSalesOrderDetail: vi.fn(),
  pageSalesOrders: vi.fn(),
  resubmitSalesOrder: vi.fn(),
  submitSalesOrder: vi.fn(),
}));

const mockedCancelSalesOrder = vi.mocked(cancelSalesOrder);
const mockedDeleteSalesOrder = vi.mocked(deleteSalesOrder);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedGetSalesOrderDetail = vi.mocked(getSalesOrderDetail);
const mockedPageSalesOrders = vi.mocked(pageSalesOrders);
const mockedResubmitSalesOrder = vi.mocked(resubmitSalesOrder);
const mockedSubmitSalesOrder = vi.mocked(submitSalesOrder);

const permissions = [
  'order:sales:create',
  'order:sales:update',
  'order:sales:delete',
  'order:sales:submit',
  'order:sales:resubmit',
  'order:sales:cancel',
];

const salesOrders = [
  {
    id: '10001',
    orderNo: 'SO-202605-00001',
    customerId: '20001',
    customerName: '上海一店',
    customerLevel: 'A',
    seasonId: '30001',
    seasonName: '2026春夏',
    orderDate: '2026-05-01',
    deliveryDate: '2026-06-01',
    status: 'DRAFT',
    statusLabel: '草稿',
    totalQuantity: 120,
    totalAmount: 12000,
    discountAmount: 600,
    actualAmount: 11400,
    paymentStatus: 'UNPAID',
    paymentAmount: 0,
    remark: '首批订单',
    lines: [],
    createdAt: '2026-05-01T10:00:00',
    updatedAt: '2026-05-01T10:00:00',
  },
  {
    id: '10002',
    orderNo: 'SO-202605-00002',
    customerId: '20002',
    customerName: '杭州渠道',
    orderDate: '2026-05-03',
    status: 'REJECTED',
    statusLabel: '已驳回',
    totalQuantity: 50,
    actualAmount: 5000,
    paymentStatus: 'PARTIAL',
    paymentAmount: 1000,
    lines: [],
  },
];

const detail = {
  ...salesOrders[0],
  lines: [
    {
      id: '90001',
      lineNo: 1,
      spuId: '80001',
      spuCode: 'SP20260001',
      spuName: '女士风衣',
      colorWayId: '70001',
      colorName: '黑色',
      colorCode: 'BK',
      totalQuantity: 120,
      unitPrice: 100,
      actualAmount: 11400,
      sizeMatrix: {
        sizes: [
          { sizeId: '1', code: 'S', quantity: 40 },
          { sizeId: '2', code: 'M', quantity: 80 },
        ],
      },
    },
  ],
};

describe('SalesOrderListPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedCancelSalesOrder.mockReset();
    mockedCancelSalesOrder.mockResolvedValue(undefined);
    mockedDeleteSalesOrder.mockReset();
    mockedDeleteSalesOrder.mockResolvedValue(undefined);
    mockedGetSalesOrderDetail.mockReset();
    mockedGetSalesOrderDetail.mockResolvedValue(detail);
    mockedPageSalesOrders.mockReset();
    mockedPageSalesOrders.mockResolvedValue({ current: 1, size: 10, total: 2, pages: 1, records: salesOrders });
    mockedResubmitSalesOrder.mockReset();
    mockedResubmitSalesOrder.mockResolvedValue(undefined);
    mockedSubmitSalesOrder.mockReset();
    mockedSubmitSalesOrder.mockResolvedValue(undefined);
  });

  it('loads and filters sales orders', async () => {
    renderPage();

    expect(screen.getByText('正在加载销售订单')).toBeInTheDocument();
    expect(await screen.findByText('SO-202605-00001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('搜索订单编号'), { target: { value: ' SO-202605 ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));
    openSelect('订单状态筛选');
    await chooseOption('草稿');

    await waitFor(() =>
      expect(mockedPageSalesOrders).toHaveBeenLastCalledWith(expect.objectContaining({ orderNo: 'SO-202605', status: 'DRAFT' })),
    );
  });

  it('shows detail and size matrix', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 SO-202605-00001' }));

    expect(await screen.findByText('女士风衣')).toBeInTheDocument();
    expect(screen.getByText('S: 40')).toBeInTheDocument();
    expect(mockedGetSalesOrderDetail).toHaveBeenCalledWith('10001');
  });

  it('validates date filters before querying', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.change(screen.getByPlaceholderText('订单开始日期'), { target: { value: '2026/05/01' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    expect(await screen.findByText('订单日期格式必须为 YYYY-MM-DD')).toBeInTheDocument();
    expect(mockedPageSalesOrders).toHaveBeenCalledTimes(1);
  });

  it('runs draft and rejected order actions', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '提交 SO-202605-00001' }));
    await waitFor(() => expect(mockedSubmitSalesOrder).toHaveBeenCalledWith('10001'));

    fireEvent.click(screen.getByRole('button', { name: '取消 SO-202605-00001' }));
    fireEvent.click(screen.getByRole('button', { name: '确认取消' }));
    await waitFor(() => expect(mockedCancelSalesOrder).toHaveBeenCalledWith('10001'));

    fireEvent.click(screen.getByRole('button', { name: '删除 SO-202605-00001' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(mockedDeleteSalesOrder).toHaveBeenCalledWith('10001'));

    fireEvent.click(screen.getByRole('button', { name: '重新提交 SO-202605-00002' }));
    await waitFor(() => expect(mockedResubmitSalesOrder).toHaveBeenCalledWith('10002'));
  });

  it('hides action buttons without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });

    renderPage();

    await screen.findByText('SO-202605-00001');
    expect(screen.queryByRole('button', { name: '提交 SO-202605-00001' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '删除 SO-202605-00001' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <SalesOrderListPage />
    </AntdApp>,
  );
}

function openSelect(label: string) {
  const select = screen.getAllByLabelText(label).find((element) => element.classList.contains('ant-select')) ?? screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
