import { DeleteOutlined, EditOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Form, Input, InputNumber, Modal, Select, Space, Switch, Table, Tag } from 'antd';
import { ProCard } from '@ant-design/pro-components';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  createAttributeDefinition,
  deleteAttributeDefinition,
  getAttributeDefinitionDetail,
  pageAttributeDefinitions,
  updateAttributeDefinition,
  type AttributeDefinitionRecord,
  type SaveAttributeDefinitionPayload,
} from '@/services/master/attributeDefService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const materialTypeOptions = [
  { label: '面料', value: 'FABRIC' },
  { label: '辅料', value: 'ACCESSORY' },
  { label: '包材', value: 'PACKAGING' },
];

const inputTypeOptions = [
  { label: '文本', value: 'TEXT' },
  { label: '数字', value: 'NUMBER' },
  { label: '单选', value: 'SELECT' },
  { label: '多选', value: 'MULTI_SELECT' },
  { label: '成分', value: 'COMPONENT' },
];

export function AttributeDefinitionPage() {
  const { message, modal } = App.useApp();
  const [form] = Form.useForm<AttrFormValues>();
  const [materialType, setMaterialType] = useState('');
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<AttributeDefinitionRecord> | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);

  const canCreate = permissions.includes('master:attr-def:create');
  const canUpdate = permissions.includes('master:attr-def:update');
  const canDelete = permissions.includes('master:attr-def:delete');

  const loadData = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageAttributeDefinitions({
        current: 1, size: 50,
        ...(materialType ? { materialType } : {}),
        ...(keyword ? { keyword } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [materialType, keyword]);

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

  async function openEditForm(record: AttributeDefinitionRecord) {
    setEditingId(record.id);
    try {
      const detail = await getAttributeDefinitionDetail(record.id);
      form.setFieldsValue({
        code: detail.code ?? '',
        name: detail.name ?? '',
        materialType: detail.materialType ?? '',
        inputType: detail.inputType ?? 'TEXT',
        required: detail.required ?? false,
        sortOrder: detail.sortOrder ?? 0,
        optionsText: (detail.options ?? []).join('\n'),
        jsonbPath: detail.jsonbPath ?? '',
        remark: detail.remark ?? '',
      });
      setFormOpen(true);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleSave() {
    try {
      const values = await form.validateFields();
      const options = values.optionsText
        ? values.optionsText.split('\n').map((s) => s.trim()).filter(Boolean)
        : undefined;
      const payload: SaveAttributeDefinitionPayload = {
        code: values.code!.trim(),
        name: values.name!.trim(),
        materialType: values.materialType!,
        inputType: values.inputType!,
        required: values.required!,
        sortOrder: values.sortOrder ?? 0,
        options,
        jsonbPath: values.jsonbPath?.trim() || undefined,
        remark: values.remark?.trim() || undefined,
      };
      setSaving(true);
      if (editingId) {
        await updateAttributeDefinition(editingId, payload);
        message.success('属性定义已更新');
      } else {
        await createAttributeDefinition(payload);
        message.success('属性定义已创建');
      }
      setFormOpen(false);
      form.resetFields();
      setEditingId(null);
      await loadData();
    } catch (error) {
      if (typeof error === 'object' && error !== null && 'errorFields' in error) return;
      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(record: AttributeDefinitionRecord) {
    modal.confirm({
      title: '确认删除',
      content: `确定删除属性定义「${record.name}」吗？`,
      onOk: async () => {
        try {
          await deleteAttributeDefinition(record.id);
          message.success('属性定义已删除');
          await loadData();
        } catch (error) {
          message.error(getApiErrorMessage(error));
        }
      },
    });
  }

  if (loading && !pageResult) return <LoadingState message="正在加载属性定义" />;
  if (errorMessage && !pageResult) return <ErrorState message={errorMessage} onRetry={loadData} />;

  const columns: ColumnsType<AttributeDefinitionRecord> = [
    { title: '编码', dataIndex: 'code', width: 150 },
    { title: '名称', dataIndex: 'name', width: 150 },
    {
      title: '物料类型', dataIndex: 'materialType', width: 100,
      render: (v) => materialTypeOptions.find((o) => o.value === v)?.label ?? v ?? '-',
    },
    {
      title: '输入类型', dataIndex: 'inputType', width: 100,
      render: (v) => inputTypeOptions.find((o) => o.value === v)?.label ?? v ?? '-',
    },
    { title: '必填', dataIndex: 'required', width: 70, render: (v) => v ? <Tag color="red">是</Tag> : <Tag>否</Tag> },
    { title: '排序', dataIndex: 'sortOrder', width: 70 },
    { title: 'JSONB 路径', dataIndex: 'jsonbPath', width: 150, render: (v) => v || '-' },
    { title: '备注', dataIndex: 'remark', width: 200, render: (v) => v || '-' },
    {
      title: '操作', key: 'actions', width: 150,
      render: (_, record) => (
        <Space>
          {canUpdate ? (
            <Button size="small" icon={<EditOutlined />} onClick={() => openEditForm(record)}>编辑</Button>
          ) : null}
          {canDelete ? (
            <Button size="small" icon={<DeleteOutlined />} danger onClick={() => handleDelete(record)}>删除</Button>
          ) : null}
        </Space>
      ),
    },
  ];

  return (
    <div className="system-page">
      <section className="system-page-topbar">
        <div><h1>属性定义管理</h1><p>管理物料扩展属性的元数据，驱动前端动态表单。</p></div>
      </section>
      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Select
            options={[{ label: '全部类型', value: '' }, ...materialTypeOptions]}
            value={materialType} onChange={(v) => setMaterialType(v)} style={{ width: 140 }}
          />
          <Input placeholder="搜索编码/名称" value={keyword} onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={() => void loadData()} style={{ width: 200 }} allowClear
          />
          <Button icon={<SearchOutlined />} onClick={loadData}>搜索</Button>
          {canCreate ? (
            <Button icon={<PlusOutlined />} type="primary" onClick={() => {
              form.resetFields(); form.setFieldsValue({ required: false, sortOrder: 0, inputType: 'TEXT' });
              setEditingId(null); setFormOpen(true);
            }}>新增属性定义</Button>
          ) : null}
        </Space>
        {pageResult?.records.length === 0 ? <EmptyState message="暂无属性定义" /> : (
          <Table rowKey="id" columns={columns} dataSource={pageResult?.records ?? []} loading={loading} pagination={false} />
        )}
      </ProCard>

      <Modal
        title={editingId ? '编辑属性定义' : '新增属性定义'}
        open={formOpen} confirmLoading={saving}
        onCancel={() => { setFormOpen(false); setEditingId(null); }}
        onOk={handleSave} okText="保存" cancelText="取消" width={640} destroyOnHidden
      >
        <Form<AttrFormValues> form={form} layout="vertical">
          <Space style={{ width: '100%' }} wrap>
            <Form.Item label="属性编码" name="code" rules={[{ required: true, message: '必填' }]}><Input /></Form.Item>
            <Form.Item label="属性名称" name="name" rules={[{ required: true, message: '必填' }]}><Input /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} wrap>
            <Form.Item label="物料类型" name="materialType" rules={[{ required: true, message: '必填' }]}>
              <Select options={materialTypeOptions} style={{ width: 160 }} />
            </Form.Item>
            <Form.Item label="输入类型" name="inputType" rules={[{ required: true, message: '必填' }]}>
              <Select options={inputTypeOptions} style={{ width: 160 }} />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} wrap>
            <Form.Item label="必填" name="required" valuePropName="checked"><Switch /></Form.Item>
            <Form.Item label="排序" name="sortOrder"><InputNumber min={0} /></Form.Item>
          </Space>
          <Form.Item label="JSONB 路径" name="jsonbPath"><Input placeholder="如: attributes.fabricWeight" /></Form.Item>
          <Form.Item label="选项（每行一个，仅单选/多选时使用）" name="optionsText">
            <Input.TextArea autoSize={{ minRows: 2, maxRows: 6 }} placeholder="选项1&#10;选项2&#10;选项3" />
          </Form.Item>
          <Form.Item label="备注" name="remark"><Input.TextArea autoSize={{ minRows: 1, maxRows: 3 }} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface AttrFormValues {
  code?: string;
  name?: string;
  materialType?: string;
  inputType?: string;
  required?: boolean;
  sortOrder?: number | null;
  optionsText?: string;
  jsonbPath?: string;
  remark?: string;
}
