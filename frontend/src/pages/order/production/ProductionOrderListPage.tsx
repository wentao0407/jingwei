import { DollarOutlined, EyeOutlined, FileSearchOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Input, Modal, Progress, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  calculateProductionOrderMaterialRequirements,
  fireProductionLineEvent,
  fireProductionOrderEvent,
  getProductionOrderCostDetail,
  getProductionOrderCostIssues,
  getProductionLineAvailableActions,
  getProductionOrderAvailableActions,
  getProductionOrderDetail,
  pageProductionOrderMaterialRequirements,
  type MaterialRequirementRecord,
  pageProductionOrders,
  type ProductionOrderCostIssueRecord,
  type ProductionOrderCostRecord,
  type ProductionActionRecord,
  type ProductionOrderLineRecord,
  type ProductionOrderRecord,
} from '@/services/order/productionOrderService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const DATE_FORMAT_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const DATE_FORMAT_ERROR_MESSAGE = '计划日期格式必须为 YYYY-MM-DD';

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已下达', value: 'RELEASED' },
  { label: '已排产', value: 'PLANNED' },
  { label: '裁剪中', value: 'CUTTING' },
  { label: '缝制中', value: 'SEWING' },
  { label: '后整中', value: 'FINISHING' },
  { label: '已完工', value: 'COMPLETED' },
  { label: '已入库', value: 'STOCKED' },
];

const statusColorMap: Record<string, string> = {
  COMPLETED: 'green',
  CUTTING: 'orange',
  DRAFT: 'default',
  FINISHING: 'cyan',
  PLANNED: 'blue',
  RELEASED: 'geekblue',
  SEWING: 'purple',
  STOCKED: 'green',
};

interface ProductionOrderActions {
  canCalculateMaterialRequirements: boolean;
  canFireLineEvent: boolean;
  canFireOrderEvent: boolean;
  canViewCostDetail: boolean;
}

