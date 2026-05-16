import { DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  createCodingRule,
  deleteCodingRule,
  generateCode,
  listCodingRules,
  previewCode,
  updateCodingRule,
  type CodingRuleRecord,
  type CodingRuleSegment,
  type CreateCodingRulePayload,
  type UpdateCodingRulePayload,
} from '@/services/master/codingRuleService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const CODE_MAX_LENGTH = 64;
const NAME_MAX_LENGTH = 100;
const DESCRIPTION_MAX_LENGTH = 300;
const DEFAULT_SEQUENCE_LENGTH = 5;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const formStatusOptions = statusOptions.filter((option) => option.value);

const segmentTypeOptions = [
  { label: '固定文本', value: 'FIXED' },
  { label: '日期', value: 'DATE' },
  { label: '流水号', value: 'SEQUENCE' },
  { label: '季节编码', value: 'SEASON' },
  { label: '仓库编码', value: 'WAREHOUSE' },
  { label: '自定义变量', value: 'CUSTOM' },
];

const resetTypeOptions = [
  { label: '不重置', value: 'NEVER' },
  { label: '按年重置', value: 'YEARLY' },
  { label: '按月重置', value: 'MONTHLY' },
  { label: '按日重置', value: 'DAILY' },
];

type FormMode = 'create' | 'edit';

interface CodingRuleFormValues {
  code: string;
  name: string;
  businessType?: string;
  description?: string;
  status?: string;
  segments: CodingRuleSegment[];
}

interface CodingRuleActions {
  canCreate: boolean;
  canDelete: boolean;
  canGenerate: boolean;
  canUpdate: boolean;
}

