import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CategoryManagementPage } from './CategoryManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { createCategory, deleteCategory, listCategoryTree, updateCategory } from '@/services/master/categoryService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/categoryService', () => ({
  createCategory: vi.fn(),
  deleteCategory: vi.fn(),
  listCategoryTree: vi.fn(),
  updateCategory: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCreateCategory = vi.mocked(createCategory);
const mockedDeleteCategory = vi.mocked(deleteCategory);
const mockedListCategoryTree = vi.mocked(listCategoryTree);
const mockedUpdateCategory = vi.mocked(updateCategory);
const categoryPermissions = ['master:category:create', 'master:category:update', 'master:category:delete'];

const categoryTree = [
  {
    id: '270',
    parentId: null,
    code: 'FABRIC',
    name: '面料',
    level: 1,
    sortOrder: 1,
    status: 'ACTIVE',
    children: [
      {
        id: '271',
        parentId: '270',
        code: 'COTTON',
        name: '棉布',
        level: 2,
        sortOrder: 1,
        status: 'ACTIVE',
        children: [],
      },
    ],
  },
  {
    id: '280',
    parentId: null,
    code: 'TRIM',
    name: '辅料',
    level: 1,
    sortOrder: 2,
    status: 'INACTIVE',
    children: [],
  },
];

describe('CategoryManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: categoryPermissions });
    mockedCreateCategory.mockReset();
    mockedDeleteCategory.mockReset();
    mockedListCategoryTree.mockReset();
    mockedUpdateCategory.mockReset();
  });

  it('loads and renders category tree', async () => {
    mockedListCategoryTree.mockResolvedValue(categoryTree);

    renderPage();

    expect(screen.getByText('正在加载物料分类')).toBeInTheDocument();
    expect(await screen.findByText('面料')).toBeInTheDocument();
    expect(screen.getByText('棉布')).toBeInTheDocument();
    expect(screen.getByText('FABRIC')).toBeInTheDocument();
    expect(screen.getByText('停用')).toBeInTheDocument();
  });

  it('creates and updates categories', async () => {
    mockedListCategoryTree.mockResolvedValue(categoryTree);
    mockedCreateCategory.mockResolvedValue(categoryTree[0]);
    mockedUpdateCategory.mockResolvedValue(categoryTree[0]);

    renderPage();

    await screen.findByText('面料');
    fireEvent.click(screen.getByRole('button', { name: '新建分类' }));
    fireEvent.change(screen.getByLabelText('分类编码'), { target: { value: ' KNIT ' } });
    fireEvent.change(screen.getByLabelText('分类名称'), { target: { value: ' 针织面料 ' } });
    fireEvent.change(screen.getByLabelText('排序号'), { target: { value: '3' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateCategory).toHaveBeenCalledWith({
        code: 'KNIT',
        name: '针织面料',
        sortOrder: 3,
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 面料' }));
    fireEvent.change(screen.getByLabelText('分类名称'), { target: { value: ' 面料更新 ' } });
    openDialogSelect('状态');
    await chooseOption('停用');
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateCategory).toHaveBeenCalledWith(
        '270',
        expect.objectContaining({ name: '面料更新', status: 'INACTIVE' }),
      ),
    );
  });

  it('deletes categories after confirmation', async () => {
    mockedListCategoryTree.mockResolvedValue(categoryTree);
    mockedDeleteCategory.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('面料');
    fireEvent.click(screen.getByRole('button', { name: '删除 辅料' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeleteCategory).toHaveBeenCalledWith('280'));
  });

  it('hides category actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });
    mockedListCategoryTree.mockResolvedValue(categoryTree);

    renderPage();

    await screen.findByText('面料');
    expect(screen.queryByRole('button', { name: '新建分类' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 面料' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <CategoryManagementPage />
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
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
