import { CheckCircleOutlined, CloseCircleOutlined, FileSearchOutlined, PlusOutlined, SearchOutlined, SwapOutlined } from '@ant-design/icons';
import { App, Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Table, Tag } from 'antd';
import { ProCard } from '@ant-design/pro-components';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  cancelTransfer,
  completeTransfer,
  confirmTransfer,
  createTransferOrder,
  getTransferDetail,
  pageTransferOrders,
  type CreateTransferPayload,
  type TransferOrderLineRecord,
  type TransferOrderRecord,
} from '@/services/inventory/inventoryService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '调拨在途', value: 'IN_TRANSIT' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
];

const statusColorMap: Record<string, string> = {
  DRAFT: 'default',
  CONFIRMED: 'blue',
  IN_TRANSIT: 'orange',
  COMPLETED: 'green',
  CANCELLED: 'red',
};

export function TransferOrderPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<TransferFormValues>();
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<TransferOrderRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<TransferOrderRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);

  const canCreate = permissions.includes('inventory:transfer:create');
  const canConfirm = permissions.includes('inventory:transfer:confirm');
  const canComplete = permissions.includes('inventory:transfer:complete');
  const canCancel = permissions.includes('inventory:transfer:cancel');

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageTransferOrders({
        current: 1,
        size: 20,
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [status]);

  useEffect(() => { void loadOrders(); }, [loadOrders]);

  useEffect(() => {
    async function refresh() {
      try {
        const response = await getCurrentUserPermissions();
        setPermissions(response.permissions);
        const session = getAuthSession();
        if (session) setAuthSession({ ...session, permissions: response.permissions, menuTree: response.menuTree });
      } catch { setPermissions(getAuthSession()?.permissions ?? []); }
    }
    void refresh();
  }, []);

  async function openDetail(record: TransferOrderRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      setDetail(await getTransferDetail(record.id));
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleConfirm(record: TransferOrderRecord) {
    try {
      await confirmTransfer(record.id);
      message.success('调拨已确认，源仓库存已扣减');
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleComplete(record: TransferOrderRecord) {
    try {
      await completeTransfer(record.id);
      message.success('调拨已完成，目标仓库存已增加');
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleCancel(record: TransferOrderRecord) {
    try {
      await cancelTransfer(record.id);
      message.success('调拨已取消');
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  function openCreateForm() {
    form.setFieldsValue({ lines: [{}] });
    setFormOpen(true);
  }

  async function handleCreate() {
    try {
      const values = await form.validateFields();
      const payload: CreateTransferPayload = {
        sourceWarehouseId: values.sourceWarehouseId,
        targetWarehouseId: values.targetWarehouseId,
        remark: values.remark?.trim() || undefined,
        lines: (values.lines ?? []).map((line) => ({
          inventoryType: line.inventoryType ?? 'MATERIAL',
          skuId: line.skuId?.trim() || undefined,
          materialId: line.materialId?.trim() || undefined,
          quantity: Number(line.quantity ?? 0),
          batchNo: line.batchNo?.trim() || undefined,
          remark: line.remark?.trim() || undefined,
        })),
      };
      setSaving(true);
      await createTransferOrder(payload);
      message.success('创建调拨单成功');
      setFormOpen(false);
      await loadOrders();
    } catch (error) {
      if (typeof error === 'object' && error !== null && 'errorFields' in error) return;
      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  if (loading && !pageResult) return <LoadingState message="正在加载调拨单" />;
  if (errorMessage && !pageResult) return <ErrorState message={errorMessage} onRetry={loadOrders} />;

  const columns: ColumnsType<TransferOrderRecord> = [
    { title: '调拨单号', dataIndex: 'transferNo', width: 170 },
    { title: '源仓库', dataIndex: 'sourceWarehouseName', render: (v) => v || '-' },
    { title: '目标仓库', dataIndex: 'targetWarehouseName', render: (v) => v || '-' },
    {
      title: '状态', dataIndex: 'statusLabel', width: 100,
      render: (value, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{value || record.status || '-'}</Tag>,
    },
    { title: '创建时间', dataIndex: 'createdAt', width: 170, render: (v) => v || '-' },
    {
      title: '操作', key: 'actions', width: 280,
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} onClick={() => openDetail(record)}>详情</Button>
          {canConfirm && record.status === 'DRAFT' ? (
            <Button size="small" icon={<CheckCircleOutlined />} type="primary" onClick={() => handleConfirm(record)}>确认</Button>
          ) : null}
          {canComplete && (record.status === 'CONFIRMED' || record.status === 'IN_TRANSIT') ? (
            <Button size="small" icon={<SwapOutlined />} type="primary" onClick={() => handleComplete(record)}>完成</Button>
          ) : null}
          {canCancel && record.status !== 'COMPLETED' && record.status !== 'CANCELLED' ? (
            <Button size="small" icon={<CloseCircleOutlined />} danger onClick={() => handleCancel(record)}>取消</Button>
          ) : null}
        </Space>
      ),
    },
  ];

  return (
    <div className="system-page">
      <section className="system-page-topbar">
        <div>
          <h1>调拨管理</h1>
          <p>跨仓库库存调拨，支持创建→确认→在途→完成流程。</p>
        </div>
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Select options={statusOptions} value={status} onChange={(v) => setStatus(v)} style={{ width: 140 }} />
          <Button icon={<SearchOutlined />} onClick={loadOrders}>搜索</Button>
          {canCreate ? (
            <Button icon={<PlusOutlined />} type="primary" onClick={openCreateForm}>新增调拨单</Button>
          ) : null}
        </Space>
        {pageResult?.records.length === 0 ? (
          <EmptyState message="暂无调拨单" />
        ) : (
          <Table rowKey="id" columns={columns} dataSource={pageResult?.records ?? []} loading={loading} pagination={false} />
        )}
      </ProCard>

      {/* 详情弹窗 */}
      <Modal title={detail?.transferNo ?? '调拨单详情'} open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={800} destroyOnHidden>
        <ProCard loading={detailLoading} bordered={false}>
          {detail ? (
            <Space direction="vertical" style={{ width: '100%' }}>
              <Descriptions column={2} size="small">
                <Descriptions.Item label="调拨单号">{detail.transferNo}</Descriptions.Item>
                <Descriptions.Item label="状态"><Tag color={statusColorMap[detail.status ?? '']}>{detail.statusLabel}</Tag></Descriptions.Item>
                <Descriptions.Item label="源仓库">{detail.sourceWarehouseName || detail.sourceWarehouseId}</Descriptions.Item>
                <Descriptions.Item label="目标仓库">{detail.targetWarehouseName || detail.targetWarehouseId}</Descriptions.Item>
                <Descriptions.Item label="备注" span={2}>{detail.remark || '-'}</Descriptions.Item>
              </Descriptions>
              <Table<TransferOrderLineRecord>
                rowKey="id" size="small" pagination={false}
                columns={[
                  { title: '类型', dataIndex: 'inventoryType', width: 80 },
                  { title: '物料/ SKU', render: (_, r) => r.materialCode || r.materialName || r.skuCode || r.materialId || r.skuId || '-' },
                  { title: '数量', dataIndex: 'quantity', width: 100 },
                  { title: '批次', dataIndex: 'batchNo', width: 120, render: (v) => v || '-' },
                  { title: '备注', dataIndex: 'remark', render: (v) => v || '-' },
                ]}
                dataSource={detail.lines ?? []}
              />
            </Space>
          ) : null}
        </ProCard>
      </Modal>

      {/* 新增弹窗 */}
      <Modal
        title="新增调拨单" open={formOpen} confirmLoading={saving}
        onCancel={() => setFormOpen(false)} onOk={handleCreate}
        okText="创建调拨单" cancelText="取消" width={800} destroyOnHidden
      >
        <Form<TransferFormValues> form={form} layout="vertical">
          <Space style={{ width: '100%' }} wrap>
            <Form.Item label="源仓库ID" name="sourceWarehouseId" rules={[{ required: true, message: '请输入源仓库ID' }]}>
              <Input />
            </Form.Item>
            <Form.Item label="目标仓库ID" name="targetWarehouseId" rules={[{ required: true, message: '请输入目标仓库ID' }]}>
              <Input />
            </Form.Item>
          </Space>
          <Form.Item label="备注" name="remark">
            <Input.TextArea autoSize={{ minRows: 1, maxRows: 3 }} />
          </Form.Item>
          <Form.List name="lines">
            {(fields, { add, remove }) => (
              <Space direction="vertical" style={{ width: '100%' }}>
                {fields.map((field) => (
                  <ProCard key={field.key} bordered size="small">
                    <Space wrap>
                      <Form.Item label="类型" name={[field.name, 'inventoryType']} initialValue="MATERIAL">
                        <Select style={{ width: 120 }} options={[{ label: '原料', value: 'MATERIAL' }, { label: '成品', value: 'SKU' }]} />
                      </Form.Item>
                      <Form.Item label="物料ID" name={[field.name, 'materialId']}>
                        <Input placeholder="原料时填写" />
                      </Form.Item>
                      <Form.Item label="SKU ID" name={[field.name, 'skuId']}>
                        <Input placeholder="成品时填写" />
                      </Form.Item>
                      <Form.Item label="数量" name={[field.name, 'quantity']} rules={[{ required: true, message: '必填' }]}>
                        <InputNumber min={0.01} />
                      </Form.Item>
                      <Form.Item label="批次号" name={[field.name, 'batchNo']}>
                        <Input />
                      </Form.Item>
                      {fields.length > 1 ? <Button danger onClick={() => remove(field.name)}>删除</Button> : null}
                    </Space>
                  </ProCard>
                ))}
                <Button onClick={() => add({})}>新增调拨行</Button>
              </Space>
            )}
          </Form.List>
        </Form>
      </Modal>
    </div>
  );
}

interface TransferFormLineValues {
  inventoryType?: string;
  skuId?: string;
  materialId?: string;
  quantity?: number | null;
  batchNo?: string;
  remark?: string;
}

interface TransferFormValues {
  sourceWarehouseId?: string;
  targetWarehouseId?: string;
  remark?: string;
  lines?: TransferFormLineValues[];
}
