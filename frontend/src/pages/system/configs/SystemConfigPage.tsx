import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, Modal, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  createSystemConfig,
  listSystemConfigs,
  updateSystemConfig,
  type CreateSystemConfigPayload,
  type SystemConfigRecord,
  type UpdateSystemConfigPayload,
} from '@/services/system/configService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const SYSTEM_CONFIG_CREATE_PERMISSION = 'system:config:create';
const SYSTEM_CONFIG_UPDATE_PERMISSION = 'system:config:update';
const CONFIG_KEY_MAX_LENGTH = 128;
const CONFIG_VALUE_MAX_LENGTH = 500;
const DESCRIPTION_MAX_LENGTH = 500;
const REMARK_MAX_LENGTH = 200;
const ALL_GROUPS = 'ALL';

const configGroupOptions = [
  { label: '全部分组', value: ALL_GROUPS },
  { label: '默认配置', value: 'DEFAULT' },
  { label: '库存配置', value: 'INVENTORY' },
  { label: '密码配置', value: 'PASSWORD' },
  { label: 'MRP配置', value: 'MRP' },
  { label: '其他配置', value: 'OTHER' },
];

const restartOptions = [
  { label: '无需重启', value: false },
  { label: '需要重启', value: true },
];

const configValueRules = [
  { required: true, whitespace: true, message: '请输入配置值' },
  { max: CONFIG_VALUE_MAX_LENGTH, message: `配置值最长${CONFIG_VALUE_MAX_LENGTH}个字符` },
];

const configKeyRules = [
  { required: true, whitespace: true, message: '请输入配置键' },
  { max: CONFIG_KEY_MAX_LENGTH, message: `配置键最长${CONFIG_KEY_MAX_LENGTH}个字符` },
];

const remarkRules = [
  { required: true, whitespace: true, message: '请输入修改原因' },
  { max: REMARK_MAX_LENGTH, message: `修改原因最长${REMARK_MAX_LENGTH}个字符` },
];

type ConfigFormValues = CreateSystemConfigPayload & UpdateSystemConfigPayload;
type FormMode = 'create' | 'edit';

