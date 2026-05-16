import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AsnManagementPage } from './AsnManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { createAsn, getAsnDetail, pageAsns, receiveAsnGoods, submitAsnQc } from '@/services/procurement/procurementService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/procurement/procurementService', () => ({
  createAsn: vi.fn(),
  getAsnDetail: vi.fn(),
  pageAsns: vi.fn(),
  receiveAsnGoods: vi.fn(),
  submitAsnQc: vi.fn(),
}));

const mockedCreateAsn = vi.mocked(createAsn);
const mockedGetAsnDetail = vi.mocked(getAsnDetail);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedPageAsns = vi.mocked(pageAsns);
const mockedReceiveAsnGoods = vi.mocked(receiveAsnGoods);
const mockedSubmitAsnQc = vi.mocked(submitAsnQc);

const permissions = ['procurement:asn:create', 'procurement:asn:receive', 'procurement:asn:qc'];

const asns = [
  {
    id: '81001',
    asnNo: 'ASN-202605-00001',
    procurementOrderId: '70001',
    procurementOrderNo: 'PO-202605-00001',
    supplierId: '90001',
    supplierName: '经纬纺织',
    expectedArrivalDate: '2026-05-18',
    status: 'PENDING',
    statusLabel: '待到货',
    lines: [],
  },
];

const asnDetail = {
  ...asns[0],
  lines: [
    {
      id: '82001',
      asnId: '81001',
      procurementLineId: '71001',
      materialId: '80001',
      materialCode: 'FAB-001',
      materialName: '高支棉',
      expectedQuantity: 120,
      receivedQuantity: 0,
      acceptedQuantity: 0,
      rejectedQuantity: 0,
      qcStatus: 'PENDING',
      qcStatusLabel: '待检',
      batchNo: 'B20260511001',
    },
  ],
};

describe('AsnManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setAuthSession({ userId: '1', username: 'admin', realName: '系统管理员', roleIds: ['1'], permissions, menuTree: [] });
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ permissions, menuTree: [] });
    mockedPageAsns.mockReset();
    mockedPageAsns.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: asns });
    mockedCreateAsn.mockReset();
    mockedCreateAsn.mockResolvedValue({ ...asns[0], id: '81002', asnNo: 'ASN-202605-00002' });
    mockedGetAsnDetail.mockReset();
    mockedGetAsnDetail.mockResolvedValue(asnDetail);
    mockedReceiveAsnGoods.mockReset();
    mockedReceiveAsnGoods.mockResolvedValue(undefined);
    mockedSubmitAsnQc.mockReset();
    mockedSubmitAsnQc.mockResolvedValue(undefined);
  });

  it('creates ASN with manual lines', async () => {
    renderPage();

    await screen.findByText('ASN-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '新增ASN' }));
    fireEvent.change(screen.getByLabelText('采购订单ID'), { target: { value: ' 70001 ' } });
    fireEvent.change(screen.getByLabelText('供应商ID'), { target: { value: ' 90001 ' } });
    fireEvent.change(screen.getByLabelText('预计到货日期'), { target: { value: ' 2026-05-25 ' } });
    fireEvent.change(screen.getByLabelText('采购订单行ID'), { target: { value: '71001' } });
    fireEvent.change(screen.getByLabelText('物料ID'), { target: { value: '80001' } });
    fireEvent.change(screen.getByLabelText('预计到货数量'), { target: { value: '120' } });
    fireEvent.change(screen.getByLabelText('批次号'), { target: { value: ' B-01 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存ASN' }));

    await waitFor(() => expect(mockedCreateAsn).toHaveBeenCalledWith({
      procurementOrderId: '70001',
      supplierId: '90001',
      expectedArrivalDate: '2026-05-25',
      lines: [
        {
          procurementLineId: '71001',
          materialId: '80001',
          expectedQuantity: 120,
          batchNo: 'B-01',
        },
      ],
    }));
  });

  it('loads filters and opens ASN detail', async () => {
    renderPage();

    expect(screen.getByText('正在加载到货通知')).toBeInTheDocument();
    expect(await screen.findByText('ASN-202605-00001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('采购订单ID'), { target: { value: ' 70001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    await waitFor(() => expect(mockedPageAsns).toHaveBeenLastCalledWith(expect.objectContaining({ procurementOrderId: '70001' })));

    fireEvent.click(screen.getByRole('button', { name: '详情 ASN-202605-00001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    expect(screen.getByText('B20260511001')).toBeInTheDocument();
  });

  it('receives goods and submits qc result', async () => {
    renderPage();

    await screen.findByText('ASN-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 ASN-202605-00001' }));
    fireEvent.click(await screen.findByRole('button', { name: '收货 82001' }));
    fireEvent.change(screen.getByLabelText('实收数量'), { target: { value: '118' } });
    fireEvent.click(screen.getByRole('button', { name: /确认收货/ }));

    await waitFor(() => expect(mockedReceiveAsnGoods).toHaveBeenCalledWith({
      asnId: '81001',
      lines: [{ lineId: '82001', receivedQuantity: 118 }],
    }));

    fireEvent.click(await screen.findByRole('button', { name: '质检 82001' }));
    fireEvent.change(screen.getByLabelText('合格数量'), { target: { value: '116' } });
    fireEvent.change(screen.getByLabelText('不合格数量'), { target: { value: '2' } });
    fireEvent.change(screen.getByLabelText('检验人'), { target: { value: ' QA ' } });
    fireEvent.click(screen.getByRole('button', { name: /提交质检/ }));

    await waitFor(() => expect(mockedSubmitAsnQc).toHaveBeenCalledWith(expect.objectContaining({
      lineId: '82001',
      acceptedQuantity: 116,
      rejectedQuantity: 2,
      inspector: 'QA',
    })));
  });

  it('validates receive and qc quantities before submitting', async () => {
    renderPage();

    await screen.findByText('ASN-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 ASN-202605-00001' }));
    fireEvent.click(await screen.findByRole('button', { name: '收货 82001' }));
    fireEvent.click(screen.getByRole('button', { name: /确认收货/ }));

    expect(await screen.findByText('请输入大于 0 的实收数量')).toBeInTheDocument();
    expect(mockedReceiveAsnGoods).not.toHaveBeenCalled();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <AsnManagementPage />
    </AntdApp>,
  );
}
