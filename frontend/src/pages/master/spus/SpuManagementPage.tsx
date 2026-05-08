import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, TreeSelect } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { listCategoryTree, type CategoryRecord } from '@/services/master/categoryService';
import { listSizeGroups, type SizeGroupRecord } from '@/services/master/sizeGroupService';
import {
  addSpuColors,
  createSpu,
  deactivateSku,
  deleteSpu,
  getSpuDetail,
  listSpus,
  updateSkuPrice,
  updateSpu,
  type ColorItemPayload,
  type CreateSpuPayload,
  type SkuRecord,
  type SpuQueryParams,
  type SpuRecord,
  type UpdateSkuPricePayload,
  type UpdateSpuPayload,
} from '@/services/master/spuService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const NAME_MAX_LENGTH = 128;
const CODE_MAX_LENGTH = 32;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
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

interface SpuFormValues {
  categoryId?: string;
  colorCode?: string;
  colorName?: string;
  designImage?: string;
  name?: string;
  remark?: string;
  sizeGroupId?: string;
  status?: string;
}

interface PriceFormValues {
  costPrice?: number;
  salePrice?: number;
  wholesalePrice?: number;
}

type FormMode = 'create' | 'edit';

interface CategoryTreeOption {
  children?: CategoryTreeOption[];
  title: string;
  value: string;
}

