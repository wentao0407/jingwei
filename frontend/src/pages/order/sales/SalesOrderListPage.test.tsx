import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SalesOrderListPage } from './SalesOrderListPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  cancelSalesOrder,
  convertSalesOrderToProduction,
  createSalesOrder,
  createQuantityChange,
  deleteSalesOrder,
  getSalesOrderDetail,
  getSalesOrderTimeline,
  listQuantityChanges,
  pageSalesOrders,
  resubmitSalesOrder,
  submitSalesOrder,
  updateSalesOrder,
} from '@/services/order/salesOrderService';
import { listCustomers } from '@/services/master/customerService';
import { listSeasons } from '@/services/master/seasonService';
import { listSpus } from '@/services/master/spuService';
import { listSizeGroups } from '@/services/master/sizeGroupService';
import { createReturnOrder } from '@/services/order/returnOrderService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/order/salesOrderService', () => ({
  cancelSalesOrder: vi.fn(),
  convertSalesOrderToProduction: vi.fn(),
  createSalesOrder: vi.fn(),
  createQuantityChange: vi.fn(),
  deleteSalesOrder: vi.fn(),
  getSalesOrderDetail: vi.fn(),
  getSalesOrderTimeline: vi.fn(),
  listQuantityChanges: vi.fn(),
  pageSalesOrders: vi.fn(),
  resubmitSalesOrder: vi.fn(),
  submitSalesOrder: vi.fn(),
  updateSalesOrder: vi.fn(),
}));

vi.mock('@/services/order/returnOrderService', () => ({
  createReturnOrder: vi.fn(),
}));

vi.mock('@/services/master/customerService', () => ({
  listCustomers: vi.fn(),
}));

vi.mock('@/services/master/seasonService', () => ({
  listSeasons: vi.fn(),
}));

vi.mock('@/services/master/spuService', () => ({
  listSpus: vi.fn(),
}));

vi.mock('@/services/master/sizeGroupService', () => ({
  listSizeGroups: vi.fn(),
}));

const mockedCancelSalesOrder = vi.mocked(cancelSalesOrder);
const mockedConvertSalesOrderToProduction = vi.mocked(convertSalesOrderToProduction);
const mockedCreateSalesOrder = vi.mocked(createSalesOrder);
const mockedCreateQuantityChange = vi.mocked(createQuantityChange);
const mockedCreateReturnOrder = vi.mocked(createReturnOrder);
const mockedDeleteSalesOrder = vi.mocked(deleteSalesOrder);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedGetSalesOrderDetail = vi.mocked(getSalesOrderDetail);
const mockedGetSalesOrderTimeline = vi.mocked(getSalesOrderTimeline);
const mockedListQuantityChanges = vi.mocked(listQuantityChanges);
const mockedListCustomers = vi.mocked(listCustomers);
const mockedListSeasons = vi.mocked(listSeasons);
const mockedListSizeGroups = vi.mocked(listSizeGroups);
const mockedListSpus = vi.mocked(listSpus);
const mockedPageSalesOrders = vi.mocked(pageSalesOrders);
const mockedResubmitSalesOrder = vi.mocked(resubmitSalesOrder);
const mockedSubmitSalesOrder = vi.mocked(submitSalesOrder);
const mockedUpdateSalesOrder = vi.mocked(updateSalesOrder);

afterEach(() => {
  cleanup();
  document.body.innerHTML = '';
});

const permissions = [
  'order:sales:create',
  'order:sales:update',
  'order:sales:delete',
  'order:sales:submit',
  'order:sales:resubmit',
  'order:sales:cancel',
  'order:sales:convert',
  'order:sales:quantity-change',
  'order:return:create',
];

