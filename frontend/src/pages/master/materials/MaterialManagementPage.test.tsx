import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MaterialManagementPage } from './MaterialManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { listCategoryTree } from '@/services/master/categoryService';
import {
  createMaterial,
  deactivateMaterial,
  getMaterialAttributeDefs,
  listMaterials,
  updateMaterial,
} from '@/services/master/materialService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/categoryService', () => ({
  listCategoryTree: vi.fn(),
}));

vi.mock('@/services/master/materialService', () => ({
  createMaterial: vi.fn(),
  deactivateMaterial: vi.fn(),
  getMaterialAttributeDefs: vi.fn(),
  listMaterials: vi.fn(),
  updateMaterial: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCreateMaterial = vi.mocked(createMaterial);
const mockedDeactivateMaterial = vi.mocked(deactivateMaterial);
const mockedGetMaterialAttributeDefs = vi.mocked(getMaterialAttributeDefs);
const mockedListCategoryTree = vi.mocked(listCategoryTree);
const mockedListMaterials = vi.mocked(listMaterials);
const mockedUpdateMaterial = vi.mocked(updateMaterial);
const materialPermissions = [
  'master:material:create',
  'master:material:update',
  'master:material:deactivate',
  'master:material:attributeDefs',
];

const categoryTree = [
  {
    id: '270',
    parentId: null,
    code: 'FABRIC',
    name: '面料',
    level: 1,
    sortOrder: 1,
    status: 'ACTIVE',
    children: [{ id: '271', parentId: '270', code: 'COTTON', name: '棉布', level: 2, sortOrder: 1, status: 'ACTIVE', children: [] }],
  },
];

const materialPage = {
  records: [
    {
      id: '2051932034979037191',
      code: 'MAT-000001',
      name: '40支棉布',
      type: 'FABRIC',
      categoryId: '271',
      unit: '米',
      status: 'ACTIVE',
      extAttrs: { width: '150cm' },
      remark: '主面料',
    },
    {
      id: '2051932034979037192',
      code: 'MAT-000002',
      name: '纽扣',
      type: 'TRIM',
      categoryId: '271',
      unit: '个',
      status: 'INACTIVE',
      extAttrs: {},
    },
  ],
  total: 2,
  current: 1,
  size: 10,
  pages: 1,
};

const fabricAttributeDefs = [
  {
    id: '1',
    code: 'width',
    name: '门幅',
    materialType: 'FABRIC',
    inputType: 'TEXT',
    required: true,
    sortOrder: 1,
    options: [],
    extJsonPath: '$.width',
  },
];

describe('MaterialManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: materialPermissions });
    mockedCreateMaterial.mockReset();
    mockedDeactivateMaterial.mockReset();
    mockedGetMaterialAttributeDefs.mockReset();
    mockedGetMaterialAttributeDefs.mockResolvedValue(fabricAttributeDefs);
    mockedListCategoryTree.mockReset();
    mockedListCategoryTree.mockResolvedValue(categoryTree);
    mockedListMaterials.mockReset();
    mockedUpdateMaterial.mockReset();
  });

  it('loads and renders paged materials', async () => {
    mockedListMaterials.mockResolvedValue(materialPage);

    renderPage();

    expect(screen.getByText('正在加载物料数据')).toBeInTheDocument();
    expect(await screen.findByText('40支棉布')).toBeInTheDocument();
    expect(screen.getByText('MAT-000001')).toBeInTheDocument();
    expect(screen.getByText('面料')).toBeInTheDocument();
    expect(screen.getAllByText('棉布')).toHaveLength(2);
  });

  it('searches and filters materials', async () => {
    mockedListMaterials.mockResolvedValue(materialPage);

    renderPage();

    await screen.findByText('40支棉布');
    fireEvent.change(screen.getByPlaceholderText('搜索物料编码/名称'), { target: { value: ' 棉 ' } });
    fireEvent.click(screen.getByRole('button', { name: /查询/ }));

    await waitFor(() =>
      expect(mockedListMaterials).toHaveBeenLastCalledWith({
        current: 1,
        size: 10,
        keyword: '棉',
      }),
    );
  });

  it('creates and updates materials with dynamic attributes', async () => {
    mockedListMaterials.mockResolvedValue(materialPage);
    mockedCreateMaterial.mockResolvedValue(materialPage.records[0]);
    mockedUpdateMaterial.mockResolvedValue(materialPage.records[0]);

    renderPage();

    await screen.findByText('40支棉布');
    fireEvent.click(screen.getByRole('button', { name: '新建物料' }));
    fireEvent.change(screen.getByLabelText('物料名称'), { target: { value: ' 32支棉布 ' } });
    openDialogSelect('物料类型');
    await chooseOption('面料');
    openDialogSelect('物料分类');
    await chooseTreeOption('棉布');
    fireEvent.change(screen.getByLabelText('基本单位'), { target: { value: ' 米 ' } });
    expect(await screen.findByLabelText('门幅')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('门幅'), { target: { value: ' 150cm ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateMaterial).toHaveBeenCalledWith({
        name: '32支棉布',
        type: 'FABRIC',
        categoryId: '271',
        unit: '米',
        extAttrs: { width: '150cm' },
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 40支棉布' }));
    fireEvent.change(screen.getByLabelText('物料名称'), { target: { value: ' 40支棉布更新 ' } });
    fireEvent.change(await screen.findByLabelText('门幅'), { target: { value: ' 160cm ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateMaterial).toHaveBeenCalledWith(
        '2051932034979037191',
        expect.objectContaining({ name: '40支棉布更新', extAttrs: { width: '160cm' } }),
      ),
    );
  });

  it('deactivates materials after confirmation', async () => {
    mockedListMaterials.mockResolvedValue(materialPage);
    mockedDeactivateMaterial.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('40支棉布');
    fireEvent.click(screen.getByRole('button', { name: '停用 40支棉布' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));

    await waitFor(() => expect(mockedDeactivateMaterial).toHaveBeenCalledWith('2051932034979037191'));
  });

  it('hides material actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });
    mockedListMaterials.mockResolvedValue(materialPage);

    renderPage();

    await screen.findByText('40支棉布');
    expect(screen.queryByRole('button', { name: '新建物料' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 40支棉布' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <MaterialManagementPage />
    </AntdApp>,
  );
}

function openDialogSelect(label: string) {
  const matches = within(screen.getByRole('dialog')).getAllByLabelText(label);
  const select = findSelectContainer(matches);
  const input = select.querySelector('input') ?? select;
  fireEvent.focus(input);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
  fireEvent.keyDown(input, { key: 'ArrowDown', code: 'ArrowDown' });
}

function findSelectContainer(elements: HTMLElement[]): HTMLElement {
  return (
    elements.find((element) => element.classList.contains('ant-select')) ??
    elements.map((element) => element.closest<HTMLElement>('.ant-select')).find(Boolean) ??
    elements.at(-1)!
  );
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  const option =
    optionLabel.closest('.ant-select-item-option') ??
    optionLabel.closest('.ant-select-tree-node-content-wrapper') ??
    optionLabel;
  fireEvent.mouseDown(option);
  fireEvent.click(option);
}

async function chooseTreeOption(label: string) {
  const option = await waitFor(() => {
    const treeOption = Array.from(document.querySelectorAll('.ant-select-tree-title')).find(
      (element) => element.textContent === label,
    );
    if (!treeOption) {
      throw new Error(`Tree option ${label} was not found`);
    }
    return treeOption;
  });
  fireEvent.click(option);
}