export function SpuManagementPage() {
  const { message } = App.useApp();
  const [spuForm] = Form.useForm<SpuFormValues>();
  const [colorForm] = Form.useForm<ColorItemPayload>();
  const [priceForm] = Form.useForm<PriceFormValues>();
  const [status, setStatus] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [spus, setSpus] = useState<SpuRecord[]>([]);
  const [categories, setCategories] = useState<CategoryRecord[]>([]);
  const [sizeGroups, setSizeGroups] = useState<SizeGroupRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [spuFormOpen, setSpuFormOpen] = useState(false);
  const [spuFormMode, setSpuFormMode] = useState<FormMode>('create');
  const [editingSpu, setEditingSpu] = useState<SpuRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedSpu, setSelectedSpu] = useState<SpuRecord | null>(null);
  const [spuDetail, setSpuDetail] = useState<SpuRecord | null>(null);
  const [colorFormOpen, setColorFormOpen] = useState(false);
  const [priceFormOpen, setPriceFormOpen] = useState(false);
  const [pricingSku, setPricingSku] = useState<SkuRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getSpuActions(permissions);
  const categoryTreeData = useMemo(() => buildCategoryTreeData(categories), [categories]);
  const categoryNameById = useMemo(() => buildCategoryNameMap(categories), [categories]);
  const sizeGroupNameById = useMemo(() => buildSizeGroupNameMap(sizeGroups), [sizeGroups]);

  const loadSpus = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      const [records, categoryTree, activeSizeGroups] = await Promise.all([
        listSpus(buildQuery({ status, categoryId })),
        listCategoryTree(),
        listSizeGroups({ status: 'ACTIVE' }),
      ]);
      setSpus(records);
      setCategories(categoryTree);
      setSizeGroups(activeSizeGroups);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [categoryId, status]);

  useEffect(() => {
    void loadSpus();
  }, [loadSpus]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const openCreateSpuForm = () => {
    setSpuFormMode('create');
    setEditingSpu(null);
    spuForm.resetFields();
    setSpuFormOpen(true);
  };

  function openEditSpuForm(spu: SpuRecord) {
    setSpuFormMode('edit');
    setEditingSpu(spu);
    spuForm.setFieldsValue({
      categoryId: spu.categoryId ?? undefined,
      designImage: spu.designImage ?? '',
      name: spu.name,
      remark: spu.remark ?? '',
      status: spu.status,
    });
    setSpuFormOpen(true);
  }

  async function openDetail(spu: SpuRecord) {
    setSelectedSpu(spu);
    setSpuDetail(null);
    setDetailOpen(true);
    await loadSpuDetail(spu.id);
  }

  async function handleSaveSpu() {
    try {
      const values = await spuForm.validateFields();
      setSaving(true);
      if (spuFormMode === 'create') {
        await createSpu(toCreateSpuPayload(values));
        message.success('款式创建成功');
      } else if (editingSpu) {
        await updateSpu(editingSpu.id, toUpdateSpuPayload(values));
        message.success('款式更新成功');
      }
      setSpuFormOpen(false);
      await loadSpus();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteSpu(spu: SpuRecord) {
    try {
      await deleteSpu(spu.id);
      message.success('款式已删除');
      await loadSpus();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  function openAddColorForm() {
    colorForm.resetFields();
    setColorFormOpen(true);
  }

  async function handleAddColor() {
    if (!selectedSpu) {
      return;
    }

    try {
      const values = await colorForm.validateFields();
      setSaving(true);
      await addSpuColors(selectedSpu.id, [toColorPayload(values)]);
      message.success('颜色已追加');
      setColorFormOpen(false);
      await loadSpuDetail(selectedSpu.id);
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  function openPriceForm(sku: SkuRecord) {
    setPricingSku(sku);
    priceForm.setFieldsValue({
      costPrice: sku.costPrice ?? undefined,
      salePrice: sku.salePrice ?? undefined,
      wholesalePrice: sku.wholesalePrice ?? undefined,
    });
    setPriceFormOpen(true);
  }

  async function handleUpdatePrice() {
    if (!pricingSku || !selectedSpu) {
      return;
    }

    try {
      const values = await priceForm.validateFields();
      setSaving(true);
      await updateSkuPrice(toUpdateSkuPricePayload(pricingSku.id, values));
      message.success('SKU 价格已更新');
      setPriceFormOpen(false);
      await loadSpuDetail(selectedSpu.id);
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleDeactivateSku(sku: SkuRecord) {
    if (!selectedSpu) {
      return;
    }

    try {
      await deactivateSku(sku.id);
      message.success('SKU 已停用');
      await loadSpuDetail(selectedSpu.id);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function loadSpuDetail(spuId: string) {
    setDetailLoading(true);
    try {
      setSpuDetail(await getSpuDetail(spuId));
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

  if (loading && spus.length === 0) {
    return <LoadingState message="正在加载款式数据" />;
  }

  if (errorMessage && spus.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadSpus} />;
  }

  return (
    <div className="system-page spu-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>SPU/SKU 管理</h1>
          <p>维护服装款式、颜色和尺码 SKU，作为订单、库存和生产业务的商品基础。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建款式" onClick={openCreateSpuForm}>
            新建款式
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <label htmlFor="spu-status-filter" style={visuallyHiddenStyle}>
            款式状态筛选
          </label>
          <Select id="spu-status-filter" value={status} options={statusOptions} onChange={(value) => setStatus(value)} />
          <TreeSelect
            allowClear
            aria-label="款式分类筛选"
            placeholder="全部分类"
            value={categoryId || undefined}
            treeData={categoryTreeData}
            treeDefaultExpandAll
            onChange={(value) => setCategoryId(value ?? '')}
          />
          <Button icon={<ReloadOutlined />} onClick={loadSpus}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadSpus} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<SpuRecord>
          rowKey="id"
          columns={buildSpuColumns({
            actions,
            categoryNameById,
            onDelete: handleDeleteSpu,
            onEdit: openEditSpuForm,
            onOpenDetail: openDetail,
            sizeGroupNameById,
          })}
          dataSource={spus}
          loading={loading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无款式" /> }}
        />
      </ProCard>

      <Modal
        title={spuFormMode === 'create' ? '新建款式' : '编辑款式'}
        open={spuFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setSpuFormOpen(false)}
        onOk={handleSaveSpu}
        destroyOnHidden
      >
        <Form<SpuFormValues> form={spuForm} layout="vertical" preserve={false}>
          <Form.Item
            label="款式名称"
            name="name"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入款式名称' },
              { max: NAME_MAX_LENGTH, message: `款式名称最长${NAME_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="物料分类" name="categoryId">
            <TreeSelect allowClear aria-label="物料分类" treeData={categoryTreeData} treeDefaultExpandAll />
          </Form.Item>
          {spuFormMode === 'create' ? (
            <Form.Item label="尺码组" name="sizeGroupId" rules={[{ required: true, message: '请选择尺码组' }]}>
              <Select aria-label="尺码组" options={sizeGroups.map((item) => ({ label: item.name, value: item.id }))} />
            </Form.Item>
          ) : (
            <Form.Item label="状态" name="status">
              <Select aria-label="状态" options={statusOptions.slice(1)} />
            </Form.Item>
          )}
          {spuFormMode === 'create' ? (
            <>
              <Form.Item
                label="颜色名称"
                name="colorName"
                normalize={normalizeTextInput}
                rules={[{ required: true, whitespace: true, message: '请输入颜色名称' }]}
              >
                <Input allowClear maxLength={NAME_MAX_LENGTH} />
              </Form.Item>
              <Form.Item
                label="颜色编码"
                name="colorCode"
                normalize={normalizeTextInput}
                rules={[
                  { required: true, whitespace: true, message: '请输入颜色编码' },
                  { max: CODE_MAX_LENGTH, message: `颜色编码最长${CODE_MAX_LENGTH}个字符` },
                ]}
              >
                <Input allowClear maxLength={CODE_MAX_LENGTH} />
              </Form.Item>
            </>
          ) : null}
          <Form.Item label="设计图" name="designImage" normalize={normalizeTextInput}>
            <Input allowClear />
          </Form.Item>
          <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
            <Input.TextArea rows={3} maxLength={256} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={selectedSpu ? `${selectedSpu.name} 详情` : '款式详情'}
        open={detailOpen}
        footer={null}
        onCancel={() => setDetailOpen(false)}
        width={980}
        destroyOnHidden
      >
        <Space className="system-table-toolbar">
          {actions.canAddColor ? (
            <Button type="primary" icon={<PlusOutlined />} aria-label="追加颜色" onClick={openAddColorForm}>
              追加颜色
            </Button>
          ) : null}
        </Space>
        <Table
          rowKey="id"
          columns={colorColumns}
          dataSource={spuDetail?.colorWays ?? []}
          loading={detailLoading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无颜色" /> }}
        />
        <Table<SkuRecord>
          className="system-nested-table"
          rowKey="id"
          columns={buildSkuColumns({ actions, onDeactivate: handleDeactivateSku, onUpdatePrice: openPriceForm })}
          dataSource={spuDetail?.skus ?? []}
          loading={detailLoading}
          pagination={false}
          locale={{ emptyText: <EmptyState message="暂无 SKU" /> }}
        />
      </Modal>

      <Modal
        title="追加颜色"
        open={colorFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setColorFormOpen(false)}
        onOk={handleAddColor}
        destroyOnHidden
      >
        <Form<ColorItemPayload> form={colorForm} layout="vertical" preserve={false}>
          <Form.Item
            label="颜色名称"
            name="colorName"
            normalize={normalizeTextInput}
            rules={[{ required: true, whitespace: true, message: '请输入颜色名称' }]}
          >
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item
            label="颜色编码"
            name="colorCode"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入颜色编码' },
              { max: CODE_MAX_LENGTH, message: `颜色编码最长${CODE_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={CODE_MAX_LENGTH} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={pricingSku ? `改价 ${pricingSku.code}` : 'SKU 改价'}
        open={priceFormOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setPriceFormOpen(false)}
        onOk={handleUpdatePrice}
        destroyOnHidden
      >
        <Form<PriceFormValues> form={priceForm} layout="vertical" preserve={false}>
          <Form.Item label="成本价" name="costPrice">
            <InputNumber className="system-number-input" min={0} precision={2} />
          </Form.Item>
          <Form.Item label="销售价" name="salePrice">
            <InputNumber className="system-number-input" min={0} precision={2} />
          </Form.Item>
          <Form.Item label="批发价" name="wholesalePrice">
            <InputNumber className="system-number-input" min={0} precision={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface SpuActions {
  canAddColor: boolean;
  canCreate: boolean;
  canDelete: boolean;
  canDeactivateSku: boolean;
  canUpdate: boolean;
  canUpdateSkuPrice: boolean;
}

function buildSpuColumns(handlers: {
  actions: SpuActions;
  categoryNameById: Map<string, string>;
  onDelete: (spu: SpuRecord) => void;
  onEdit: (spu: SpuRecord) => void;
  onOpenDetail: (spu: SpuRecord) => void;
  sizeGroupNameById: Map<string, string>;
}): ColumnsType<SpuRecord> {
  return [
    { title: '款式编码', dataIndex: 'code' },
    { title: '款式名称', dataIndex: 'name' },
    { title: '分类', dataIndex: 'categoryId', render: (value: unknown) => handlers.categoryNameById.get(String(value)) ?? '-' },
    { title: '尺码组', dataIndex: 'sizeGroupId', render: (value: unknown) => handlers.sizeGroupNameById.get(String(value)) ?? '-' },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <SpuStatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, spu) => (
        <Space>
          <Button type="link" aria-label={`详情 ${spu.name}`} onClick={() => handlers.onOpenDetail(spu)}>
            详情
          </Button>
          {handlers.actions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${spu.name}`} onClick={() => handlers.onEdit(spu)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canDelete ? (
            <Popconfirm title="确认删除该款式？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(spu)}>
              <Button danger type="link" aria-label={`删除 ${spu.name}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

const colorColumns: ColumnsType<{ colorCode: string; colorName: string; id: string; sortOrder?: number | null }> = [
  { title: '颜色编码', dataIndex: 'colorCode' },
  { title: '颜色名称', dataIndex: 'colorName' },
  { title: '排序号', dataIndex: 'sortOrder', render: (value: unknown) => value ?? 0 },
];

function buildSkuColumns(handlers: {
  actions: SpuActions;
  onDeactivate: (sku: SkuRecord) => void;
  onUpdatePrice: (sku: SkuRecord) => void;
}): ColumnsType<SkuRecord> {
  return [
    { title: 'SKU 编码', dataIndex: 'code' },
    { title: '销售价', dataIndex: 'salePrice', render: (value: unknown) => formatPrice(value) },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <SpuStatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, sku) => (
        <Space>
          {handlers.actions.canUpdateSkuPrice ? (
            <Button type="link" aria-label={`改价 ${sku.code}`} onClick={() => handlers.onUpdatePrice(sku)}>
              改价
            </Button>
          ) : null}
          {handlers.actions.canDeactivateSku && sku.status !== 'INACTIVE' ? (
            <Popconfirm title="确认停用该 SKU？" okText="确认停用" cancelText="取消" onConfirm={() => handlers.onDeactivate(sku)}>
              <Button danger type="link" aria-label={`停用SKU ${sku.code}`}>
                停用
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function SpuStatusTag({ status }: { status: string }) {
  const colorByStatus: Record<string, string> = { ACTIVE: 'green', DRAFT: 'blue', INACTIVE: 'default' };
  const labelByStatus: Record<string, string> = { ACTIVE: '启用', DRAFT: '草稿', INACTIVE: '停用' };
  return <Tag color={colorByStatus[status]}>{labelByStatus[status] ?? status}</Tag>;
}

function getSpuActions(permissions: string[]): SpuActions {
  return {
    canCreate: permissions.includes('master:spu:create'),
    canUpdate: permissions.includes('master:spu:update'),
    canDelete: permissions.includes('master:spu:deactivate'),
    canAddColor: permissions.includes('master:spu:addColor'),
    canUpdateSkuPrice: permissions.includes('master:sku:updatePrice'),
    canDeactivateSku: permissions.includes('master:sku:deactivate'),
  };
}

function buildQuery(values: SpuQueryParams): SpuQueryParams {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value)) as SpuQueryParams;
}

function buildCategoryTreeData(categories: CategoryRecord[]): CategoryTreeOption[] {
  return categories.map((category) => ({
    title: category.name,
    value: category.id,
    children: buildCategoryTreeData(category.children ?? []),
  }));
}

function buildCategoryNameMap(categories: CategoryRecord[]): Map<string, string> {
  const entries = categories.flatMap((category): Array<[string, string]> => [
    [category.id, category.name],
    ...Array.from(buildCategoryNameMap(category.children ?? [])),
  ]);
  return new Map(entries);
}

function buildSizeGroupNameMap(sizeGroups: SizeGroupRecord[]): Map<string, string> {
  return new Map(sizeGroups.map((item) => [item.id, item.name]));
}

function toCreateSpuPayload(values: SpuFormValues): CreateSpuPayload {
  return {
    name: values.name?.trim() ?? '',
    ...(values.categoryId ? { categoryId: values.categoryId } : {}),
    sizeGroupId: values.sizeGroupId ?? '',
    ...(values.designImage ? { designImage: values.designImage.trim() } : {}),
    ...(values.remark ? { remark: values.remark.trim() } : {}),
    colors: [toColorPayload({ colorName: values.colorName ?? '', colorCode: values.colorCode ?? '' })],
  };
}

function toUpdateSpuPayload(values: SpuFormValues): UpdateSpuPayload {
  return {
    name: values.name?.trim(),
    categoryId: values.categoryId,
    designImage: values.designImage?.trim(),
    remark: values.remark?.trim(),
    status: values.status,
  };
}

function toColorPayload(values: ColorItemPayload): ColorItemPayload {
  return {
    colorName: values.colorName.trim(),
    colorCode: values.colorCode.trim(),
  };
}

function toUpdateSkuPricePayload(skuId: string, values: PriceFormValues): UpdateSkuPricePayload {
  return {
    skuId,
    ...(values.costPrice !== undefined ? { costPrice: values.costPrice } : {}),
    ...(values.salePrice !== undefined ? { salePrice: values.salePrice } : {}),
    ...(values.wholesalePrice !== undefined ? { wholesalePrice: values.wholesalePrice } : {}),
  };
}

function formatPrice(value: unknown) {
  return typeof value === 'number' ? value.toFixed(2) : '-';
}

function normalizeTextInput(value: unknown) {
  return typeof value === 'string' ? value.trim() : value;
}

function isFormValidationError(error: unknown) {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}
