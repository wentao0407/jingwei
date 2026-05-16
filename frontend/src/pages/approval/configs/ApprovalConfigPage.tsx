import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Modal, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  createApprovalConfig,
  deleteApprovalConfig,
  listApprovalConfigs,
  updateApprovalConfig,
  type ApprovalConfigRecord,
} from '@/services/approval/approvalService';

type ConfigFormValues = {
  businessType: string;
  configName: string;
  approvalMode: string;
  approverRoleIds: string;
  enabled: boolean;
};

const DEFAULT_FORM_VALUES: ConfigFormValues = {
  businessType: '',
  configName: '',
  approvalMode: 'SINGLE',
  approverRoleIds: '',
  enabled: true,
};

export function ApprovalConfigPage() {
  const { message, modal } = App.useApp();
  const [form] = Form.useForm<ConfigFormValues>();
  const [configs, setConfigs] = useState<ApprovalConfigRecord[]>([]);
  const [editingConfig, setEditingConfig] = useState<ApprovalConfigRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const loadConfigs = useCallback(async () => {
    setLoading(true);
    try {
      setConfigs(await listApprovalConfigs());
    } catch (error) {
      message.error(error instanceof Error ? error.message : '审批配置查询失败');
    } finally {
      setLoading(false);
    }
  }, [message]);

  useEffect(() => {
    void loadConfigs();
  }, [loadConfigs]);

  const openCreateForm = () => {
    setEditingConfig(null);
    form.setFieldsValue(DEFAULT_FORM_VALUES);
    setFormOpen(true);
  };

  const openEditForm = (record: ApprovalConfigRecord) => {
    setEditingConfig(record);
    form.setFieldsValue({
      businessType: record.businessType ?? '',
      configName: record.configName ?? '',
      approvalMode: record.approvalMode ?? 'SINGLE',
      approverRoleIds: (record.approverRoleIds ?? []).join(','),
      enabled: record.enabled ?? true,
    });
    setFormOpen(true);
  };

  const handleSave = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values) return;

    setSaving(true);
    try {
      const approverRoleIds = splitIds(values.approverRoleIds);
      if (editingConfig) {
        await updateApprovalConfig({
          id: editingConfig.id,
          configName: values.configName.trim(),
          approvalMode: values.approvalMode,
          approverRoleIds,
          enabled: values.enabled,
        });
        message.success('审批配置已更新');
      } else {
        await createApprovalConfig({
          businessType: values.businessType.trim(),
          configName: values.configName.trim(),
          approvalMode: values.approvalMode,
          approverRoleIds,
          enabled: values.enabled,
        });
        message.success('审批配置已创建');
      }
      setFormOpen(false);
      await loadConfigs();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '审批配置保存失败');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = (record: ApprovalConfigRecord) => {
    modal.confirm({
      title: '删除审批配置',
      content: record.configName || record.businessType || record.id,
      okText: '删除',
      okButtonProps: { danger: true },
      onOk: async () => {
        await deleteApprovalConfig(record.id);
        message.success('审批配置已删除');
        await loadConfigs();
      },
    });
  };

  return (
    <Card
      title="审批配置"
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} loading={loading} onClick={() => void loadConfigs()}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateForm}>
            新增配置
          </Button>
        </Space>
      }
    >
      <Table<ApprovalConfigRecord>
        rowKey="id"
        loading={loading}
        columns={buildColumns(openEditForm, handleDelete)}
        dataSource={configs}
        pagination={false}
      />
      <Modal
        title={editingConfig ? '编辑审批配置' : '新增审批配置'}
        open={formOpen}
        onCancel={() => setFormOpen(false)}
        onOk={() => void handleSave()}
        confirmLoading={saving}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" initialValues={DEFAULT_FORM_VALUES}>
          <Form.Item label="业务类型" name="businessType" rules={[{ required: true, whitespace: true, message: '请输入业务类型' }]}>
            <Input disabled={Boolean(editingConfig)} />
          </Form.Item>
          <Form.Item label="配置名称" name="configName" rules={[{ required: true, whitespace: true, message: '请输入配置名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="审批模式" name="approvalMode" rules={[{ required: true, message: '请选择审批模式' }]}>
            <Select options={[{ label: '单人审批', value: 'SINGLE' }, { label: '多人会签', value: 'COUNTERSIGN' }]} />
          </Form.Item>
          <Form.Item label="审批角色 ID" name="approverRoleIds" rules={[{ required: true, whitespace: true, message: '请输入审批角色 ID' }]}>
            <Input placeholder="多个 ID 用逗号分隔" />
          </Form.Item>
          <Form.Item label="状态" name="enabled" rules={[{ required: true, message: '请选择状态' }]}>
            <Select options={[{ label: '启用', value: true }, { label: '停用', value: false }]} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}

function buildColumns(
  onEdit: (record: ApprovalConfigRecord) => void,
  onDelete: (record: ApprovalConfigRecord) => void,
): ColumnsType<ApprovalConfigRecord> {
  return [
    { title: '业务类型', dataIndex: 'businessType', key: 'businessType', render: renderText },
    { title: '配置名称', dataIndex: 'configName', key: 'configName', render: renderText },
    { title: '审批模式', dataIndex: 'approvalMode', key: 'approvalMode', render: renderText },
    { title: '审批角色', dataIndex: 'approverRoleIds', key: 'approverRoleIds', render: (value?: string[] | null) => value?.join(', ') || '-' },
    { title: '状态', dataIndex: 'enabled', key: 'enabled', render: (value?: boolean | null) => <Tag color={value === false ? 'default' : 'green'}>{value === false ? '停用' : '启用'}</Tag> },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => onEdit(record)}>编辑</Button>
          <Button size="small" danger icon={<DeleteOutlined />} onClick={() => onDelete(record)}>删除</Button>
        </Space>
      ),
    },
  ];
}

function renderText(value?: string | null) {
  return value || '-';
}

function splitIds(value: string): string[] {
  return value.split(',').map((id) => id.trim()).filter(Boolean);
}