export function ProductionOrderListPage() {
  const { message } = App.useApp();
  const [orderNoInput, setOrderNoInput] = useState('');
  const [orderNo, setOrderNo] = useState('');
  const [status, setStatus] = useState('');
  const [planDateStartInput, setPlanDateStartInput] = useState('');
  const [planDateEndInput, setPlanDateEndInput] = useState('');
  const [planDateStart, setPlanDateStart] = useState('');
  const [planDateEnd, setPlanDateEnd] = useState('');
  const [dateValidationMessage, setDateValidationMessage] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<ProductionOrderRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<ProductionOrderRecord | null>(null);
  const [orderActions, setOrderActions] = useState<ProductionActionRecord[]>([]);
  const [lineActions, setLineActions] = useState<Record<string, ProductionActionRecord[]>>({});
  const [materialRequirementsOpen, setMaterialRequirementsOpen] = useState(false);
  const [materialRequirementsLoading, setMaterialRequirementsLoading] = useState(false);
  const [materialRequirements, setMaterialRequirements] = useState<MaterialRequirementRecord[]>([]);
  const [materialRequirementsBatchNo, setMaterialRequirementsBatchNo] = useState('');
  const [costOpen, setCostOpen] = useState(false);
  const [costLoading, setCostLoading] = useState(false);
  const [costDetail, setCostDetail] = useState<ProductionOrderCostRecord | null>(null);
  const [costIssues, setCostIssues] = useState<ProductionOrderCostIssueRecord[]>([]);
  const [selectedCostLine, setSelectedCostLine] = useState<ProductionOrderLineRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getProductionOrderActions(permissions);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(
        await pageProductionOrders({
          current: currentPage,
          size: pageSize,
          ...(orderNo ? { orderNo } : {}),
          ...(status ? { status } : {}),
          ...(planDateStart ? { planDateStart } : {}),
          ...(planDateEnd ? { planDateEnd } : {}),
        }),
      );
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, orderNo, status, planDateStart, planDateEnd]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const handleSearch = () => {
    const nextPlanDateStart = planDateStartInput.trim();
    const nextPlanDateEnd = planDateEndInput.trim();

    if (!isValidDateFilter(nextPlanDateStart) || !isValidDateFilter(nextPlanDateEnd)) {
      setDateValidationMessage(DATE_FORMAT_ERROR_MESSAGE);
      return;
    }

    setDateValidationMessage('');
    setOrderNo(orderNoInput.trim());
    setPlanDateStart(nextPlanDateStart);
    setPlanDateEnd(nextPlanDateEnd);
    setCurrentPage(INITIAL_PAGE);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  };

  async function openDetail(order: ProductionOrderRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      const [orderDetail, availableActions] = await Promise.all([
        getProductionOrderDetail(order.id),
        getProductionOrderAvailableActions(order.id),
      ]);
      const nextLineActions = await loadLineActions(orderDetail);
      setDetail(orderDetail);
      setOrderActions(availableActions);
      setLineActions(nextLineActions);
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function loadLineActions(order: ProductionOrderRecord) {
    const entries = await Promise.all(
      (order.lines ?? []).map(async (line) => [
        line.id,
        await getProductionLineAvailableActions(order.id, line.id),
      ] as const),
    );
    return Object.fromEntries(entries);
  }

  async function handleFireOrderEvent(action: ProductionActionRecord) {
    if (!detail) {
      return;
    }

    try {
      await fireProductionOrderEvent({ orderId: detail.id, event: action.event });
      message.success(`${action.label}成功`);
      await reloadDetail(detail.id);
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleFireLineEvent(line: ProductionOrderLineRecord, action: ProductionActionRecord) {
    if (!detail) {
      return;
    }

    try {
      await fireProductionLineEvent({ orderId: detail.id, lineId: line.id, event: action.event });
      message.success(`${action.label}成功`);
      await reloadDetail(detail.id);
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleShowMaterialRequirements() {
    if (!detail) {
      return;
    }

    setMaterialRequirementsOpen(true);
    setMaterialRequirementsLoading(true);
    setMaterialRequirements([]);
    setMaterialRequirementsBatchNo('');
    try {
      const calculation = await calculateProductionOrderMaterialRequirements(detail.id);
      const results = await pageProductionOrderMaterialRequirements({
        current: INITIAL_PAGE,
        size: 50,
        batchNo: calculation.batchNo,
      });
      setMaterialRequirementsBatchNo(calculation.batchNo);
      setMaterialRequirements(results.records ?? []);
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setMaterialRequirementsOpen(false);
    } finally {
      setMaterialRequirementsLoading(false);
    }
  }

  async function handleShowCostDetail(line: ProductionOrderLineRecord) {
    if (!detail) {
      return;
    }

    setCostOpen(true);
    setCostLoading(true);
    setCostDetail(null);
    setCostIssues([]);
    setSelectedCostLine(line);
    try {
      const [nextCostDetail, nextCostIssues] = await Promise.all([
        getProductionOrderCostDetail(detail.id, line.id),
        getProductionOrderCostIssues(detail.id),
      ]);
      setCostDetail(nextCostDetail);
      setCostIssues(nextCostIssues.filter((issue) => issue.productionLineId === line.id));
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setCostOpen(false);
    } finally {
      setCostLoading(false);
    }
  }

  async function reloadDetail(orderId: string) {
    const [orderDetail, availableActions] = await Promise.all([
      getProductionOrderDetail(orderId),
      getProductionOrderAvailableActions(orderId),
    ]);
    setDetail(orderDetail);
    setOrderActions(availableActions);
    setLineActions(await loadLineActions(orderDetail));
  }

  async function refreshPermissions() {
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

  if (loading && !pageResult) {
    return <LoadingState message="正在加载生产订单" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadOrders} />;
  }

  return (
    <div className="system-page production-order-list-page">
      <section className="system-page-topbar">
        <div>
          <h1>生产订单</h1>
          <p>跟踪生产单计划、工序状态、完工与入库进度。</p>
        </div>
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Input
            allowClear
            placeholder="搜索生产单号"
            prefix={<SearchOutlined />}
            value={orderNoInput}
            onChange={(event) => setOrderNoInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select aria-label="生产状态筛选" options={statusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Input
            placeholder="计划开始日期"
            value={planDateStartInput}
            onChange={(event) => setPlanDateStartInput(event.target.value)}
          />
          <Input
            placeholder="计划结束日期"
            value={planDateEndInput}
            onChange={(event) => setPlanDateEndInput(event.target.value)}
          />
          <Button icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadOrders}>
            刷新
          </Button>
        </Space>
        {dateValidationMessage ? <p className="ant-form-item-explain-error">{dateValidationMessage}</p> : null}

        {pageResult?.records.length === 0 ? (
          <EmptyState message="暂无生产订单" />
        ) : (
          <Table<ProductionOrderRecord>
            rowKey="id"
            columns={buildColumns(openDetail)}
            dataSource={pageResult?.records ?? []}
            loading={loading}
            pagination={{
              current: Number(pageResult?.current ?? currentPage),
              pageSize: Number(pageResult?.size ?? pageSize),
              total: Number(pageResult?.total ?? 0),
              showSizeChanger: true,
            }}
            onChange={handleTableChange}
          />
        )}
      </ProCard>

      <ProductionDetailModal
        actions={actions}
        detail={detail}
        lineActions={lineActions}
        loading={detailLoading}
        open={detailOpen}
        orderActions={orderActions}
        onCancel={() => setDetailOpen(false)}
        onFireLineEvent={handleFireLineEvent}
        onFireOrderEvent={handleFireOrderEvent}
        onShowCostDetail={handleShowCostDetail}
        onShowMaterialRequirements={handleShowMaterialRequirements}
      />
      <MaterialRequirementsModal
        batchNo={materialRequirementsBatchNo}
        loading={materialRequirementsLoading}
        open={materialRequirementsOpen}
        records={materialRequirements}
        onCancel={() => setMaterialRequirementsOpen(false)}
      />
      <CostDetailModal
        costDetail={costDetail}
        issues={costIssues}
        line={selectedCostLine}
        loading={costLoading}
        open={costOpen}
        onCancel={() => setCostOpen(false)}
      />
    </div>
  );
}

function buildColumns(onDetail: (order: ProductionOrderRecord) => void): ColumnsType<ProductionOrderRecord> {
  return [
    { title: '生产单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120, render: formatSourceType },
    { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120, render: (value) => value || '-' },
    { title: '完工期限', dataIndex: 'deadlineDate', key: 'deadlineDate', width: 120, render: (value) => value || '-' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (_, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{getStatusLabel(record)}</Tag>,
    },
    { title: '总数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 100, render: (value) => value ?? 0 },
    { title: '已完工', dataIndex: 'completedQuantity', key: 'completedQuantity', width: 100, render: (value) => value ?? 0 },
    { title: '已入库', dataIndex: 'stockedQuantity', key: 'stockedQuantity', width: 100, render: (value) => value ?? 0 },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.orderNo}`} onClick={() => onDetail(record)}>
          详情
        </Button>
      ),
    },
  ];
}

function ProductionDetailModal({
  actions,
  detail,
  lineActions,
  loading,
  open,
  orderActions,
  onCancel,
  onFireLineEvent,
  onFireOrderEvent,
  onShowCostDetail,
  onShowMaterialRequirements,
}: {
  actions: ProductionOrderActions;
  detail: ProductionOrderRecord | null;
  lineActions: Record<string, ProductionActionRecord[]>;
  loading: boolean;
  open: boolean;
  orderActions: ProductionActionRecord[];
  onCancel: () => void;
  onFireLineEvent: (line: ProductionOrderLineRecord, action: ProductionActionRecord) => void;
  onFireOrderEvent: (action: ProductionActionRecord) => void;
  onShowCostDetail: (line: ProductionOrderLineRecord) => void;
  onShowMaterialRequirements: () => void;
}) {
  return (
    <Modal
      footer={null}
      getContainer={false}
      onCancel={onCancel}
      open={open}
      title={detail?.orderNo ?? '生产订单详情'}
      width={980}
    >
      <ProCard loading={loading} bordered={false}>
        {detail ? (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Descriptions column={3} size="small">
              <Descriptions.Item label="生产单号">{detail.orderNo}</Descriptions.Item>
              <Descriptions.Item label="状态">{getStatusLabel(detail)}</Descriptions.Item>
              <Descriptions.Item label="来源">{formatSourceType(detail.sourceType)}</Descriptions.Item>
              <Descriptions.Item label="计划日期">{detail.planDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="完工期限">{detail.deadlineDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="总数量">{detail.totalQuantity ?? 0}</Descriptions.Item>
              <Descriptions.Item label="已完工">{detail.completedQuantity ?? 0}</Descriptions.Item>
              <Descriptions.Item label="已入库">{detail.stockedQuantity ?? 0}</Descriptions.Item>
              <Descriptions.Item label="备注">{detail.remark || '-'}</Descriptions.Item>
            </Descriptions>
            <Space wrap>
              <ProgressSummary label="完工进度" total={detail.totalQuantity} value={detail.completedQuantity} />
              <ProgressSummary label="入库进度" total={detail.totalQuantity} value={detail.stockedQuantity} />
            </Space>
            {(actions.canFireOrderEvent && orderActions.length > 0) || actions.canCalculateMaterialRequirements ? (
              <Space wrap>
                {actions.canCalculateMaterialRequirements ? (
                  <Button aria-label="物料需求" icon={<FileSearchOutlined />} onClick={onShowMaterialRequirements}>
                    物料需求
                  </Button>
                ) : null}
                {actions.canFireOrderEvent
                  ? orderActions.map((action) => (
                    <Button key={action.event} type="primary" onClick={() => onFireOrderEvent(action)}>
                      {action.label}
                    </Button>
                  ))
                  : null}
              </Space>
            ) : null}
            <Table<ProductionOrderLineRecord>
              rowKey="id"
              size="small"
              pagination={false}
              columns={buildLineColumns(actions, lineActions, onFireLineEvent, onShowCostDetail)}
              dataSource={detail.lines ?? []}
            />
          </Space>
        ) : null}
      </ProCard>
    </Modal>
  );
}

function buildLineColumns(
  actions: ProductionOrderActions,
  lineActions: Record<string, ProductionActionRecord[]>,
  onFireLineEvent: (line: ProductionOrderLineRecord, action: ProductionActionRecord) => void,
  onShowCostDetail: (line: ProductionOrderLineRecord) => void,
): ColumnsType<ProductionOrderLineRecord> {
  return [
    { title: '行号', dataIndex: 'lineNo', key: 'lineNo', width: 70 },
    { title: '款式', dataIndex: 'spuName', key: 'spuName', render: (_, record) => record.spuName || record.spuCode || '-' },
    { title: '颜色', dataIndex: 'colorName', key: 'colorName', render: (_, record) => record.colorName || record.colorCode || '-' },
    { title: '尺码矩阵', dataIndex: 'sizeMatrix', key: 'sizeMatrix', render: renderSizeMatrix },
    { title: '数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 80, render: (value) => value ?? 0 },
    { title: '已完工', dataIndex: 'completedQuantity', key: 'completedQuantity', width: 90, render: (value) => value ?? 0 },
    { title: '已入库', dataIndex: 'stockedQuantity', key: 'stockedQuantity', width: 90, render: (value) => value ?? 0 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      render: (_, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{record.statusLabel || record.status || '-'}</Tag>,
    },
    {
      title: '行操作',
      key: 'lineActions',
      width: 180,
      render: (_, record) => (
        <Space wrap>
          {actions.canFireLineEvent
            ? (lineActions[record.id] ?? []).map((action) => (
              <Button key={action.event} size="small" aria-label={`${action.label} ${record.id}`} onClick={() => onFireLineEvent(record, action)}>
                {action.label}
              </Button>
            ))
            : null}
          {actions.canViewCostDetail ? (
            <Button icon={<DollarOutlined />} size="small" aria-label={`成本 ${record.id}`} onClick={() => onShowCostDetail(record)}>
              成本
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];
}

function ProgressSummary({
  label,
  total,
  value,
}: {
  label: string;
  total?: number | null;
  value?: number | null;
}) {
  const safeTotal = Math.max(0, total ?? 0);
  const safeValue = Math.max(0, value ?? 0);
  const percent = safeTotal > 0 ? Math.min(100, Math.round((safeValue / safeTotal) * 100)) : 0;

  return (
    <div style={{ minWidth: 220 }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <span>{label}</span>
        <span>{`${safeValue}/${safeTotal}`}</span>
      </Space>
      <Progress percent={percent} size="small" />
    </div>
  );
}

function MaterialRequirementsModal({
  batchNo,
  loading,
  open,
  records,
  onCancel,
}: {
  batchNo: string;
  loading: boolean;
  open: boolean;
  records: MaterialRequirementRecord[];
  onCancel: () => void;
}) {
  return (
    <Modal
      footer={null}
      getContainer={false}
      onCancel={onCancel}
      open={open}
      title="物料需求"
      width={980}
    >
      <Space direction="vertical" style={{ width: '100%' }}>
        <Descriptions column={2} size="small">
          <Descriptions.Item label="MRP批次">{batchNo || '-'}</Descriptions.Item>
          <Descriptions.Item label="需求项数">{records.length}</Descriptions.Item>
        </Descriptions>
        <Table<MaterialRequirementRecord>
          rowKey="id"
          loading={loading}
          size="small"
          pagination={false}
          columns={materialRequirementColumns}
          dataSource={records}
        />
      </Space>
    </Modal>
  );
}

function CostDetailModal({
  costDetail,
  issues,
  line,
  loading,
  open,
  onCancel,
}: {
  costDetail: ProductionOrderCostRecord | null;
  issues: ProductionOrderCostIssueRecord[];
  line: ProductionOrderLineRecord | null;
  loading: boolean;
  open: boolean;
  onCancel: () => void;
}) {
  return (
    <Modal
      footer={null}
      getContainer={false}
      onCancel={onCancel}
      open={open}
      title="成本归集"
      width={900}
    >
      <Space direction="vertical" style={{ width: '100%' }}>
        <Descriptions column={3} size="small">
          <Descriptions.Item label="生产行">{line?.lineNo ?? line?.id ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="款式">{line?.spuName || line?.spuCode || '-'}</Descriptions.Item>
          <Descriptions.Item label="颜色">{line?.colorName || line?.colorCode || '-'}</Descriptions.Item>
          <Descriptions.Item label="面料成本">{formatAmount(costDetail?.materialCost)}</Descriptions.Item>
          <Descriptions.Item label="辅料成本">{formatAmount(costDetail?.trimCost)}</Descriptions.Item>
          <Descriptions.Item label="包材成本">{formatAmount(costDetail?.packagingCost)}</Descriptions.Item>
          <Descriptions.Item label="总成本">{formatAmount(costDetail?.totalCost)}</Descriptions.Item>
          <Descriptions.Item label="完工数量">{costDetail?.completedQty ?? 0}</Descriptions.Item>
          <Descriptions.Item label="单位成本">{formatAmount(costDetail?.unitCost)}</Descriptions.Item>
        </Descriptions>
        <h3 style={{ fontSize: 14, margin: 0 }}>领料明细</h3>
        <Table<ProductionOrderCostIssueRecord>
          rowKey="id"
          loading={loading}
          size="small"
          pagination={false}
          columns={costIssueColumns}
          dataSource={issues}
        />
      </Space>
    </Modal>
  );
}

const materialRequirementColumns: ColumnsType<MaterialRequirementRecord> = [
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
  { title: '毛需求', dataIndex: 'grossDemand', key: 'grossDemand', width: 90, render: formatQuantity },
  { title: '可用库存', dataIndex: 'allocatedStock', key: 'allocatedStock', width: 90, render: formatQuantity },
  { title: '在途', dataIndex: 'inTransitQuantity', key: 'inTransitQuantity', width: 80, render: formatQuantity },
  { title: '净需求', dataIndex: 'netDemand', key: 'netDemand', width: 90, render: formatQuantity },
  { title: '建议采购', dataIndex: 'suggestedQuantity', key: 'suggestedQuantity', width: 100, render: formatQuantity },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 70, render: (value) => value || '-' },
  { title: '建议供应商', dataIndex: 'suggestedSupplierName', key: 'suggestedSupplierName', width: 140, render: (value) => value || '-' },
  { title: '预估成本', dataIndex: 'estimatedCost', key: 'estimatedCost', width: 100, render: formatAmount },
  { title: '状态', dataIndex: 'statusLabel', key: 'statusLabel', width: 90, render: (value) => value || '-' },
];

const costIssueColumns: ColumnsType<ProductionOrderCostIssueRecord> = [
  { title: '物料类型', dataIndex: 'materialTypeLabel', key: 'materialTypeLabel', width: 100, render: (value) => value || '-' },
  { title: '物料ID', dataIndex: 'materialId', key: 'materialId', width: 120, render: (value) => value || '-' },
  { title: '领料数量', dataIndex: 'issueQty', key: 'issueQty', width: 110, render: formatQuantity },
  { title: '单位成本', dataIndex: 'unitCost', key: 'unitCost', width: 110, render: formatAmount },
  { title: '成本金额', dataIndex: 'costAmount', key: 'costAmount', width: 110, render: formatAmount },
  { title: '领料日期', dataIndex: 'issueDate', key: 'issueDate', width: 120, render: (value) => value || '-' },
];

function renderSizeMatrix(value?: Record<string, unknown> | null) {
  const sizes = Array.isArray(value?.sizes) ? value.sizes : [];
  if (sizes.length === 0) {
    return '-';
  }

  return (
    <Space wrap>
      {sizes.map((item) => {
        const size = item as { code?: string; quantity?: number };
        return <Tag key={size.code}>{`${size.code}: ${size.quantity ?? 0}`}</Tag>;
      })}
    </Space>
  );
}

function getProductionOrderActions(permissions: string[]): ProductionOrderActions {
  return {
    canCalculateMaterialRequirements: permissions.includes('procurement:mrp:calculate'),
    canFireLineEvent: permissions.includes('order:production:fire-line-event'),
    canFireOrderEvent: permissions.includes('order:production:fire-event'),
    canViewCostDetail: permissions.includes('cost:query:detail'),
  };
}

function getStatusLabel(order: ProductionOrderRecord): string {
  return order.statusLabel || statusOptions.find((option) => option.value === order.status)?.label || order.status || '-';
}

function formatSourceType(value?: string | null): string {
  if (value === 'SALES_ORDER') {
    return '销售订单';
  }
  if (value === 'MANUAL') {
    return '手工创建';
  }
  return value || '-';
}

function isValidDateFilter(value: string): boolean {
  if (!value) {
    return true;
  }

  return DATE_FORMAT_PATTERN.test(value);
}

function formatQuantity(value?: number | null): string {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '0';
}

function formatAmount(value?: number | null): string {
  return typeof value === 'number'
    ? value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : '0.00';
}