const optionData = {
  customers: [
    { id: '20001', code: 'CUS001', name: '上海一店', type: 'DIRECT', status: 'ACTIVE' },
    { id: '20002', code: 'CUS002', name: '杭州渠道', type: 'WHOLESALE', status: 'ACTIVE' },
  ],
  seasons: [
    { id: '30001', code: 'SS26', name: '2026春夏', year: 2026, seasonType: 'SPRING_SUMMER', startDate: '2026-01-01', endDate: '2026-06-30', status: 'ACTIVE' },
  ],
  spus: [
    {
      id: '80001',
      code: 'SP20260001',
      name: '女士风衣',
      sizeGroupId: '60001',
      status: 'ACTIVE',
      colorWays: [
        { id: '70001', spuId: '80001', colorName: '黑色', colorCode: 'BK' },
        { id: '70002', spuId: '80001', colorName: '米色', colorCode: 'BE' },
      ],
    },
  ],
  sizeGroups: [
    {
      id: '60001',
      code: 'WOMEN',
      name: '女装尺码',
      category: 'CLOTHING',
      status: 'ACTIVE',
      sizes: [
        { id: '50001', sizeGroupId: '60001', code: 'S', name: 'S' },
        { id: '50002', sizeGroupId: '60001', code: 'M', name: 'M' },
      ],
    },
  ],
};

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
  {
    id: '10003',
    orderNo: 'SO-202605-00003',
    customerId: '20001',
    customerName: '上海一店',
    seasonId: '30001',
    seasonName: '2026春夏',
    orderDate: '2026-05-05',
    deliveryDate: '2026-06-25',
    status: 'CONFIRMED',
    statusLabel: '已确认',
    totalQuantity: 140,
    totalAmount: 14000,
    discountAmount: 0,
    actualAmount: 14000,
    paymentStatus: 'UNPAID',
    paymentAmount: 0,
    lines: [],
  },
  {
    id: '10004',
    orderNo: 'SO-202605-00004',
    customerId: '20001',
    customerName: '上海一店',
    seasonId: '30001',
    seasonName: '2026春夏',
    orderDate: '2026-05-06',
    deliveryDate: '2026-06-28',
    status: 'SHIPPED',
    statusLabel: '已发货',
    totalQuantity: 60,
    totalAmount: 6000,
    discountAmount: 0,
    actualAmount: 6000,
    paymentStatus: 'PAID',
    paymentAmount: 6000,
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
        sizeGroupId: '60001',
        sizes: [
          { sizeId: '50001', code: 'S', quantity: 40 },
          { sizeId: '50002', code: 'M', quantity: 80 },
        ],
      },
    },
  ],
};

const confirmedDetail = {
  ...salesOrders[2],
  lines: [
    {
      id: '90003',
      lineNo: 1,
      spuId: '80001',
      spuCode: 'SP20260001',
      spuName: '女士风衣',
      colorWayId: '70001',
      colorName: '黑色',
      colorCode: 'BK',
      totalQuantity: 140,
      unitPrice: 100,
      actualAmount: 14000,
      sizeMatrix: {
        sizeGroupId: '60001',
        sizes: [
          { sizeId: '50001', code: 'S', quantity: 50 },
          { sizeId: '50002', code: 'M', quantity: 90 },
        ],
      },
    },
  ],
};

const shippedDetail = {
  ...salesOrders[3],
  lines: [
    {
      id: '90004',
      lineNo: 1,
      spuId: '80001',
      spuCode: 'SP20260001',
      spuName: '女士风衣',
      colorWayId: '70001',
      colorName: '黑色',
      colorCode: 'BK',
      totalQuantity: 60,
      unitPrice: 100,
      actualAmount: 6000,
      sizeMatrix: {
        sizeGroupId: '60001',
        sizes: [
          { sizeId: '50001', code: 'S', quantity: 20 },
          { sizeId: '50002', code: 'M', quantity: 40 },
        ],
      },
    },
  ],
};