export function CodingRuleManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<CodingRuleFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [rules, setRules] = useState<CodingRuleRecord[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>('create');
  const [editingRule, setEditingRule] = useState<CodingRuleRecord | null>(null);
  const [previewRule, setPreviewRule] = useState<CodingRuleRecord | null>(null);
  const [previewValue, setPreviewValue] = useState('');
  const [previewLoading, setPreviewLoading] = useState(false);
  const [generatedRule, setGeneratedRule] = useState<CodingRuleRecord | null>(null);
  const [generatedValue, setGeneratedValue] = useState('');
  const [generateLoading, setGenerateLoading] = useState(false);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getCodingRuleActions(permissions);

  const loadRules = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setRules(await listCodingRules());
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadRules();
  }, [loadRules]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const filteredRules = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return rules.filter((rule) => {
      const matchesKeyword =
        !normalizedKeyword ||
        rule.code.toLowerCase().includes(normalizedKeyword) ||
        rule.name.toLowerCase().includes(normalizedKeyword);
      const matchesStatus = !status || rule.status === status;
      return matchesKeyword && matchesStatus;
    });
  }, [rules, keyword, status]);

  const handleSearch = () => {
    setKeyword(keywordInput.trim());
  };

  const openCreateForm = () => {
    setFormMode('create');
    setEditingRule(null);
    form.setFieldsValue({
      code: '',
      name: '',
      businessType: '',
      description: '',
      segments: [{ segmentType: 'FIXED', segmentValue: '', sortOrder: 1 }],
    });
    setFormOpen(true);
  };

  function openEditForm(rule: CodingRuleRecord) {
    setFormMode('edit');
    setEditingRule(rule);
    form.setFieldsValue({
      code: rule.code,
      name: rule.name,
      businessType: rule.businessType ?? '',
      description: rule.description ?? '',
      status: rule.status,
      segments: rule.segments ?? [],
    });
    setFormOpen(true);
  }

  async function handleSave() {
    try {
      const values = await form.validateFields();
      if (formMode === 'create' && !hasSequenceSegment(values.segments)) {
        form.setFields([{ name: 'segments', errors: ['至少需要一个流水号段'] }]);
        return;
      }
      setSaving(true);
      if (formMode === 'create') {
        await createCodingRule(toCreatePayload(values));
        message.success('编码规则创建成功');
      } else if (editingRule) {
        await updateCodingRule(editingRule.id, toUpdatePayload(values));
        message.success('编码规则更新成功');
      }
      setFormOpen(false);
      await loadRules();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(rule: CodingRuleRecord) {
    try {
      await deleteCodingRule(rule.id);
      message.success('编码规则已删除');
      await loadRules();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handlePreview(rule: CodingRuleRecord) {
    setPreviewRule(rule);
    setPreviewValue('');
    setPreviewLoading(true);
    try {
      const response = await previewCode({ ruleCode: rule.code });
      setPreviewValue(response.previewCode);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setPreviewLoading(false);
    }
  }

  async function handleGenerate(rule: CodingRuleRecord) {
    setGeneratedRule(rule);
    setGeneratedValue('');
    setGenerateLoading(true);
    try {
      setGeneratedValue(await generateCode({ ruleCode: rule.code }));
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setGenerateLoading(false);
    }
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

  if (loading && rules.length === 0) {
    return <LoadingState message="正在加载编码规则" />;
  }

  if (errorMessage && rules.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadRules} />;
  }

  return (
    <div className="system-page coding-rule-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>编码规则</h1>
          <p>维护业务编码规则、流水号段和预览效果。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建规则" onClick={openCreateForm}>
            新建规则
          </Button>
        ) : null}
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Input
            allowClear
            placeholder="搜索规则编码或名称"
            prefix={<SearchOutlined />}
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select aria-label="状态筛选" options={statusOptions} value={status} onChange={setStatus} />
          <Button icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadRules}>
            刷新
          </Button>
        </Space>

        {filteredRules.length === 0 ? (
          <EmptyState message="暂无编码规则" />
        ) : (
          <Table<CodingRuleRecord>
            rowKey="id"
            columns={buildColumns(actions, { onDelete: handleDelete, onEdit: openEditForm, onGenerate: handleGenerate, onPreview: handlePreview })}
            dataSource={filteredRules}
            loading={loading}
            pagination={false}
          />
        )}
      </ProCard>

      <Modal
        title={formMode === 'create' ? '新建编码规则' : '编辑编码规则'}
        open={formOpen}
        confirmLoading={saving}
        onCancel={() => setFormOpen(false)}
        onOk={handleSave}
        okText="保存"
      >
        <Form form={form} layout="vertical" preserve={false}>
          <Form.Item label="规则编码" name="code" normalize={normalizeTextInput} rules={[{ required: true, whitespace: true, message: '请输入规则编码' }]}>
            <Input allowClear disabled={formMode === 'edit'} maxLength={CODE_MAX_LENGTH} placeholder="如 SALES_ORDER" />
          </Form.Item>
          <Form.Item label="规则名称" name="name" normalize={normalizeTextInput} rules={[{ required: true, whitespace: true, message: '请输入规则名称' }]}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} placeholder="请输入规则名称" />
          </Form.Item>
          <Form.Item label="业务类型" name="businessType" normalize={normalizeTextInput}>
            <Input allowClear maxLength={CODE_MAX_LENGTH} placeholder="如 ORDER / MASTER" />
          </Form.Item>
          {formMode === 'edit' ? (
            <Form.Item label="状态" name="status" rules={[{ required: true, message: '请选择状态' }]}>
              <Select options={formStatusOptions} />
            </Form.Item>
          ) : null}
          <Form.Item label="说明" name="description" normalize={normalizeTextInput}>
            <Input.TextArea rows={2} maxLength={DESCRIPTION_MAX_LENGTH} placeholder="请输入说明" />
          </Form.Item>
          {formMode === 'create' ? <SegmentEditor /> : null}
        </Form>
      </Modal>

      <Modal title={previewRule?.name ?? '编码预览'} open={!!previewRule} footer={null} onCancel={() => setPreviewRule(null)}>
        <ProCard loading={previewLoading} bordered={false}>
          <p className="system-page-muted">规则编码：{previewRule?.code}</p>
          <h2>{previewValue || '-'}</h2>
        </ProCard>
      </Modal>

      <Modal title={generatedRule?.name ?? '正式生成'} open={!!generatedRule} footer={null} onCancel={() => setGeneratedRule(null)}>
        <ProCard loading={generateLoading} bordered={false}>
          <p className="system-page-muted">规则编码：{generatedRule?.code}</p>
          <h2>{generatedValue || '-'}</h2>
        </ProCard>
      </Modal>
    </div>
  );
}