export function SystemConfigPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<ConfigFormValues>();
  const [configs, setConfigs] = useState<SystemConfigRecord[]>([]);
  const [groupFilter, setGroupFilter] = useState(ALL_GROUPS);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [editingConfig, setEditingConfig] = useState<SystemConfigRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>('edit');
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const canCreateConfig = permissions.includes(SYSTEM_CONFIG_CREATE_PERMISSION);
  const canUpdateConfig = permissions.includes(SYSTEM_CONFIG_UPDATE_PERMISSION);
  const filteredConfigs = useMemo(
    () => filterConfigsByGroup(configs, groupFilter),
    [configs, groupFilter],
  );
  const configColumns = buildConfigColumns({
    canUpdate: canUpdateConfig,
    onEdit: openEditForm,
  });

  const loadConfigs = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setConfigs(await listSystemConfigs());
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadConfigs();
  }, [loadConfigs]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  function openCreateForm() {
    setFormMode('create');
    setEditingConfig(null);
    form.resetFields();
    form.setFieldsValue({
      configGroup: 'DEFAULT',
      needRestart: false,
    });
    setFormOpen(true);
  }

  function openEditForm(config: SystemConfigRecord) {
    setFormMode('edit');
    setEditingConfig(config);
    form.setFieldsValue({
      configValue: config.configValue,
      description: config.description ?? '',
      needRestart: config.needRestart ?? false,
      remark: '',
    });
    setFormOpen(true);
  }

  const closeForm = () => {
    if (saving) {
      return;
    }

    setFormOpen(false);
  };

  const handleSaveConfig = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createSystemConfig(toCreatePayload(values));
        message.success('系统配置创建成功');
      } else if (editingConfig) {
        await updateSystemConfig(editingConfig.id, toUpdatePayload(values));
        message.success('系统配置更新成功');
      }
      setFormOpen(false);
      await loadConfigs();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }

      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  async function refreshPermissions() {
    try {
      const response = await getCurrentUserPermissions();
      setPermissions(response.permissions);
      const session = getAuthSession();
      if (session) {
        setAuthSession({
          ...session,
          permissions: response.permissions,
          menuTree: response.menuTree,
        });
      }
    } catch {
      setPermissions(getAuthSession()?.permissions ?? []);
    }
  }

  if (loading && configs.length === 0) {
    return <LoadingState message="正在加载系统配置" />;
  }

  if (errorMessage && configs.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadConfigs} />;
  }

  return (
    <div className="system-page system-config-page">
      <section className="system-page-topbar">
        <div>
          <h1>系统配置</h1>
          <p>维护运行参数、密码策略、库存规则和 MRP 相关配置。</p>
        </div>
        <Space>
          <Select
            aria-label="配置分组"
            value={groupFilter}
            options={configGroupOptions}
            onChange={setGroupFilter}
          />
          {canCreateConfig ? (
            <Button type="primary" aria-label="新增配置" onClick={openCreateForm}>
              新增配置
            </Button>
          ) : null}
          <Button onClick={loadConfigs}>刷新</Button>
        </Space>
      </section>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadConfigs} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<SystemConfigRecord>
          rowKey="id"
          columns={configColumns}
          dataSource={filteredConfigs}
          loading={loading}
          locale={{
            emptyText: <EmptyState message="暂无系统配置" />,
          }}
          pagination={false}
        />
      </ProCard>

      <Modal
        title={formMode === 'create' ? '新增系统配置' : '编辑系统配置'}
        open={formOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={closeForm}
        onOk={handleSaveConfig}
        destroyOnHidden
        forceRender
      >
        <Form<ConfigFormValues> form={form} layout="vertical" preserve={false}>
          {formMode === 'create' ? (
            <Form.Item label="配置键" name="configKey" normalize={normalizeTextInput} rules={configKeyRules}>
              <Input allowClear maxLength={CONFIG_KEY_MAX_LENGTH} placeholder="请输入配置键" />
            </Form.Item>
          ) : (
            <Form.Item label="配置键">
              <Input disabled value={editingConfig?.configKey ?? ''} />
            </Form.Item>
          )}
          {formMode === 'create' ? (
            <Form.Item label="配置分组" name="configGroup">
              <Select aria-label="新增配置分组" options={configGroupOptions.slice(1)} />
            </Form.Item>
          ) : (
            <Form.Item label="配置分组">
              <Input disabled value={editingConfig?.configGroup ?? 'DEFAULT'} />
            </Form.Item>
          )}
          <Form.Item label="配置值" name="configValue" normalize={normalizeTextInput} rules={configValueRules}>
            <Input.TextArea rows={3} maxLength={CONFIG_VALUE_MAX_LENGTH} placeholder="请输入配置值" />
          </Form.Item>
          <Form.Item
            label="配置说明"
            name="description"
            normalize={normalizeTextInput}
            rules={[{ max: DESCRIPTION_MAX_LENGTH, message: `配置说明最长${DESCRIPTION_MAX_LENGTH}个字符` }]}
          >
            <Input.TextArea rows={2} maxLength={DESCRIPTION_MAX_LENGTH} placeholder="请输入配置说明" />
          </Form.Item>
          <Form.Item label="修改后重启" name="needRestart">
            <Select options={restartOptions} />
          </Form.Item>
          {formMode === 'edit' ? (
            <Form.Item label="修改原因" name="remark" normalize={normalizeTextInput} rules={remarkRules}>
              <Input.TextArea rows={2} maxLength={REMARK_MAX_LENGTH} placeholder="请输入修改原因" />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
    </div>
  );
}

function buildConfigColumns(actions: {
  canUpdate: boolean;
  onEdit: (config: SystemConfigRecord) => void;
}): ColumnsType<SystemConfigRecord> {
  const columns: ColumnsType<SystemConfigRecord> = [
    {
      title: '配置键',
      dataIndex: 'configKey',
    },
    {
      title: '配置值',
      dataIndex: 'configValue',
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '分组',
      dataIndex: 'configGroup',
      render: (value) => <Tag>{formatConfigGroupName(value)}</Tag>,
    },
    {
      title: '修改后重启',
      dataIndex: 'needRestart',
      render: (needRestart) => <RestartTag needRestart={Boolean(needRestart)} />,
    },
    {
      title: '配置说明',
      dataIndex: 'description',
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '修改原因',
      dataIndex: 'remark',
      ellipsis: true,
      render: (value) => value || '-',
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      render: (value) => formatDateTime(value),
    },
  ];

  if (!actions.canUpdate) {
    return columns;
  }

  return [
    ...columns,
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_, config) => (
        <Button type="link" aria-label={`编辑 ${config.configKey}`} onClick={() => actions.onEdit(config)}>
          编辑
        </Button>
      ),
    },
  ];
}

function RestartTag({ needRestart }: { needRestart: boolean }) {
  if (needRestart) {
    return <Tag color="orange">需要重启</Tag>;
  }

  return <Tag color="green">无需重启</Tag>;
}

function filterConfigsByGroup(configs: SystemConfigRecord[], groupFilter: string): SystemConfigRecord[] {
  if (groupFilter === ALL_GROUPS) {
    return configs;
  }

  return configs.filter((config) => (config.configGroup || 'DEFAULT') === groupFilter);
}

function formatConfigGroupName(value?: string | null): string {
  const groupValue = value || 'DEFAULT';
  const option = configGroupOptions.find((item) => item.value === groupValue);
  return option?.label ?? '未知分组';
}

function formatDateTime(value?: string | null): string {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 19);
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function toCreatePayload(values: ConfigFormValues): CreateSystemConfigPayload {
  return removeEmptyFields({
    configKey: values.configKey,
    configValue: values.configValue,
    configGroup: values.configGroup,
    description: values.description,
    needRestart: values.needRestart,
  }) as CreateSystemConfigPayload;
}

function toUpdatePayload(values: ConfigFormValues): UpdateSystemConfigPayload {
  return removeEmptyFields({
    configValue: values.configValue,
    description: values.description,
    needRestart: values.needRestart,
    remark: values.remark,
  }) as UpdateSystemConfigPayload;
}

function removeEmptyFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}

function normalizeTextInput(value: unknown): unknown {
  return typeof value === 'string' ? value.trim() : value;
}
