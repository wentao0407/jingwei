import { CheckCircleOutlined, FileSearchOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Descriptions, Form, Input, InputNumber, Modal, ProCard, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  confirmMaterialReturn,
  createMaterialReturn,
  getMaterialReturnDetail,
  pageMaterialReturns,
  type CreateMaterialReturnPayload,
  type MaterialReturnLineRecord,
  type MaterialReturnRecord,
} from '@/services/warehouse/materialReturnService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' },
];

const statusColorMap: Record<string, string> = { DRAFT: 'default', CONFIRMED: 'green', CANCELLED: 'red' };

export function MaterialReturnPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<ReturnFormValues>();
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<MaterialReturnRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<MaterialReturnRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);

  const canCreate = permissions.includes('warehouse:material-return:create');
  const canConfirm = permissions.includes('warehouse:material-return:confirm');

  const loadData = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageMaterialReturns({ current: 1, size: 20, ...(status ? { status } : {}) }));
    } catch (error) { setErrorMessage(getApiErrorMessage(error)); }
    finally { setLoading(false); }
  }, [status]);

  useEffect(() => { void loadData(); }, [loadData]);
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

  async function openDetail(record: MaterialReturnRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try { setDetail(await getMaterialReturnDetail(record.id)); }
    catch (error) { message.error(getApiErrorMessage(error)); setDetailOpen(false); }
    finally { setDetailLoading(false); }
  }

  async function handleConfirm(record: MaterialReturnRecord) {
    try {
      await confirmMaterialReturn(record.id);
      message.success('退料已确认，库存已增加');
      await loadData();
    } catch (error) { message.error(getApiErrorMessage(error)); }
  }

  async function handleCreate() {
    try {
      const values = await form.validateFields();
      const payload: CreateMaterialReturnPayload = {
        productionOrderId: values.productionOrderId,
        remark: values.remark?.trim() || undefined,
        lines: (values.lines ?? []).map((line) => ({
          materialId: line.materialId ?? '',
          batchNo: line.batchNo?.trim() || undefined,
          quantity: Number(line.quantity ?? 0),
          unit: line.unit?.trim() || undefined,
          remark: line.remark?.trim() || undefined,
        })),
      };
      setSaving(true);
      await createMaterialReturn(payload);
      message.success('创建退料单成功');
      setFormOpen(false);
      await loadData();
    } catch (error) {
      if (typeof error === 'object' && error !== null && 'errorFields' in error) return;
      message.error(getApiErrorMessage(error));
    } finally { setSaving(false); }
  }

  if (loading && !pageResult) return <LoadingState message="正在加载退料单" />;
  if (errorMessage && !pageResult) return <ErrorState message={errorMessage} onRetry={loadData} />;

  const columns: ColumnsType<MaterialReturnRecord> = [
    { title: '退料单号', dataIndex: 'returnNo', width: 170 },
    { title: '生产订单ID', dataIndex: 'productionOrderId', width: 130 },
    {
      title: '状态', dataIndex: 'statusLabel', width: 100,
      render: (value, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{value || record.status || '-'}</Tag>,
    },
    { title: '创建时间', dataIndex: 'createdAt', width: 170, render: (v) => v || '-' },
    {
      title: '操作', key: 'actions', width: 200,
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} onClick={() => openDetail(record)}>详情</Button>
          {canConfirm && record.status === 'DRAFT' ? (
            <Button size="small" icon={<CheckCircleOutlined />} type="primary" onClick={() => handleConfirm(record)}>确认退料</Button>
          ) : null}
        </Space>
      ),
    },
  ];

  return (
    <div className="system-page">
      <section className="system-page-topbar">
        <div><h1>退料管理</h1><p>车间退料入库，增加原料可用库存。</p></div>
      </section>
      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Select options={statusOptions} value={status} onChange={(v) => setStatus(v)} style={{ width: 140 }} />
          <Button icon={<SearchOutlined />} onClick={loadData}>搜索</Button>
          {canCreate ? <Button icon={<PlusOutlined />} type="primary" onClick={() => { form.setFieldsValue({ lines: [{}] }); setFormOpen(true); }}>新增退料单</Button> : null}
        </Space>
        {pageResult?.records.length === 0 ? <EmptyState message="暂无退料单" /> : (
          <Table rowKey="id" columns={columns} dataSource={pageResult?.records ?? []} loading={loading} pagination={false} />
        )}
      </ProCard>

      <Modal title={detail?.returnNo ?? '退料单详情'} open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={800} destroyOnHidden>
        <ProCard loading={detailLoading} bordered={false}>
          {detail ? (
            <Space direction="vertical" style={{ width: '100%' }}>
              <Descriptions column={2} size="small">
                <Descriptions.Item label="退料单号">{detail.returnNo}</Descriptions.Item>
                <Descriptions.Item label="状态"><Tag color={statusColorMap[detail.status ?? '']}>{detail.statusLabel}</Tag></Descriptions.Item>
                <Descriptions.Item label="生产订单ID">{detail.productionOrderId}</Descriptions.Item>
                <Descriptions.Item label="备注">{detail.remark || '-'}</Descriptions.Item>
              </Descriptions>
              <Table<MaterialReturnLineRecord> rowKey="id" size="small" pagination={false}
                columns={[
                  { title: '物料', render: (_, r) => r.materialCode || r.materialName || r.materialId || '-' },
                  { title: '数量', dataIndex: 'quantity', width: 100 },
                  { title: '单位', dataIndex: 'unit', width: 70, render: (v) => v || '-' },
                  { title: '批次', dataIndex: 'batchNo', width: 120, render: (v) => v || '-' },
                ]}
                dataSource={detail.lines ?? []}
              />
            </Space>
          ) : null}
        </ProCard>
      </Modal>

      <Modal title="新增退料单" open={formOpen} confirmLoading={saving} onCancel={() => setFormOpen(false)} onOk={handleCreate} okText="创建退料单" cancelText="取消" width={800} destroyOnHidden>
        <Form<ReturnFormValues> form={form} layout="vertical">
          <Form.Item label="生产订单ID" name="productionOrderId" rules={[{ required: true, message: '必填' }]}><Input /></Form.Item>
          <Form.Item label="备注" name="remark"><Input.TextArea autoSize={{ minRows: 1, maxRows: 3 }} /></Form.Item>
          <Form.List name="lines">
            {(fields, { add, remove }) => (
              <Space direction="vertical" style={{ width: '100%' }}>
                {fields.map((field) => (
                  <ProCard key={field.key} bordered size="small">
                    <Space wrap>
                      <Form.Item label="物料ID" name={[field.name, 'materialId']} rules={[{ required: true, message: '必填' }]}><Input /></Form.Item>
                      <Form.Item label="数量" name={[field.name, 'quantity']} rules={[{ required: true, message: '必填' }]}><InputNumber min={0.01} /></Form.Item>
                      <Form.Item label="单位" name={[field.name, 'unit']}><Input /></Form.Item>
                      <Form.Item label="批次号" name={[field.name, 'batchNo']}><Input /></Form.Item>
                      {fields.length > 1 ? <Button danger onClick={() => remove(field.name)}>删除</Button> : null}
                    </Space>
                  </ProCard>
                ))}
                <Button onClick={() => add({})}>新增退料行</Button>
              </Space>
            )}
          </Form.List>
        </Form>
      </Modal>
    </div>
  );
}

interface ReturnFormLineValues { materialId?: string; batchNo?: string; quantity?: number | null; unit?: string; remark?: string; }
interface ReturnFormValues { productionOrderId?: string; remark?: string; lines?: ReturnFormLineValues[]; }
