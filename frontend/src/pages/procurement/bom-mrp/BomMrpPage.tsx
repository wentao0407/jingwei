import { CheckOutlined, EyeOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Input, Modal, Select, Space, Table, Tabs, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  approveBom,
  calculateMrp,
  getBomDetail,
  pageBoms,
  pageMrpResults,
  type BomItemRecord,
  type BomRecord,
  type MrpResultRecord,
} from '@/services/procurement/procurementService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;

const bomStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已生效', value: 'APPROVED' },
  { label: '已作废', value: 'OBSOLETE' },
];

const mrpStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已转采购', value: 'CONVERTED' },
  { label: '已忽略', value: 'IGNORED' },
  { label: '已过期', value: 'EXPIRED' },
];

export function BomMrpPage() {
  const { message } = App.useApp();
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = {
    canApproveBom: permissions.includes('procurement:bom:approve'),
    canCalculateMrp: permissions.includes('procurement:mrp:calculate'),
  };

  useEffect(() => {
    void refreshPermissions(setPermissions);
  }, []);

  return (
    <div className="system-page bom-mrp-page">
      <section className="system-page-topbar">
        <div>
          <h1>BOM 与 MRP</h1>
          <p>维护生产物料清单，查看 MRP 物料需求和采购建议。</p>
        </div>
      </section>
      <ProCard className="system-page-card" bordered={false}>
        <Tabs
          items={[
            { key: 'bom', label: 'BOM清单', children: <BomPanel actions={actions} messageApi={message} /> },
            { key: 'mrp', label: 'MRP结果', children: <MrpPanel actions={actions} messageApi={message} /> },
          ]}
        />
      </ProCard>
    </div>
  );
}

