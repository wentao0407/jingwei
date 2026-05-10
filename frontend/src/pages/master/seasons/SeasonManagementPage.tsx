import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  closeSeason,
  createSeason,
  createWave,
  deleteSeason,
  deleteWave,
  getSeasonDetail,
  listSeasons,
  updateSeason,
  updateWave,
  type CreateSeasonPayload,
  type CreateWavePayload,
  type SeasonQueryParams,
  type SeasonRecord,
  type UpdateSeasonPayload,
  type UpdateWavePayload,
  type WaveRecord,
} from '@/services/master/seasonService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const CODE_MAX_LENGTH = 16;
const NAME_MAX_LENGTH = 64;
const INITIAL_YEAR = 2026;

const seasonTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '春夏', value: 'SPRING_SUMMER' },
  { label: '秋冬', value: 'AUTUMN_WINTER' },
];

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '关闭', value: 'CLOSED' },
];

type FormMode = 'create' | 'edit';
type SeasonFormValues = CreateSeasonPayload & UpdateSeasonPayload;
type WaveFormValues = CreateWavePayload & UpdateWavePayload;

export function SeasonManagementPage() {
  const { message } = App.useApp();
  const [seasonForm] = Form.useForm<SeasonFormValues>();
  const [waveForm] = Form.useForm<WaveFormValues>();
  const [yearInput, setYearInput] = useState('');
  const [year, setYear] = useState<number | undefined>();
  const [seasonType, setSeasonType] = useState('');
  const [status, setStatus] = useState('');
  const [seasons, setSeasons] = useState<SeasonRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [seasonFormOpen, setSeasonFormOpen] = useState(false);
  const [seasonFormMode, setSeasonFormMode] = useState<FormMode>('create');
  const [editingSeason, setEditingSeason] = useState<SeasonRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedSeason, setSelectedSeason] = useState<SeasonRecord | null>(null);
  const [seasonDetail, setSeasonDetail] = useState<SeasonRecord | null>(null);
  const [waveFormOpen, setWaveFormOpen] = useState(false);
  const [waveFormMode, setWaveFormMode] = useState<FormMode>('create');
  const [editingWave, setEditingWave] = useState<WaveRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getSeasonActions(permissions);

  const loadSeasons = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setSeasons(await listSeasons(buildQuery({ year, seasonType, status })));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [year, seasonType, status]);

  useEffect(() => {
    void loadSeasons();
  }, [loadSeasons]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  function handleYearChange(value: string) {
    setYearInput(value);
    const nextYear = Number(value);
    setYear(Number.isInteger(nextYear) && value.trim() ? nextYear : undefined);
  }

  function openCreateSeasonForm() {
    setSeasonFormMode('create');
    setEditingSeason(null);
    seasonForm.setFieldsValue({ year: INITIAL_YEAR });
    setSeasonFormOpen(true);
  }

  function openEditSeasonForm(season: SeasonRecord) {
    setSeasonFormMode('edit');
    setEditingSeason(season);
    seasonForm.setFieldsValue({
      name: season.name,
      startDate: season.startDate,
      endDate: season.endDate,
    });
    setSeasonFormOpen(true);
  }

  async function handleSaveSeason() {
    try {
      const values = await seasonForm.validateFields();
      setSaving(true);
      if (seasonFormMode === 'create') {
        await createSeason(toCreateSeasonPayload(values));
        message.success('季节创建成功');
      } else if (editingSeason) {
        await updateSeason(editingSeason.id, toUpdateSeasonPayload(values));
        message.success('季节更新成功');
      }
      setSeasonFormOpen(false);
      await loadSeasons();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function runSeasonAction(action: () => Promise<void>, successMessage: string) {
    try {
      await action();
      message.success(successMessage);
      await loadSeasons();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function openDetail(season: SeasonRecord) {
    setSelectedSeason(season);
    setSeasonDetail(null);
    setDetailOpen(true);
    await loadSeasonDetail(season.id);
  }

  function openCreateWaveForm() {
    setWaveFormMode('create');
    setEditingWave(null);
    waveForm.resetFields();
    setWaveFormOpen(true);
  }

  function openEditWaveForm(wave: WaveRecord) {
    setWaveFormMode('edit');
    setEditingWave(wave);
    waveForm.setFieldsValue({
      name: wave.name,
      deliveryDate: wave.deliveryDate ?? undefined,
      sortOrder: wave.sortOrder ?? undefined,
    });
    setWaveFormOpen(true);
  }

  async function handleSaveWave() {
    if (!selectedSeason) {
      return;
    }

    try {
      const values = await waveForm.validateFields();
      setSaving(true);
      if (waveFormMode === 'create') {
        await createWave(selectedSeason.id, toCreateWavePayload(values));
        message.success('波段创建成功');
      } else if (editingWave) {
        await updateWave(editingWave.id, toUpdateWavePayload(values));
        message.success('波段更新成功');
      }
      setWaveFormOpen(false);
      await loadSeasonDetail(selectedSeason.id);
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteWave(wave: WaveRecord) {
    if (!selectedSeason) {
      return;
    }

    try {
      await deleteWave(wave.id);
      message.success('波段已删除');
      await loadSeasonDetail(selectedSeason.id);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function loadSeasonDetail(seasonId: string) {
    setDetailLoading(true);
    try {
      setSeasonDetail(await getSeasonDetail(seasonId));
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setDetailLoading(false);
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

  if (loading && seasons.length === 0) {
    return <LoadingState message="正在加载季节数据" />;
  }

  if (errorMessage && seasons.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadSeasons} />;
  }

  return (
    <div className="system-page season-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>季节波段</h1>
          <p>维护季节周期、上市波段和关闭状态，供款式与订单引用。</p>
        </div>
        {actions.canCreateSeason ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建季节" onClick={openCreateSeasonForm}>
            新建季节
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input aria-label="年份筛选" value={yearInput} onChange={(event) => handleYearChange(event.target.value)} placeholder="年份" />
          <Select aria-label="季节类型筛选" value={seasonType} options={seasonTypeOptions} onChange={setSeasonType} />
          <Select aria-label="季节状态筛选" value={status} options={statusOptions} onChange={setStatus} />
          <Button icon={<ReloadOutlined />} onClick={loadSeasons}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadSeasons} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<SeasonRecord>
          rowKey="id"
          columns={buildSeasonColumns({
            actions,
            onClose: (season) => runSeasonAction(() => closeSeason(season.id), '季节已关闭'),
            onDelete: (season) => runSeasonAction(() => deleteSeason(season.id), '季节已删除'),
            onEdit: openEditSeasonForm,
            onOpenDetail: openDetail,
          })}
          dataSource={seasons}
          loading={loading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无季节" /> }}
        />
      </ProCard>

      <Modal
        title={seasonFormMode === 'create' ? '新建季节' : '编辑季节'}
        open={seasonFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setSeasonFormOpen(false)}
        onOk={handleSaveSeason}
        destroyOnHidden
      >
        <Form<SeasonFormValues> form={seasonForm} layout="vertical" preserve={false}>
          {seasonFormMode === 'create' ? (
            <>
              <Form.Item label="季节编码" name="code" normalize={normalizeTextInput} rules={requiredTextRules('季节编码', CODE_MAX_LENGTH)}>
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
              <Form.Item label="年份" name="year" rules={[{ required: true, message: '请输入年份' }]}>
                <InputNumber className="system-number-input" min={2000} max={2100} precision={0} />
              </Form.Item>
              <Form.Item label="季节类型" name="seasonType" rules={[{ required: true, message: '请选择季节类型' }]}>
                <Select aria-label="季节类型" options={seasonTypeOptions.slice(1)} />
              </Form.Item>
            </>
          ) : null}
          <Form.Item label="季节名称" name="name" normalize={normalizeTextInput} rules={requiredTextRules('季节名称', NAME_MAX_LENGTH)}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="开始日期" name="startDate" rules={[{ required: seasonFormMode === 'create', message: '请输入开始日期' }]}>
            <Input type="date" />
          </Form.Item>
          <Form.Item label="结束日期" name="endDate" rules={[{ required: seasonFormMode === 'create', message: '请输入结束日期' }]}>
            <Input type="date" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={selectedSeason ? `${selectedSeason.name} 波段` : '波段明细'} open={detailOpen} footer={null} onCancel={() => setDetailOpen(false)} width={760} destroyOnHidden>
        <Space className="system-table-toolbar">
          {actions.canCreateWave ? (
            <Button type="primary" icon={<PlusOutlined />} aria-label="新增波段" onClick={openCreateWaveForm}>
              新增波段
            </Button>
          ) : null}
        </Space>
        <Table<WaveRecord>
          rowKey="id"
          columns={buildWaveColumns({ actions, onDelete: handleDeleteWave, onEdit: openEditWaveForm })}
          dataSource={seasonDetail?.waves ?? []}
          loading={detailLoading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无波段" /> }}
        />
      </Modal>

      <Modal
        title={waveFormMode === 'create' ? '新增波段' : '编辑波段'}
        open={waveFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setWaveFormOpen(false)}
        onOk={handleSaveWave}
        destroyOnHidden
      >
        <Form<WaveFormValues> form={waveForm} layout="vertical" preserve={false}>
          {waveFormMode === 'create' ? (
            <Form.Item label="波段编码" name="code" normalize={normalizeTextInput} rules={requiredTextRules('波段编码', CODE_MAX_LENGTH)}>
              <Input allowClear maxLength={CODE_MAX_LENGTH} />
            </Form.Item>
          ) : null}
          <Form.Item label="波段名称" name="name" normalize={normalizeTextInput} rules={requiredTextRules('波段名称', NAME_MAX_LENGTH)}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="交货日期" name="deliveryDate">
            <Input type="date" />
          </Form.Item>
          <Form.Item label="排序号" name="sortOrder">
            <InputNumber className="system-number-input" min={0} precision={0} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface SeasonActions {
  canCloseSeason: boolean;
  canCreateSeason: boolean;
  canCreateWave: boolean;
  canDeleteSeason: boolean;
  canDeleteWave: boolean;
  canUpdateSeason: boolean;
  canUpdateWave: boolean;
}

function buildSeasonColumns(handlers: {
  actions: SeasonActions;
  onClose: (season: SeasonRecord) => void;
  onDelete: (season: SeasonRecord) => void;
  onEdit: (season: SeasonRecord) => void;
  onOpenDetail: (season: SeasonRecord) => void;
}): ColumnsType<SeasonRecord> {
  return [
    { title: '季节编码', dataIndex: 'code' },
    { title: '季节名称', dataIndex: 'name' },
    { title: '年份', dataIndex: 'year' },
    { title: '季节类型', dataIndex: 'seasonType', render: (value: unknown) => getOptionLabel(seasonTypeOptions, String(value)) },
    { title: '周期', render: (_, season) => `${season.startDate} 至 ${season.endDate}` },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      render: (_, season) => (
        <Space>
          <Button type="link" aria-label={`波段 ${season.name}`} onClick={() => handlers.onOpenDetail(season)}>
            波段
          </Button>
          {handlers.actions.canUpdateSeason ? (
            <Button type="link" aria-label={`编辑 ${season.name}`} onClick={() => handlers.onEdit(season)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canCloseSeason && season.status === 'ACTIVE' ? (
            <Popconfirm title="确认关闭该季节？" okText="确认关闭" cancelText="取消" onConfirm={() => handlers.onClose(season)}>
              <Button type="link" aria-label={`关闭 ${season.name}`}>
                关闭
              </Button>
            </Popconfirm>
          ) : null}
          {handlers.actions.canDeleteSeason ? (
            <Popconfirm title="确认删除该季节？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(season)}>
              <Button danger type="link" aria-label={`删除 ${season.name}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function buildWaveColumns(handlers: {
  actions: SeasonActions;
  onDelete: (wave: WaveRecord) => void;
  onEdit: (wave: WaveRecord) => void;
}): ColumnsType<WaveRecord> {
  return [
    { title: '波段编码', dataIndex: 'code' },
    { title: '波段名称', dataIndex: 'name' },
    { title: '交货日期', dataIndex: 'deliveryDate', render: (value: unknown) => value || '-' },
    { title: '排序号', dataIndex: 'sortOrder', render: (value: unknown) => value ?? 0 },
    {
      title: '操作',
      render: (_, wave) => (
        <Space>
          {handlers.actions.canUpdateWave ? (
            <Button type="link" aria-label={`编辑波段 ${wave.code}`} onClick={() => handlers.onEdit(wave)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canDeleteWave ? (
            <Popconfirm title="确认删除该波段？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(wave)}>
              <Button danger type="link" aria-label={`删除波段 ${wave.code}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function StatusTag({ status }: { status: string }) {
  if (status === 'ACTIVE') {
    return <Tag color="green">启用</Tag>;
  }
  if (status === 'CLOSED') {
    return <Tag color="default">关闭</Tag>;
  }
  return <Tag>{status || '-'}</Tag>;
}

function getSeasonActions(permissions: string[]): SeasonActions {
  return {
    canCloseSeason: permissions.includes('master:season:close'),
    canCreateSeason: permissions.includes('master:season:create'),
    canCreateWave: permissions.includes('master:wave:create'),
    canDeleteSeason: permissions.includes('master:season:delete'),
    canDeleteWave: permissions.includes('master:wave:delete'),
    canUpdateSeason: permissions.includes('master:season:update'),
    canUpdateWave: permissions.includes('master:wave:update'),
  };
}

function buildQuery(values: SeasonQueryParams): SeasonQueryParams {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value)) as SeasonQueryParams;
}

function getOptionLabel(options: { label: string; value: string }[], value: string) {
  return options.find((option) => option.value === value)?.label ?? value;
}

function toCreateSeasonPayload(values: SeasonFormValues): CreateSeasonPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    year: values.year,
    seasonType: values.seasonType,
    startDate: values.startDate,
    endDate: values.endDate,
  };
}

function toUpdateSeasonPayload(values: SeasonFormValues): UpdateSeasonPayload {
  return {
    name: values.name?.trim(),
    startDate: values.startDate,
    endDate: values.endDate,
  };
}

function toCreateWavePayload(values: WaveFormValues): CreateWavePayload {
  return {
    code: values.code?.trim() ?? '',
    name: values.name?.trim() ?? '',
    deliveryDate: values.deliveryDate,
    sortOrder: values.sortOrder,
  };
}

function toUpdateWavePayload(values: WaveFormValues): UpdateWavePayload {
  return {
    name: values.name?.trim(),
    deliveryDate: values.deliveryDate,
    sortOrder: values.sortOrder,
  };
}

function requiredTextRules(label: string, maxLength: number) {
  return [
    { required: true, whitespace: true, message: `请输入${label}` },
    { max: maxLength, message: `${label}最长${maxLength}个字符` },
  ];
}

function normalizeTextInput(value: unknown) {
  return typeof value === 'string' ? value.trim() : value;
}

function isFormValidationError(error: unknown) {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}
