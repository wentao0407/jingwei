import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  createSize,
  createSizeGroup,
  deleteSize,
  deleteSizeGroup,
  getSizeGroupDetail,
  listSizeGroups,
  updateSize,
  updateSizeGroup,
  type CreateSizeGroupPayload,
  type CreateSizePayload,
  type SizeGroupQueryParams,
  type SizeGroupRecord,
  type SizeRecord,
  type UpdateSizeGroupPayload,
  type UpdateSizePayload,
} from '@/services/master/sizeGroupService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const CODE_MAX_LENGTH = 32;
const NAME_MAX_LENGTH = 64;

const categoryOptions = [
  { label: '全部品类', value: '' },
  { label: '女装', value: 'WOMEN' },
  { label: '男装', value: 'MEN' },
  { label: '童装', value: 'CHILDREN' },
];

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const visuallyHiddenStyle = {
  clip: 'rect(0 0 0 0)',
  height: 1,
  overflow: 'hidden',
  position: 'absolute',
  width: 1,
} as const;

type GroupFormValues = CreateSizeGroupPayload & UpdateSizeGroupPayload;
type SizeFormValues = CreateSizePayload & UpdateSizePayload;
type FormMode = 'create' | 'edit';

export function SizeGroupManagementPage() {
  const { message } = App.useApp();
  const [groupForm] = Form.useForm<GroupFormValues>();
  const [sizeForm] = Form.useForm<SizeFormValues>();
  const [category, setCategory] = useState('');
  const [status, setStatus] = useState('');
  const [groups, setGroups] = useState<SizeGroupRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [groupFormOpen, setGroupFormOpen] = useState(false);
  const [groupFormMode, setGroupFormMode] = useState<FormMode>('create');
  const [editingGroup, setEditingGroup] = useState<SizeGroupRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<SizeGroupRecord | null>(null);
  const [groupDetail, setGroupDetail] = useState<SizeGroupRecord | null>(null);
  const [sizeFormOpen, setSizeFormOpen] = useState(false);
  const [sizeFormMode, setSizeFormMode] = useState<FormMode>('create');
  const [editingSize, setEditingSize] = useState<SizeRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getSizeGroupActions(permissions);

  const loadGroups = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setGroups(await listSizeGroups(buildQuery({ category, status })));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [category, status]);

  useEffect(() => {
    void loadGroups();
  }, [loadGroups]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const openCreateGroupForm = () => {
    setGroupFormMode('create');
    setEditingGroup(null);
    groupForm.resetFields();
    setGroupFormOpen(true);
  };

  function openEditGroupForm(group: SizeGroupRecord) {
    setGroupFormMode('edit');
    setEditingGroup(group);
    groupForm.setFieldsValue({
      name: group.name,
      category: group.category,
      status: group.status,
    });
    setGroupFormOpen(true);
  }

  async function openDetail(group: SizeGroupRecord) {
    setSelectedGroup(group);
    setGroupDetail(null);
    setDetailOpen(true);
    await loadGroupDetail(group.id);
  }

  const handleSaveGroup = async () => {
    try {
      const values = await groupForm.validateFields();
      setSaving(true);
      if (groupFormMode === 'create') {
        await createSizeGroup(toCreateGroupPayload(values));
        message.success('尺码组创建成功');
      } else if (editingGroup) {
        await updateSizeGroup(editingGroup.id, toUpdateGroupPayload(values));
        message.success('尺码组更新成功');
      }
      setGroupFormOpen(false);
      await loadGroups();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  };

  async function handleDeleteGroup(group: SizeGroupRecord) {
    try {
      await deleteSizeGroup(group.id);
      message.success('尺码组已删除');
      await loadGroups();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  function openCreateSizeForm() {
    setSizeFormMode('create');
    setEditingSize(null);
    sizeForm.resetFields();
    setSizeFormOpen(true);
  }

  function openEditSizeForm(size: SizeRecord) {
    setSizeFormMode('edit');
    setEditingSize(size);
    sizeForm.setFieldsValue({ code: size.code, name: size.name, sortOrder: size.sortOrder ?? undefined });
    setSizeFormOpen(true);
  }

  async function handleSaveSize() {
    if (!selectedGroup) {
      return;
    }

    try {
      const values = await sizeForm.validateFields();
      setSaving(true);
      if (sizeFormMode === 'create') {
        await createSize(selectedGroup.id, toCreateSizePayload(values));
        message.success('尺码创建成功');
      } else if (editingSize) {
        await updateSize(editingSize.id, toUpdateSizePayload(values));
        message.success('尺码更新成功');
      }
      setSizeFormOpen(false);
      await loadGroupDetail(selectedGroup.id);
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteSize(size: SizeRecord) {
    if (!selectedGroup) {
      return;
    }

    try {
      await deleteSize(size.id);
      message.success('尺码已删除');
      await loadGroupDetail(selectedGroup.id);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function loadGroupDetail(sizeGroupId: string) {
    setDetailLoading(true);
    try {
      setGroupDetail(await getSizeGroupDetail(sizeGroupId));
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

  if (loading && groups.length === 0) {
    return <LoadingState message="正在加载尺码组" />;
  }

  if (errorMessage && groups.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadGroups} />;
  }

  return (
    <div className="system-page size-group-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>尺码组管理</h1>
          <p>维护女装、男装、童装等尺码组及尺码明细，供 SPU/SKU 自动生成使用。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建尺码组" onClick={openCreateGroupForm}>
            新建尺码组
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <label htmlFor="size-group-category-filter" style={visuallyHiddenStyle}>
            适用品类筛选
          </label>
          <Select
            id="size-group-category-filter"
            value={category}
            options={categoryOptions}
            onChange={(value) => setCategory(value)}
          />
          <Select aria-label="尺码组状态筛选" value={status} options={statusOptions} onChange={(value) => setStatus(value)} />
          <Button icon={<ReloadOutlined />} onClick={loadGroups}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadGroups} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<SizeGroupRecord>
          rowKey="id"
          columns={buildGroupColumns({ actions, onDelete: handleDeleteGroup, onEdit: openEditGroupForm, onOpenDetail: openDetail })}
          dataSource={groups}
          loading={loading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无尺码组" /> }}
        />
      </ProCard>

      <Modal
        title={groupFormMode === 'create' ? '新建尺码组' : '编辑尺码组'}
        open={groupFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setGroupFormOpen(false)}
        onOk={handleSaveGroup}
        destroyOnHidden
      >
        <Form<GroupFormValues> form={groupForm} layout="vertical" preserve={false}>
          {groupFormMode === 'create' ? (
            <Form.Item
              label="尺码组编码"
              name="code"
              normalize={normalizeTextInput}
              rules={[
                { required: true, whitespace: true, message: '请输入尺码组编码' },
                { max: CODE_MAX_LENGTH, message: `尺码组编码最长${CODE_MAX_LENGTH}个字符` },
              ]}
            >
              <Input allowClear maxLength={CODE_MAX_LENGTH} />
            </Form.Item>
          ) : null}
          <Form.Item
            label="尺码组名称"
            name="name"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入尺码组名称' },
              { max: NAME_MAX_LENGTH, message: `尺码组名称最长${NAME_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="适用品类" name="category" rules={[{ required: true, message: '请选择适用品类' }]}>
            <Select aria-label="适用品类" options={categoryOptions.slice(1)} />
          </Form.Item>
          {groupFormMode === 'edit' ? (
            <Form.Item label="状态" name="status">
              <Select aria-label="状态" options={statusOptions.slice(1)} />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>

      <Modal
        title={selectedGroup ? `${selectedGroup.name} 尺码` : '尺码明细'}
        open={detailOpen}
        footer={null}
        onCancel={() => setDetailOpen(false)}
        width={760}
        destroyOnHidden
      >
        <Space className="system-table-toolbar">
          {actions.canCreate ? (
            <Button type="primary" icon={<PlusOutlined />} aria-label="新增尺码" onClick={openCreateSizeForm}>
              新增尺码
            </Button>
          ) : null}
        </Space>
        <Table<SizeRecord>
          rowKey="id"
          columns={buildSizeColumns({ actions, onDelete: handleDeleteSize, onEdit: openEditSizeForm })}
          dataSource={groupDetail?.sizes ?? []}
          loading={detailLoading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无尺码" /> }}
        />
      </Modal>

      <Modal
        title={sizeFormMode === 'create' ? '新增尺码' : '编辑尺码'}
        open={sizeFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setSizeFormOpen(false)}
        onOk={handleSaveSize}
        destroyOnHidden
      >
        <Form<SizeFormValues> form={sizeForm} layout="vertical" preserve={false}>
          <Form.Item
            label="尺码编码"
            name="code"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入尺码编码' },
              { max: CODE_MAX_LENGTH, message: `尺码编码最长${CODE_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={CODE_MAX_LENGTH} />
          </Form.Item>
          <Form.Item
            label="尺码名称"
            name="name"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入尺码名称' },
              { max: NAME_MAX_LENGTH, message: `尺码名称最长${NAME_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="排序号" name="sortOrder">
            <InputNumber className="system-number-input" min={0} precision={0} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface SizeGroupActions {
  canCreate: boolean;
  canDelete: boolean;
  canUpdate: boolean;
}

function buildGroupColumns(handlers: {
  actions: SizeGroupActions;
  onDelete: (group: SizeGroupRecord) => void;
  onEdit: (group: SizeGroupRecord) => void;
  onOpenDetail: (group: SizeGroupRecord) => void;
}): ColumnsType<SizeGroupRecord> {
  return [
    { title: '尺码组编码', dataIndex: 'code' },
    { title: '尺码组名称', dataIndex: 'name' },
    { title: '适用品类', dataIndex: 'category', render: (value: unknown) => getCategoryLabel(String(value)) },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, group) => (
        <Space>
          <Button type="link" aria-label={`尺码 ${group.name}`} onClick={() => handlers.onOpenDetail(group)}>
            尺码
          </Button>
          {handlers.actions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${group.name}`} onClick={() => handlers.onEdit(group)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canDelete ? (
            <Popconfirm title="确认删除该尺码组？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(group)}>
              <Button danger type="link" aria-label={`删除 ${group.name}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function buildSizeColumns(handlers: {
  actions: SizeGroupActions;
  onDelete: (size: SizeRecord) => void;
  onEdit: (size: SizeRecord) => void;
}): ColumnsType<SizeRecord> {
  return [
    { title: '尺码编码', dataIndex: 'code' },
    { title: '尺码名称', dataIndex: 'name', render: (value: unknown, size) => (value === size.code ? '-' : String(value)) },
    { title: '排序号', dataIndex: 'sortOrder', render: (value: unknown) => value ?? 0 },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, size) => (
        <Space>
          {handlers.actions.canUpdate ? (
            <Button type="link" aria-label={`编辑尺码 ${size.code}`} onClick={() => handlers.onEdit(size)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canDelete ? (
            <Popconfirm title="确认删除该尺码？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(size)}>
              <Button danger type="link" aria-label={`删除尺码 ${size.code}`}>
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
  if (status === 'INACTIVE') {
    return <Tag color="default">停用</Tag>;
  }
  return <Tag>{status || '-'}</Tag>;
}

function getSizeGroupActions(permissions: string[]): SizeGroupActions {
  return {
    canCreate: permissions.includes('master:sizeGroup:create'),
    canUpdate: permissions.includes('master:sizeGroup:update'),
    canDelete: permissions.includes('master:sizeGroup:delete'),
  };
}

function buildQuery(values: SizeGroupQueryParams): SizeGroupQueryParams {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value)) as SizeGroupQueryParams;
}

function getCategoryLabel(value: string) {
  return categoryOptions.find((option) => option.value === value)?.label ?? value;
}

function toCreateGroupPayload(values: GroupFormValues): CreateSizeGroupPayload {
  return {
    code: values.code.trim(),
    name: values.name?.trim() ?? '',
    category: values.category?.trim() ?? '',
  };
}

function toUpdateGroupPayload(values: GroupFormValues): UpdateSizeGroupPayload {
  return {
    name: values.name?.trim(),
    category: values.category,
    status: values.status,
  };
}

function toCreateSizePayload(values: SizeFormValues): CreateSizePayload {
  return {
    code: values.code?.trim() ?? '',
    name: values.name?.trim() ?? '',
    sortOrder: values.sortOrder,
  };
}

function toUpdateSizePayload(values: SizeFormValues): UpdateSizePayload {
  return {
    code: values.code?.trim(),
    name: values.name?.trim(),
    sortOrder: values.sortOrder,
  };
}

function normalizeTextInput(value: unknown) {
  return typeof value === 'string' ? value.trim() : value;
}

function isFormValidationError(error: unknown) {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}