describe('SalesOrderListPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setAuthSession({ userId: '1', username: 'admin', realName: '系统管理员', roleIds: ['admin'], permissions, menuTree: [] });
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedCancelSalesOrder.mockReset();
    mockedCancelSalesOrder.mockResolvedValue(undefined);
    mockedConvertSalesOrderToProduction.mockReset();
    mockedConvertSalesOrderToProduction.mockResolvedValue({
      salesOrderId: '10003',
      salesOrderNo: 'SO-202605-00003',
      salesOrderStatus: 'PRODUCING',
      salesOrderStatusLabel: '生产中',
      productionOrders: [{ id: '50001', orderNo: 'MO-202605-00001', status: 'DRAFT', totalQuantity: 140, lines: [] }],
    });
    mockedCreateSalesOrder.mockReset();
    mockedCreateSalesOrder.mockResolvedValue(salesOrders[0]);
    mockedCreateQuantityChange.mockReset();
    mockedCreateQuantityChange.mockResolvedValue({ id: '70001', orderId: '10003', orderLineId: '90003', status: 'PENDING' });
    mockedCreateReturnOrder.mockReset();
    mockedCreateReturnOrder.mockResolvedValue({ id: '71001', returnNo: 'RT-202605-0001', status: 'DRAFT' });
    mockedDeleteSalesOrder.mockReset();
    mockedDeleteSalesOrder.mockResolvedValue(undefined);
    mockedGetSalesOrderDetail.mockReset();
    mockedGetSalesOrderDetail.mockImplementation((orderId) => {
      if (orderId === '10003') {
        return Promise.resolve(confirmedDetail);
      }
      if (orderId === '10004') {
        return Promise.resolve(shippedDetail);
      }
      return Promise.resolve(detail);
    });
    mockedGetSalesOrderTimeline.mockReset();
    mockedGetSalesOrderTimeline.mockResolvedValue([
      { id: 'tl-1', changeType: 'STATUS', fieldName: 'status', changeReason: '提交订单', operatedBy: 'admin' },
    ]);
    mockedListQuantityChanges.mockReset();
    mockedListQuantityChanges.mockResolvedValue([
      { id: 'qc-1', reason: '客户加单', status: 'PENDING', createdBy: 'admin' },
    ]);
    mockedPageSalesOrders.mockReset();
    mockedPageSalesOrders.mockResolvedValue({ current: 1, size: 10, total: 2, pages: 1, records: salesOrders });
    mockedListCustomers.mockReset();
    mockedListCustomers.mockResolvedValue({ current: 1, size: 200, total: 2, pages: 1, records: optionData.customers });
    mockedListSeasons.mockReset();
    mockedListSeasons.mockResolvedValue(optionData.seasons);
    mockedListSizeGroups.mockReset();
    mockedListSizeGroups.mockResolvedValue(optionData.sizeGroups);
    mockedListSpus.mockReset();
    mockedListSpus.mockResolvedValue(optionData.spus);
    mockedResubmitSalesOrder.mockReset();
    mockedResubmitSalesOrder.mockResolvedValue(undefined);
    mockedSubmitSalesOrder.mockReset();
    mockedSubmitSalesOrder.mockResolvedValue(undefined);
    mockedUpdateSalesOrder.mockReset();
    mockedUpdateSalesOrder.mockResolvedValue(salesOrders[0]);
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
    expect(screen.getByText('订单总金额')).toBeInTheDocument();
    expect(screen.getByText('12000.00')).toBeInTheDocument();
    expect(screen.getByText('折扣金额')).toBeInTheDocument();
    expect(screen.getByText('600.00')).toBeInTheDocument();
    expect(screen.getByText('已收金额')).toBeInTheDocument();
    expect(screen.getByText('0.00')).toBeInTheDocument();
    expect(mockedGetSalesOrderDetail).toHaveBeenCalledWith('10001');
  });

  it('opens draft edit form from the detail dialog', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 SO-202605-00001' }));
    await screen.findByText('女士风衣');
    fireEvent.click(screen.getByRole('button', { name: '详情编辑 SO-202605-00001' }));

    expect(await screen.findByRole('dialog', { name: '编辑销售订单' })).toBeInTheDocument();
    expect(mockedGetSalesOrderDetail).toHaveBeenCalledTimes(2);
  });

  it('validates date filters before querying', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.change(screen.getByPlaceholderText('订单开始日期'), { target: { value: '2026/05/01' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    expect(await screen.findByText('订单日期格式必须为 YYYY-MM-DD')).toBeInTheDocument();
    expect(mockedPageSalesOrders).toHaveBeenCalledTimes(1);
  });

  it('creates a sales order with line size quantities', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '新建订单' }));
    await screen.findByRole('dialog', { name: '新建销售订单' });

    openDialogSelect('客户');
    await chooseOption('上海一店');
    openDialogSelect('季节');
    await chooseOption('2026春夏');
    fireEvent.change(screen.getByLabelText('订单日期'), { target: { value: '2026-05-10' } });
    fireEvent.change(screen.getByLabelText('整单交期'), { target: { value: '2026-06-10' } });
    openDialogSelect('款式');
    await chooseOption('SP20260001 女士风衣');
    openDialogSelect('颜色');
    await chooseOption('黑色 / BK');
    fireEvent.change(screen.getByLabelText('尺码 S'), { target: { value: '20' } });
    fireEvent.change(screen.getByLabelText('尺码 M'), { target: { value: '30' } });
    fireEvent.change(screen.getByLabelText('单价'), { target: { value: '129' } });
    fireEvent.change(screen.getByLabelText('折扣率'), { target: { value: '0.95' } });
    fireEvent.click(screen.getByRole('button', { name: '保存订单' }));

    await waitFor(() =>
      expect(mockedCreateSalesOrder).toHaveBeenCalledWith({
        customerId: '20001',
        seasonId: '30001',
        orderDate: '2026-05-10',
        deliveryDate: '2026-06-10',
        lines: [
          {
            spuId: '80001',
            colorWayId: '70001',
            sizeGroupId: '60001',
            sizes: [
              { sizeId: '50001', code: 'S', quantity: 20 },
              { sizeId: '50002', code: 'M', quantity: 30 },
            ],
            unitPrice: 129,
            discountRate: 0.95,
          },
        ],
      }),
    );
    expect(mockedPageSalesOrders).toHaveBeenCalledTimes(2);
  });

  it('updates a draft sales order from its detail data', async () => {
    renderPage();

    await screen.findByText('SO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '编辑 SO-202605-00001' }));
    await screen.findByRole('dialog', { name: '编辑销售订单' });
    fireEvent.change(screen.getByLabelText('整单交期'), { target: { value: '2026-06-20' } });
    fireEvent.change(screen.getByLabelText('尺码 S'), { target: { value: '45' } });
    fireEvent.click(screen.getByRole('button', { name: '保存订单' }));

    await waitFor(() =>
      expect(mockedUpdateSalesOrder).toHaveBeenCalledWith('10001', expect.objectContaining({
        customerId: '20001',
        seasonId: '30001',
        deliveryDate: '2026-06-20',
        lines: [
          expect.objectContaining({
            spuId: '80001',
            colorWayId: '70001',
            sizeGroupId: '60001',
            sizes: [
              { sizeId: '50001', code: 'S', quantity: 45 },
              { sizeId: '50002', code: 'M', quantity: 80 },
            ],
          }),
        ],
      })),
    );
  });

  it('converts a confirmed sales order to production', async () => {
    renderPage();

    await screen.findByText('SO-202605-00003');
    fireEvent.click(screen.getByRole('button', { name: '生成生产 SO-202605-00003' }));
    await screen.findByText('生成生产订单');
    fireEvent.click(screen.getByLabelText('转生产行 90003'));
    fireEvent.change(screen.getByLabelText('要求完工日期'), { target: { value: '2026-07-15' } });
    fireEvent.change(getLastByLabelText('转生产备注'), { target: { value: ' 春季订单排产 ' } });
    fireEvent.click(screen.getByRole('button', { name: '确认生成生产订单' }));

    await waitFor(() =>
      expect(mockedConvertSalesOrderToProduction).toHaveBeenCalledWith({
        salesOrderId: '10003',
        lines: [{ salesOrderLineId: '90003', skipCutting: false }],
        deadlineDate: '2026-07-15',
        remark: '春季订单排产',
      }),
    );
    expect(mockedPageSalesOrders).toHaveBeenCalledTimes(2);
  });

  it('creates a quantity change for a confirmed sales order', async () => {
    renderPage();

    await screen.findByText('SO-202605-00003');
    fireEvent.click(screen.getByRole('button', { name: '数量变更 SO-202605-00003' }));
    await screen.findByText('创建数量变更');
    fireEvent.change(screen.getByLabelText('变更尺码 S'), { target: { value: '55' } });
    fireEvent.change(screen.getByLabelText('变更尺码 M'), { target: { value: '95' } });
    fireEvent.change(screen.getByLabelText('变更原因'), { target: { value: ' 客户追加数量 ' } });
    fireEvent.click(screen.getByRole('button', { name: '提交数量变更' }));

    await waitFor(() =>
      expect(mockedCreateQuantityChange).toHaveBeenCalledWith({
        orderId: '10003',
        orderLineId: '90003',
        sizeGroupId: '60001',
        sizes: [
          { sizeId: '50001', code: 'S', quantity: 55 },
          { sizeId: '50002', code: 'M', quantity: 95 },
        ],
        reason: '客户追加数量',
      }),
    );
    expect(mockedPageSalesOrders).toHaveBeenCalledTimes(2);
  });

  it('creates a return order from a shipped sales order', async () => {
    renderPage();

    await screen.findByText('SO-202605-00004');
    fireEvent.click(screen.getByRole('button', { name: '创建退货 SO-202605-00004' }));
    await screen.findByText('创建退货单');
    fireEvent.click(screen.getByLabelText('退货行 90004'));
    fireEvent.change(screen.getByLabelText('退货尺码 S'), { target: { value: '3' } });
    fireEvent.change(screen.getByLabelText('退货尺码 M'), { target: { value: '2' } });
    fireEvent.change(screen.getByLabelText('退货原因'), { target: { value: ' 尺码问题 ' } });
    fireEvent.click(screen.getByRole('button', { name: '提交退货单' }));

    await waitFor(() =>
      expect(mockedCreateReturnOrder).toHaveBeenCalledWith({
        returnType: 'CUSTOMER_REJECT',
        salesOrderId: '10004',
        salesOrderNo: 'SO-202605-00004',
        customerId: '20001',
        reason: '尺码问题',
        lines: [
          {
            salesOrderLineId: '90004',
            spuId: '80001',
            colorWayId: '70001',
            sizeMatrixJson: JSON.stringify({
              sizeGroupId: '60001',
              sizes: [
                { sizeId: '50001', code: 'S', quantity: 3 },
                { sizeId: '50002', code: 'M', quantity: 2 },
              ],
              totalQuantity: 5,
            }),
            totalQuantity: 5,
          },
        ],
      }),
    );
    expect(mockedPageSalesOrders).toHaveBeenCalledTimes(2);
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
    expect(screen.queryByRole('button', { name: '新建订单' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 SO-202605-00001' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '生成生产 SO-202605-00003' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '数量变更 SO-202605-00003' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '创建退货 SO-202605-00004' })).not.toBeInTheDocument();
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

function openDialogSelect(label: string) {
  const select = screen.getAllByLabelText(label).find((element) => element.classList.contains('ant-select')) ?? screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}

function getLastByLabelText(label: string): HTMLElement {
  return screen.getAllByLabelText(label).at(-1)!;
}
