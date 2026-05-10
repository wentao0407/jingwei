import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  activateWarehouse,
  createLocation,
  createWarehouse,
  deactivateLocation,
  deactivateWarehouse,
  deleteLocation,
  deleteWarehouse,
  freezeLocation,
  getWarehouseDetail,
  pageWarehouses,
  unfreezeLocation,
  updateLocation,
  updateWarehouse,
  type CreateLocationPayload,
  type CreateWarehousePayload,
  type LocationRecord,
  type UpdateLocationPayload,
  type UpdateWarehousePayload,
  type WarehouseRecord,
} from '@/services/master/warehouseService';
import type { PageResult } from '@/services/master/customerService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const CODE_MAX_LENGTH = 16;
const NAME_MAX_LENGTH = 64;

const warehouseTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '成品仓', value: 'FINISHED_GOODS' },
  { label: '原料仓', value: 'RAW_MATERIAL' },
  { label: '退货仓', value: 'RETURN' },
];

const warehouseStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const locationTypeOptions = [
  { label: '存储位', value: 'STORAGE' },
  { label: '拣货位', value: 'PICKING' },
  { label: '暂存位', value: 'STAGING' },
  { label: '质检位', value: 'QC' },
];

type FormMode = 'create' | 'edit';
type WarehouseFormValues = CreateWarehousePayload & UpdateWarehousePayload;
type LocationFormValues = CreateLocationPayload & UpdateLocationPayload;