function SegmentEditor() {
  return (
    <Form.List
      name="segments"
      rules={[
        {
          validator: async (_, value?: CodingRuleSegment[]) => {
            if (!hasSequenceSegment(value)) {
              throw new Error('至少需要一个流水号段');
            }
          },
        },
      ]}
    >
      {(fields, operations, meta) => (
        <div>
          <Space direction="vertical" style={{ width: '100%' }}>
            {fields.map((field, index) => (
              <ProCard key={field.key} size="small" bordered>
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Form.Item label="段类型" name={[field.name, 'segmentType']} rules={[{ required: true, message: '请选择段类型' }]}>
                    <Select options={segmentTypeOptions} />
                  </Form.Item>
                  <Form.Item noStyle shouldUpdate>
                    {({ getFieldValue }) =>
                      getFieldValue(['segments', field.name, 'segmentType']) === 'SEQUENCE' ? (
                        <Space.Compact block>
                          <Form.Item
                            label="流水号长度"
                            name={[field.name, 'seqLength']}
                            rules={[{ required: true, message: '请输入流水号长度' }]}
                            style={{ width: '50%' }}
                          >
                            <InputNumber min={1} max={12} precision={0} style={{ width: '100%' }} />
                          </Form.Item>
                          <Form.Item
                            label="重置方式"
                            name={[field.name, 'seqResetType']}
                            rules={[{ required: true, message: '请选择重置方式' }]}
                            style={{ width: '50%' }}
                          >
                            <Select options={resetTypeOptions} />
                          </Form.Item>
                        </Space.Compact>
                      ) : (
                        <Form.Item label="段值" name={[field.name, 'segmentValue']} normalize={normalizeTextInput}>
                          <Input allowClear placeholder="固定文本、日期格式或上下文变量名" />
                        </Form.Item>
                      )
                    }
                  </Form.Item>
                  <Space.Compact block>
                    <Form.Item label="连接符" name={[field.name, 'connector']} normalize={normalizeTextInput} style={{ width: '50%' }}>
                      <Input allowClear placeholder="如 -" />
                    </Form.Item>
                    <Form.Item label="排序号" name={[field.name, 'sortOrder']} initialValue={index + 1} rules={[{ required: true, message: '请输入排序号' }]} style={{ width: '50%' }}>
                      <InputNumber min={1} precision={0} style={{ width: '100%' }} />
                    </Form.Item>
                  </Space.Compact>
                  {fields.length > 1 ? <Button onClick={() => operations.remove(field.name)}>删除段</Button> : null}
                </Space>
              </ProCard>
            ))}
            <Button onClick={() => operations.add({ segmentType: 'SEQUENCE', seqLength: DEFAULT_SEQUENCE_LENGTH, seqResetType: 'NEVER', sortOrder: fields.length + 1 })}>
              新增段
            </Button>
          </Space>
          {meta.errors.map((error, index) => (
            <div className="ant-form-item-explain-error" key={`${String(error)}-${index}`}>
              {error}
            </div>
          ))}
        </div>
      )}
    </Form.List>
  );
}

function buildColumns(
  actions: CodingRuleActions,
  handlers: {
    onDelete: (rule: CodingRuleRecord) => void;
    onEdit: (rule: CodingRuleRecord) => void;
    onGenerate: (rule: CodingRuleRecord) => void;
    onPreview: (rule: CodingRuleRecord) => void;
  },
): ColumnsType<CodingRuleRecord> {
  return [
    { title: '规则编码', dataIndex: 'code', key: 'code', width: 160 },
    { title: '规则名称', dataIndex: 'name', key: 'name', width: 180 },
    { title: '业务类型', dataIndex: 'businessType', key: 'businessType', render: (value) => value || '-' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (value) => <Tag color={value === 'ACTIVE' ? 'green' : 'default'}>{value === 'ACTIVE' ? '启用' : '停用'}</Tag>,
    },
    { title: '使用状态', dataIndex: 'used', key: 'used', render: (value) => (value ? '已使用' : '未使用') },
    {
      title: '规则段',
      dataIndex: 'segments',
      key: 'segments',
      render: (segments?: CodingRuleSegment[] | null) => renderSegments(segments),
    },
    {
      title: '操作',
      key: 'actions',
      width: 220,
      render: (_, record) => (
        <Space>
          <Button icon={<EyeOutlined />} size="small" aria-label={`预览 ${record.code}`} onClick={() => handlers.onPreview(record)}>
            预览
          </Button>
          {actions.canGenerate ? (
            <Button size="small" aria-label={`正式生成 ${record.code}`} onClick={() => handlers.onGenerate(record)}>
              正式生成
            </Button>
          ) : null}
          {actions.canUpdate ? (
            <Button icon={<EditOutlined />} size="small" aria-label={`编辑 ${record.code}`} onClick={() => handlers.onEdit(record)}>
              编辑
            </Button>
          ) : null}
          {actions.canDelete && !record.used ? (
            <Popconfirm title="确认删除该编码规则？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(record)}>
              <Button danger icon={<DeleteOutlined />} size="small" aria-label={`删除 ${record.code}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function renderSegments(segments?: CodingRuleSegment[] | null) {
  if (!segments || segments.length === 0) {
    return '-';
  }

  return segments
    .slice()
    .sort((left, right) => left.sortOrder - right.sortOrder)
    .map((segment) => segment.segmentValue || segment.segmentType)
    .join('');
}

function hasSequenceSegment(segments?: CodingRuleSegment[]) {
  return (segments ?? []).some((segment) => segment.segmentType === 'SEQUENCE');
}

function getCodingRuleActions(permissions: string[]): CodingRuleActions {
  return {
    canCreate: permissions.includes('master:codingRule:create'),
    canDelete: permissions.includes('master:codingRule:delete'),
    canGenerate: permissions.includes('master:codingRule:generate'),
    canUpdate: permissions.includes('master:codingRule:update'),
  };
}

function toCreatePayload(values: CodingRuleFormValues): CreateCodingRulePayload {
  return {
    code: values.code,
    name: values.name,
    businessType: values.businessType,
    description: values.description,
    segments: values.segments,
  };
}

function toUpdatePayload(values: CodingRuleFormValues): UpdateCodingRulePayload {
  return {
    name: values.name,
    businessType: values.businessType,
    description: values.description,
    status: values.status,
  };
}

function normalizeTextInput(value?: string) {
  return typeof value === 'string' ? value.trim() : value;
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}