function BomPanel({
  actions,
  messageApi,
}: {
  actions: { canApproveBom: boolean; canCalculateMrp: boolean };
  messageApi: ReturnType<typeof App.useApp>['message'];
}) {
  const [spuInput, setSpuInput] = useState('');
  const [spuId, setSpuId] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<BomRecord> | null>(null);
  const [detail, setDetail] = useState<BomRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const loadBoms = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageBoms({
        current: currentPage,
        size: pageSize,
        ...(spuId ? { spuId } : {}),
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, spuId, status]);

  useEffect(() => {
    void loadBoms();
  }, [loadBoms]);

  async function openDetail(record: BomRecord) {
    try {
      setDetail(await getBomDetail(record.id));
      setDetailOpen(true);
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  async function handleApprove(record: BomRecord) {
    try {
      await approveBom(record.id);
      messageApi.success('审批BOM成功');
      await loadBoms();
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载BOM" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadBoms} />;
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space className="system-page-toolbar" wrap>
        <Input placeholder="款式ID" value={spuInput} onChange={(event) => setSpuInput(event.target.value)} />
        <Select aria-label="BOM状态筛选" options={bomStatusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
        <Button icon={<SearchOutlined />} onClick={() => { setSpuId(spuInput.trim()); setCurrentPage(INITIAL_PAGE); }}>搜索BOM</Button>
        <Button icon={<ReloadOutlined />} onClick={loadBoms}>刷新</Button>
      </Space>
      {pageResult?.records.length === 0 ? (
        <EmptyState message="暂无BOM" />
      ) : (
        <Table<BomRecord>
          rowKey="id"
          columns={buildBomColumns(actions, openDetail, handleApprove)}
          dataSource={pageResult?.records ?? []}
          loading={loading}
          pagination={{
            current: Number(pageResult?.current ?? currentPage),
            pageSize: Number(pageResult?.size ?? pageSize),
            total: Number(pageResult?.total ?? 0),
            showSizeChanger: true,
          }}
          onChange={(pagination: TablePaginationConfig) => {
            setCurrentPage(pagination.current ?? INITIAL_PAGE);
            setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
          }}
        />
      )}
      <BomDetailModal bom={detail} open={detailOpen} onCancel={() => setDetailOpen(false)} />
    </Space>
  );
}

function MrpPanel({
  actions,
  messageApi,
}: {
  actions: { canApproveBom: boolean; canCalculateMrp: boolean };
  messageApi: ReturnType<typeof App.useApp>['message'];
}) {
  const [batchInput, setBatchInput] = useState('');
  const [batchNo, setBatchNo] = useState('');
  const [status, setStatus] = useState('');
  const [productionOrderInput, setProductionOrderInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [pageResult, setPageResult] = useState<PageResult<MrpResultRecord> | null>(null);
  const [errorMessage, setErrorMessage] = useState('');

  const loadResults = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageMrpResults({
        current: INITIAL_PAGE,
        size: DEFAULT_PAGE_SIZE,
        ...(batchNo ? { batchNo } : {}),
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [batchNo, status]);

  useEffect(() => {
    void loadResults();
  }, [loadResults]);

  async function handleCalculate() {
    try {
      const productionOrderIds = productionOrderInput.split(',').map((id) => id.trim()).filter(Boolean);
      const result = await calculateMrp({ productionOrderIds });
      messageApi.success(`MRP计算完成：${result.batchNo}`);
      setBatchNo(result.batchNo);
      setBatchInput(result.batchNo);
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载MRP结果" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadResults} />;
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space className="system-page-toolbar" wrap>
        <Input placeholder="MRP批次号" value={batchInput} onChange={(event) => setBatchInput(event.target.value)} />
        <Select aria-label="MRP状态筛选" options={mrpStatusOptions} value={status} onChange={setStatus} />
        <Button icon={<SearchOutlined />} onClick={() => setBatchNo(batchInput.trim())}>搜索MRP</Button>
        {actions.canCalculateMrp ? (
          <>
            <Input placeholder="生产订单ID，多个用逗号分隔" value={productionOrderInput} onChange={(event) => setProductionOrderInput(event.target.value)} />
            <Button type="primary" onClick={handleCalculate}>执行MRP计算</Button>
          </>
        ) : null}
      </Space>
      <Table<MrpResultRecord>
        rowKey="id"
        columns={mrpColumns}
        dataSource={pageResult?.records ?? []}
        loading={loading}
        pagination={false}
      />
    </Space>
  );
}

function buildBomColumns(
  actions: { canApproveBom: boolean },
  onDetail: (record: BomRecord) => void,
  onApprove: (record: BomRecord) => void,
): ColumnsType<BomRecord> {
  return [
    { title: 'BOM编码', dataIndex: 'code', key: 'code', width: 150 },
    { title: '款式', key: 'spu', render: (_, record) => record.spuName || record.spuCode || '-' },
    { title: '版本', dataIndex: 'bomVersion', key: 'bomVersion', width: 80, render: (value) => value ?? '-' },
    { title: '状态', key: 'status', width: 110, render: (_, record) => <Tag>{record.statusLabel || record.status || '-'}</Tag> },
    {
      title: '操作',
      key: 'actions',
      width: 180,
      render: (_, record) => (
        <Space wrap>
          <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.code}`} onClick={() => onDetail(record)}>详情</Button>
          {actions.canApproveBom ? (
            <Button icon={<CheckOutlined />} size="small" aria-label={`审批 ${record.code}`} onClick={() => onApprove(record)}>审批</Button>
          ) : null}
        </Space>
      ),
    },
  ];
}

function BomDetailModal({ bom, open, onCancel }: { bom: BomRecord | null; open: boolean; onCancel: () => void }) {
  return (
    <Modal footer={null} getContainer={false} onCancel={onCancel} open={open} title={bom?.code ?? 'BOM详情'} width={900}>
      {bom ? (
        <Space direction="vertical" style={{ width: '100%' }}>
          <Descriptions column={3} size="small">
            <Descriptions.Item label="BOM编码">{bom.code}</Descriptions.Item>
            <Descriptions.Item label="款式">{bom.spuName || bom.spuCode || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{bom.statusLabel || bom.status || '-'}</Descriptions.Item>
          </Descriptions>
          <Table<BomItemRecord> rowKey="id" size="small" pagination={false} columns={bomItemColumns} dataSource={bom.items ?? []} />
        </Space>
      ) : null}
    </Modal>
  );
}

const bomItemColumns: ColumnsType<BomItemRecord> = [
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
  { title: '消耗类型', dataIndex: 'consumptionTypeLabel', key: 'consumptionTypeLabel', width: 120, render: (value) => value || '-' },
  { title: '基准用量', dataIndex: 'baseConsumption', key: 'baseConsumption', width: 100, render: formatQuantity },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 70, render: (value) => value || '-' },
  { title: '损耗率', dataIndex: 'wastageRate', key: 'wastageRate', width: 90, render: formatPercent },
];

const mrpColumns: ColumnsType<MrpResultRecord> = [
  { title: '批次号', dataIndex: 'batchNo', key: 'batchNo', width: 150, render: (value) => value || '-' },
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
  { title: '毛需求', dataIndex: 'grossDemand', key: 'grossDemand', width: 90, render: formatQuantity },
  { title: '可用库存', dataIndex: 'allocatedStock', key: 'allocatedStock', width: 90, render: formatQuantity },
  { title: '在途', dataIndex: 'inTransitQuantity', key: 'inTransitQuantity', width: 80, render: formatQuantity },
  { title: '净需求', dataIndex: 'netDemand', key: 'netDemand', width: 90, render: formatQuantity },
  { title: '建议采购', dataIndex: 'suggestedQuantity', key: 'suggestedQuantity', width: 100, render: formatQuantity },
  { title: '供应商', dataIndex: 'suggestedSupplierName', key: 'suggestedSupplierName', width: 120, render: (value) => value || '-' },
  { title: '预估成本', dataIndex: 'estimatedCost', key: 'estimatedCost', width: 100, render: formatAmount },
  { title: '状态', dataIndex: 'statusLabel', key: 'statusLabel', width: 90, render: (value) => value || '-' },
];

async function refreshPermissions(setPermissions: (permissions: string[]) => void) {
  try {
    const response = await getCurrentUserPermissions();
    setPermissions(response.permissions);
    const session = getAuthSession();
    if (session) {
      setAuthSession({ ...session, permissions: response.permissions, menuTree: response.menuTree });
    }
  } catch {
    setPermissions(getAuthSession()?.permissions ?? []);
  }
}

function formatAmount(value?: number | null): string {
  return typeof value === 'number'
    ? value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : '0.00';
}

function formatQuantity(value?: number | null): string {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '0';
}

function formatPercent(value?: number | null): string {
  return typeof value === 'number' ? `${Math.round(value * 100)}%` : '-';
}
