import {
  AccountBookOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  FileSearchOutlined,
  PlusOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { App, Button, DatePicker, Descriptions, Form, Input, Modal, ProCard, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  confirmStatement,
  disputeStatement,
  generateStatement,
  getStatementDetail,
  pageStatements,
  type GenerateStatementPayload,
  type StatementRecord,
  type StatementLineRecord,
} from '@/services/procurement/procurementService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '争议中', value: 'DISPUTED' },
];

const statusColorMap: Record<string, string> = { DRAFT: 'default', CONFIRMED: 'green', DISPUTED: 'orange' };

export function SupplierStatementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<GenerateFormValues>();
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<StatementRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<StatementRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);

  const canGenerate = permissions.includes('procurement:statement:generate');
  const canConfirm = permissions.includes('procurement:statement:confirm');
  const canDispute = permissions.includes('procurement:statement:dispute');

  const loadData = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageStatements({ current: 1, size: 20, ...(status ? { status } : {}) }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [status]);

  useEffect(() => { void loadData(); }, [loadData]);
  useEffect(() => {
    async function refresh() {
      try {
        const response = await getCurrentUserPermissions();
        setPermissions(response.permissions);
        const session = getAuthSession();
        if (session) setAuthSession({ ...session, permissions: response.permissions, menuTree: response.menuTree });
      } catch {
        setPermissions(getAuthSession()?.permissions ?? []);
      }
    }
    void refresh();
  }, []);

  async function openDetail(record: StatementRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      setDetail(await getStatementDetail(record.id));
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleConfirm(record: StatementRecord) {
    try {
      await confirmStatement(record.id);
      message.success('对账单已确认');
      await loadData();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleDispute(record: StatementRecord) {
    try {
      await disputeStatement(record.id);
      message.success('已标记为争议');
      await loadData();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleGenerate() {
    try {
      const values = await form.validateFields();
      const payload: GenerateStatementPayload = {
        supplierId: values.supplierId,
        periodStart: values.periodRange[0].format('YYYY-MM-DD'),
        periodEnd: values.periodRange[1].format('YYYY-MM-DD'),
        remark: values.remark?.trim() || undefined,
      };
      setSaving(true);
      await generateStatement(payload);
      message.success('对账单生成成功');
      setFormOpen(false);
      form.resetFields();
      await loadData();
    } catch (error) {
      if (typeof error === 'object' && error !== null && 'errorFields' in error) return;
      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  if (loading && !pageResult) return <LoadingState message="正在加载对账单" />;
  if (errorMessage && !pageResult) return <ErrorState message={errorMessage} onRetry={loadData} />;

  const columns: ColumnsType<StatementRecord> = [
    { title: '对账单号', dataIndex: 'statementNo', width: 170 },
    { title: '供应商ID', dataIndex: 'supplierId', width: 120 },
    { title: '对账期间', key: 'period', width: 220, render: (_, r) => `${r.periodStart ?? '-'} ~ ${r.periodEnd ?? '-'}` },
    { title: '对账金额', dataIndex: 'totalAmount', width: 120, render: (v) => v != null ? `¥${Number(v).toFixed(2)}` : '-' },
    {
      title: '状态', dataIndex: 'statusLabel', width: 100,
      render: (value, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{value || record.status || '-'}</Tag>,
    },
    { title: '创建时间', dataIndex: 'createdAt', width: 170, render: (v) => v || '-' },
    {
      title: '操作', key: 'actions', width: 260,
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} onClick={() => openDetail(record)}>详情</Button>
          {canConfirm && record.status === 'DRAFT' ? (
            <Button size="small" icon={<CheckCircleOutlined />} type="primary" onClick={() => handleConfirm(record)}>确认</Button>
          ) : null}
          {canDispute && record.status !== 'DISPUTED' ? (
            <Button size="small" icon={<ExclamationCircleOutlined />} danger onClick={() => handleDispute(record)}>争议</Button>
          ) : null}
        </Space>
      ),
    },
  ];

  const lineColumns: ColumnsType<StatementLineRecord> = [
    { title: '物料ID', dataIndex: 'materialId', width: 120 },
    { title: '物料编码', dataIndex: 'materialCode', width: 130, render: (v) => v || '-' },
    { title: '物料名称', dataIndex: 'materialName', width: 150, render: (v) => v || '-' },
    { title: '合格数量', dataIndex: 'acceptedQuantity', width: 110, render: (v) => v ?? '-' },
    { title: '单价', dataIndex: 'unitPrice', width: 100, render: (v) => v != null ? `¥${Number(v).toFixed(4)}` : '-' },
    { title: '金额', dataIndex: 'lineAmount', width: 120, render: (v) => v != null ? `¥${Number(v).toFixed(2)}` : '-' },
    { title: 'ASN ID', dataIndex: 'asnId', width: 120, render: (v) => v || '-' },
    { title: '采购订单ID', dataIndex: 'procurementOrderId', width: 120, render: (v) => v || '-' },
  ];

  return (
    <div className="system-page">
      <section className="system-page-topbar">
        <div><h1>供应商对账</h1><p>按期间汇总采购到货验收记录，生成对账单。</p></div>
      </section>
      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Select options={statusOptions} value={status} onChange={(v) => setStatus(v)} style={{ width: 140 }} />
          <Button icon={<SearchOutlined />} onClick={loadData}>搜索</Button>
          {canGenerate ? (
            <Button icon={<PlusOutlined />} type="primary" onClick={() => { form.resetFields(); setFormOpen(true); }}>
              生成对账单
            </Button>
          ) : null}
        </Space>
        {pageResult?.records.length === 0 ? <EmptyState message="暂无对账单" /> : (
          <Table rowKey="id" columns={columns} dataSource={pageResult?.records ?? []} loading={loading} pagination={false} />
        )}
      </ProCard>

      <Modal title={detail?.statementNo ?? '对账单详情'} open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={900} destroyOnHidden>
        <ProCard loading={detailLoading} bordered={false}>
          {detail ? (
            <Space direction="vertical" style={{ width: '100%' }}>
              <Descriptions column={2} size="small">
                <Descriptions.Item label="对账单号">{detail.statementNo}</Descriptions.Item>
                <Descriptions.Item label="状态"><Tag color={statusColorMap[detail.status ?? '']}>{detail.statusLabel}</Tag></Descriptions.Item>
                <Descriptions.Item label="供应商ID">{detail.supplierId}</Descriptions.Item>
                <Descriptions.Item label="对账金额">{detail.totalAmount != null ? `¥${Number(detail.totalAmount).toFixed(2)}` : '-'}</Descriptions.Item>
                <Descriptions.Item label="对账期间">{detail.periodStart} ~ {detail.periodEnd}</Descriptions.Item>
                <Descriptions.Item label="备注">{detail.remark || '-'}</Descriptions.Item>
              </Descriptions>
              <Table<StatementLineRecord> rowKey="id" size="small" pagination={false} columns={lineColumns} dataSource={detail.lines ?? []} />
            </Space>
          ) : null}
        </ProCard>
      </Modal>

      <Modal title="生成对账单" open={formOpen} confirmLoading={saving} onCancel={() => setFormOpen(false)} onOk={handleGenerate} okText="生成" cancelText="取消" width={600} destroyOnHidden>
        <Form<GenerateFormValues> form={form} layout="vertical">
          <Form.Item label="供应商ID" name="supplierId" rules={[{ required: true, message: '必填' }]}>
            <Input placeholder="输入供应商ID" />
          </Form.Item>
          <Form.Item label="对账期间" name="periodRange" rules={[{ required: true, message: '必填' }]}>
            <DatePicker.RangePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="备注" name="remark">
            <Input.TextArea autoSize={{ minRows: 1, maxRows: 3 }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface GenerateFormValues {
  supplierId?: string;
  periodRange?: [dayjs.Dayjs, dayjs.Dayjs];
  remark?: string;
}