export function WarehouseManagementPage() {
  const { message } = App.useApp();
  const [warehouseForm] = Form.useForm<WarehouseFormValues>();
  const [locationForm] = Form.useForm<LocationFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [type, setType] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<WarehouseRecord> | null>(null);
  const [warehouseFormOpen, setWarehouseFormOpen] = useState(false);
  const [warehouseFormMode, setWarehouseFormMode] = useState<FormMode>('create');
  const [editingWarehouse, setEditingWarehouse] = useState<WarehouseRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedWarehouse, setSelectedWarehouse] = useState<WarehouseRecord | null>(null);
  const [warehouseDetail, setWarehouseDetail] = useState<WarehouseRecord | null>(null);
  const [locationFormOpen, setLocationFormOpen] = useState(false);
  const [locationFormMode, setLocationFormMode] = useState<FormMode>('create');
  const [editingLocation, setEditingLocation] = useState<LocationRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getWarehouseActions(permissions);

  const loadWarehouses = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(
        await pageWarehouses({
          current: currentPage,
          size: pageSize,
          ...(keyword ? { keyword } : {}),
          ...(type ? { type } : {}),
          ...(status ? { status } : {}),
        }),
      );
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, keyword, type, status]);

  useEffect(() => {
    void loadWarehouses();
  }, [loadWarehouses]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  function handleSearch() {
    setKeyword(keywordInput.trim());
    setCurrentPage(INITIAL_PAGE);
  }

  function handleTableChange(pagination: TablePaginationConfig) {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  }

  function openCreateWarehouseForm() {
    setWarehouseFormMode('create');
    setEditingWarehouse(null);
    warehouseForm.resetFields();
    setWarehouseFormOpen(true);
  }

  function openEditWarehouseForm(warehouse: WarehouseRecord) {
    setWarehouseFormMode('edit');
    setEditingWarehouse(warehouse);
    warehouseForm.setFieldsValue({
      name: warehouse.name,
      address: warehouse.address ?? '',
      managerId: warehouse.managerId ?? undefined,
      remark: warehouse.remark ?? '',
    });
    setWarehouseFormOpen(true);
  }

  async function handleSaveWarehouse() {
    try {
      const values = await warehouseForm.validateFields();
      setSaving(true);
      if (warehouseFormMode === 'create') {
        await createWarehouse(toCreateWarehousePayload(values));
        message.success('仓库创建成功');
      } else if (editingWarehouse) {
        await updateWarehouse(editingWarehouse.id, toUpdateWarehousePayload(values));
        message.success('仓库更新成功');
      }
      setWarehouseFormOpen(false);
      await loadWarehouses();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function runWarehouseAction(action: () => Promise<void>, successMessage: string) {
    try {
      await action();
      message.success(successMessage);
      await loadWarehouses();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function openDetail(warehouse: WarehouseRecord) {
    setSelectedWarehouse(warehouse);
    setWarehouseDetail(null);
    setDetailOpen(true);
    await loadWarehouseDetail(warehouse.id);
  }

  function openCreateLocationForm() {
    setLocationFormMode('create');
    setEditingLocation(null);
    locationForm.resetFields();
    setLocationFormOpen(true);
  }

  function openEditLocationForm(location: LocationRecord) {
    setLocationFormMode('edit');
    setEditingLocation(location);
    locationForm.setFieldsValue({ capacity: location.capacity ?? undefined, remark: location.remark ?? '' });
    setLocationFormOpen(true);
  }

  async function handleSaveLocation() {
    if (!selectedWarehouse) {
      return;
    }

    try {
      const values = await locationForm.validateFields();
      setSaving(true);
      if (locationFormMode === 'create') {
        await createLocation(selectedWarehouse.id, toCreateLocationPayload(values));
        message.success('库位创建成功');
      } else if (editingLocation) {
        await updateLocation(editingLocation.id, toUpdateLocationPayload(values));
        message.success('库位更新成功');
      }
      setLocationFormOpen(false);
      await loadWarehouseDetail(selectedWarehouse.id);
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function runLocationAction(action: () => Promise<void>, successMessage: string) {
    if (!selectedWarehouse) {
      return;
    }

    try {
      await action();
      message.success(successMessage);
      await loadWarehouseDetail(selectedWarehouse.id);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function loadWarehouseDetail(warehouseId: string) {
    setDetailLoading(true);
    try {
      setWarehouseDetail(await getWarehouseDetail(warehouseId));
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

  if (loading && !pageResult) {
    return <LoadingState message="正在加载仓库数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadWarehouses} />;
  }

  return (
    <div className="system-page warehouse-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>仓库库位</h1>
          <p>维护仓库档案、库位容量和冻结状态，支撑库存与仓库作业。</p>
        </div>
        {actions.canCreateWarehouse ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建仓库" onClick={openCreateWarehouseForm}>
            新建仓库
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input
            allowClear
            placeholder="搜索仓库编码或名称"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Button icon={<SearchOutlined />} aria-label="搜索" onClick={handleSearch}>
            搜索
          </Button>
          <Select aria-label="仓库类型筛选" value={type} options={warehouseTypeOptions} onChange={setType} />
          <Select aria-label="仓库状态筛选" value={status} options={warehouseStatusOptions} onChange={setStatus} />
          <Button icon={<ReloadOutlined />} onClick={loadWarehouses}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadWarehouses} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<WarehouseRecord>
          rowKey="id"
          columns={buildWarehouseColumns({
            actions,
            onActivate: (warehouse) => runWarehouseAction(() => activateWarehouse(warehouse.id), '仓库已启用'),
            onDeactivate: (warehouse) => runWarehouseAction(() => deactivateWarehouse(warehouse.id), '仓库已停用'),
            onDelete: (warehouse) => runWarehouseAction(() => deleteWarehouse(warehouse.id), '仓库已删除'),
            onEdit: openEditWarehouseForm,
            onOpenDetail: openDetail,
          })}
          dataSource={pageResult?.records ?? []}
          loading={loading}
          pagination={{
            current: pageResult?.current ?? currentPage,
            pageSize: pageResult?.size ?? pageSize,
            total: pageResult?.total ?? 0,
            showSizeChanger: true,
          }}
          onChange={handleTableChange}
          locale={{ emptyText: <EmptyState message="暂无仓库" /> }}
        />
      </ProCard>

      <Modal
        title={warehouseFormMode === 'create' ? '新建仓库' : '编辑仓库'}
        open={warehouseFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setWarehouseFormOpen(false)}
        onOk={handleSaveWarehouse}
        destroyOnHidden
      >
        <Form<WarehouseFormValues> form={warehouseForm} layout="vertical" preserve={false}>
          {warehouseFormMode === 'create' ? (
            <>
              <Form.Item label="仓库编码" name="code" normalize={normalizeTextInput} rules={requiredTextRules('仓库编码', CODE_MAX_LENGTH)}>
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
              <Form.Item label="仓库类型" name="type" rules={[{ required: true, message: '请选择仓库类型' }]}>
                <Select aria-label="仓库类型" options={warehouseTypeOptions.slice(1)} />
              </Form.Item>
            </>
          ) : null}
          <Form.Item label="仓库名称" name="name" normalize={normalizeTextInput} rules={requiredTextRules('仓库名称', NAME_MAX_LENGTH)}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="地址" name="address" normalize={normalizeTextInput}>
            <Input allowClear />
          </Form.Item>
          <Form.Item label="管理员ID" name="managerId" normalize={normalizeTextInput}>
            <Input allowClear />
          </Form.Item>
          <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
            <Input.TextArea rows={3} maxLength={200} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={selectedWarehouse ? `${selectedWarehouse.name} 库位` : '库位明细'} open={detailOpen} footer={null} onCancel={() => setDetailOpen(false)} width={900} destroyOnHidden>
        <Space className="system-table-toolbar">
          {actions.canCreateLocation ? (
            <Button type="primary" icon={<PlusOutlined />} aria-label="新增库位" onClick={openCreateLocationForm}>
              新增库位
            </Button>
          ) : null}
        </Space>
        <Table<LocationRecord>
          rowKey="id"
          columns={buildLocationColumns({
            actions,
            onDeactivate: (location) => runLocationAction(() => deactivateLocation(location.id), '库位已停用'),
            onDelete: (location) => runLocationAction(() => deleteLocation(location.id), '库位已删除'),
            onEdit: openEditLocationForm,
            onFreeze: (location) => runLocationAction(() => freezeLocation(location.id), '库位已冻结'),
            onUnfreeze: (location) => runLocationAction(() => unfreezeLocation(location.id), '库位已解冻'),
          })}
          dataSource={warehouseDetail?.locations ?? []}
          loading={detailLoading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无库位" /> }}
        />
      </Modal>

      <Modal
        title={locationFormMode === 'create' ? '新增库位' : '编辑库位'}
        open={locationFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setLocationFormOpen(false)}
        onOk={handleSaveLocation}
        destroyOnHidden
      >
        <Form<LocationFormValues> form={locationForm} layout="vertical" preserve={false}>
          {locationFormMode === 'create' ? (
            <>
              <Form.Item label="库区编码" name="zoneCode" normalize={normalizeTextInput} rules={requiredTextRules('库区编码', CODE_MAX_LENGTH)}>
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
              <Form.Item label="货架编码" name="rackCode" normalize={normalizeTextInput} rules={requiredTextRules('货架编码', CODE_MAX_LENGTH)}>
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
              <Form.Item label="层编码" name="rowCode" normalize={normalizeTextInput} rules={requiredTextRules('层编码', CODE_MAX_LENGTH)}>
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
              <Form.Item label="位编码" name="binCode" normalize={normalizeTextInput} rules={requiredTextRules('位编码', CODE_MAX_LENGTH)}>
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
              <Form.Item label="库位类型" name="locationType" rules={[{ required: true, message: '请选择库位类型' }]}>
                <Select aria-label="库位类型" options={locationTypeOptions} />
              </Form.Item>
            </>
          ) : null}
          <Form.Item label="容量" name="capacity">
            <InputNumber className="system-number-input" min={0} precision={0} />
          </Form.Item>
          <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
            <Input.TextArea rows={3} maxLength={200} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface WarehouseActions {
  canActivateWarehouse: boolean;
  canCreateLocation: boolean;
  canCreateWarehouse: boolean;
  canDeactivateLocation: boolean;
  canDeactivateWarehouse: boolean;
  canDeleteLocation: boolean;
  canDeleteWarehouse: boolean;
  canFreezeLocation: boolean;
  canUnfreezeLocation: boolean;
  canUpdateLocation: boolean;
  canUpdateWarehouse: boolean;
}

function buildWarehouseColumns(handlers: {
  actions: WarehouseActions;
  onActivate: (warehouse: WarehouseRecord) => void;
  onDeactivate: (warehouse: WarehouseRecord) => void;
  onDelete: (warehouse: WarehouseRecord) => void;
  onEdit: (warehouse: WarehouseRecord) => void;
  onOpenDetail: (warehouse: WarehouseRecord) => void;
}): ColumnsType<WarehouseRecord> {
  return [
    { title: '仓库编码', dataIndex: 'code' },
    { title: '仓库名称', dataIndex: 'name' },
    { title: '仓库类型', dataIndex: 'type', render: (value: unknown) => getOptionLabel(warehouseTypeOptions, String(value)) },
    { title: '地址', dataIndex: 'address', render: (value: unknown) => value || '-' },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      render: (_, warehouse) => (
        <Space>
          <Button type="link" aria-label={`库位 ${warehouse.name}`} onClick={() => handlers.onOpenDetail(warehouse)}>
            库位
          </Button>
          {handlers.actions.canUpdateWarehouse ? (
            <Button type="link" aria-label={`编辑 ${warehouse.name}`} onClick={() => handlers.onEdit(warehouse)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canDeactivateWarehouse && warehouse.status === 'ACTIVE' ? (
            <Popconfirm title="确认停用该仓库？" okText="确认停用" cancelText="取消" onConfirm={() => handlers.onDeactivate(warehouse)}>
              <Button type="link" aria-label={`停用 ${warehouse.name}`}>
                停用
              </Button>
            </Popconfirm>
          ) : null}
          {handlers.actions.canActivateWarehouse && warehouse.status === 'INACTIVE' ? (
            <Button type="link" aria-label={`启用 ${warehouse.name}`} onClick={() => handlers.onActivate(warehouse)}>
              启用
            </Button>
          ) : null}
          {handlers.actions.canDeleteWarehouse ? (
            <Popconfirm title="确认删除该仓库？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(warehouse)}>
              <Button danger type="link" aria-label={`删除 ${warehouse.name}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function buildLocationColumns(handlers: {
  actions: WarehouseActions;
  onDeactivate: (location: LocationRecord) => void;
  onDelete: (location: LocationRecord) => void;
  onEdit: (location: LocationRecord) => void;
  onFreeze: (location: LocationRecord) => void;
  onUnfreeze: (location: LocationRecord) => void;
}): ColumnsType<LocationRecord> {
  return [
    { title: '完整编码', dataIndex: 'fullCode' },
    { title: '库位类型', dataIndex: 'locationType', render: (value: unknown) => getOptionLabel(locationTypeOptions, String(value)) },
    { title: '容量', dataIndex: 'capacity', render: (value: unknown) => value ?? 0 },
    { title: '已用', dataIndex: 'usedCapacity', render: (value: unknown) => value ?? 0 },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      render: (_, location) => (
        <Space>
          {handlers.actions.canUpdateLocation ? (
            <Button type="link" aria-label={`编辑库位 ${location.fullCode}`} onClick={() => handlers.onEdit(location)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canFreezeLocation && location.status !== 'FROZEN' ? (
            <Button type="link" aria-label={`冻结库位 ${location.fullCode}`} onClick={() => handlers.onFreeze(location)}>
              冻结
            </Button>
          ) : null}
          {handlers.actions.canUnfreezeLocation && location.status === 'FROZEN' ? (
            <Button type="link" aria-label={`解冻库位 ${location.fullCode}`} onClick={() => handlers.onUnfreeze(location)}>
              解冻
            </Button>
          ) : null}
          {handlers.actions.canDeactivateLocation && location.status !== 'INACTIVE' ? (
            <Button type="link" aria-label={`停用库位 ${location.fullCode}`} onClick={() => handlers.onDeactivate(location)}>
              停用
            </Button>
          ) : null}
          {handlers.actions.canDeleteLocation ? (
            <Popconfirm title="确认删除该库位？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(location)}>
              <Button danger type="link" aria-label={`删除库位 ${location.fullCode}`}>
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
  if (status === 'FROZEN') {
    return <Tag color="orange">冻结</Tag>;
  }
  if (status === 'INACTIVE') {
    return <Tag color="default">停用</Tag>;
  }
  return <Tag>{status || '-'}</Tag>;
}

function getWarehouseActions(permissions: string[]): WarehouseActions {
  return {
    canActivateWarehouse: permissions.includes('master:warehouse:activate'),
    canCreateLocation: permissions.includes('master:location:create'),
    canCreateWarehouse: permissions.includes('master:warehouse:create'),
    canDeactivateLocation: permissions.includes('master:location:deactivate'),
    canDeactivateWarehouse: permissions.includes('master:warehouse:deactivate'),
    canDeleteLocation: permissions.includes('master:location:delete'),
    canDeleteWarehouse: permissions.includes('master:warehouse:delete'),
    canFreezeLocation: permissions.includes('master:location:freeze'),
    canUnfreezeLocation: permissions.includes('master:location:unfreeze'),
    canUpdateLocation: permissions.includes('master:location:update'),
    canUpdateWarehouse: permissions.includes('master:warehouse:update'),
  };
}

function getOptionLabel(options: { label: string; value: string }[], value: string) {
  return options.find((option) => option.value === value)?.label ?? value;
}

function toCreateWarehousePayload(values: WarehouseFormValues): CreateWarehousePayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    type: values.type,
    address: values.address?.trim(),
    managerId: values.managerId?.trim(),
    remark: values.remark?.trim(),
  };
}

function toUpdateWarehousePayload(values: WarehouseFormValues): UpdateWarehousePayload {
  return {
    name: values.name?.trim(),
    address: values.address?.trim(),
    managerId: values.managerId?.trim(),
    remark: values.remark?.trim(),
  };
}

function toCreateLocationPayload(values: LocationFormValues): CreateLocationPayload {
  return {
    zoneCode: values.zoneCode?.trim() ?? '',
    rackCode: values.rackCode?.trim() ?? '',
    rowCode: values.rowCode?.trim() ?? '',
    binCode: values.binCode?.trim() ?? '',
    locationType: values.locationType,
    capacity: values.capacity,
    remark: values.remark?.trim(),
  };
}

function toUpdateLocationPayload(values: LocationFormValues): UpdateLocationPayload {
  return {
    capacity: values.capacity,
    remark: values.remark?.trim(),
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
