import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SizeGroupManagementPage } from './SizeGroupManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  createSize,
  createSizeGroup,
  deleteSize,
  deleteSizeGroup,
  getSizeGroupDetail,
  listSizeGroups,
  updateSize,
  updateSizeGroup,
} from '@/services/master/sizeGroupService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/sizeGroupService', () => ({
  createSize: vi.fn(),
  createSizeGroup: vi.fn(),
  deleteSize: vi.fn(),
  deleteSizeGroup: vi.fn(),
  getSizeGroupDetail: vi.fn(),
  listSizeGroups: vi.fn(),
  updateSize: vi.fn(),
  updateSizeGroup: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCreateSize = vi.mocked(createSize);
const mockedCreateSizeGroup = vi.mocked(createSizeGroup);
const mockedDeleteSize = vi.mocked(deleteSize);
const mockedDeleteSizeGroup = vi.mocked(deleteSizeGroup);
const mockedGetSizeGroupDetail = vi.mocked(getSizeGroupDetail);
const mockedListSizeGroups = vi.mocked(listSizeGroups);
const mockedUpdateSize = vi.mocked(updateSize);
const mockedUpdateSizeGroup = vi.mocked(updateSizeGroup);

const permissions = ['master:sizeGroup:create', 'master:sizeGroup:update', 'master:sizeGroup:delete'];

const groups = [
  { id: '10001', code: 'WOMEN_STD', name: '女装标准码', category: 'WOMEN', status: 'ACTIVE', sizes: [] },
  { id: '10002', code: 'MEN_STD', name: '男装标准码', category: 'MEN', status: 'INACTIVE', sizes: [] },
];

const groupDetail = {
  ...groups[0],
  sizes: [
    { id: '10101', sizeGroupId: '10001', code: 'S', name: 'S', sortOrder: 1 },
    { id: '10102', sizeGroupId: '10001', code: 'M', name: 'M', sortOrder: 2 },
  ],
};

describe('SizeGroupManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedCreateSize.mockReset();
    mockedCreateSizeGroup.mockReset();
    mockedDeleteSize.mockReset();
    mockedDeleteSizeGroup.mockReset();
    mockedGetSizeGroupDetail.mockReset();
    mockedGetSizeGroupDetail.mockResolvedValue(groupDetail);
    mockedListSizeGroups.mockReset();
    mockedUpdateSize.mockReset();
    mockedUpdateSizeGroup.mockReset();
  });

  it('loads and filters size groups', async () => {
    mockedListSizeGroups.mockResolvedValue(groups);

    renderPage();

    expect(screen.getByText('正在加载尺码组')).toBeInTheDocument();
    expect(await screen.findByText('女装标准码')).toBeInTheDocument();
    openSelect('适用品类筛选');
    await chooseOption('女装');

    await waitFor(() => expect(mockedListSizeGroups).toHaveBeenLastCalledWith({ category: 'WOMEN' }));
  });

  it('creates, updates and deletes size groups', async () => {
    mockedListSizeGroups.mockResolvedValue(groups);
    mockedCreateSizeGroup.mockResolvedValue(groups[0]);
    mockedUpdateSizeGroup.mockResolvedValue(groups[0]);
    mockedDeleteSizeGroup.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('女装标准码');
    fireEvent.click(screen.getByRole('button', { name: '新建尺码组' }));
    fireEvent.change(screen.getByLabelText('尺码组编码'), { target: { value: ' WOMEN_NEW ' } });
    fireEvent.change(screen.getByLabelText('尺码组名称'), { target: { value: ' 新女装 ' } });
    openDialogSelect('适用品类');
    await chooseOption('女装');
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateSizeGroup).toHaveBeenCalledWith({
        code: 'WOMEN_NEW',
        name: '新女装',
        category: 'WOMEN',
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 女装标准码' }));
    fireEvent.change(screen.getByLabelText('尺码组名称'), { target: { value: ' 女装更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateSizeGroup).toHaveBeenCalledWith('10001', expect.objectContaining({ name: '女装更新' })));

    fireEvent.click(screen.getByRole('button', { name: '删除 男装标准码' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeleteSizeGroup).toHaveBeenCalledWith('10002'));
  });

  it('manages sizes inside selected group', async () => {
    mockedListSizeGroups.mockResolvedValue(groups);
    mockedCreateSize.mockResolvedValue(groupDetail.sizes[0]);
    mockedUpdateSize.mockResolvedValue(groupDetail.sizes[1]);
    mockedDeleteSize.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('女装标准码');
    fireEvent.click(screen.getByRole('button', { name: '尺码 女装标准码' }));
    expect(await screen.findByText('S')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '新增尺码' }));
    fireEvent.change(screen.getByLabelText('尺码编码'), { target: { value: ' L ' } });
    fireEvent.change(screen.getByLabelText('尺码名称'), { target: { value: ' 大码 ' } });
    fireEvent.change(screen.getByLabelText('排序号'), { target: { value: '3' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedCreateSize).toHaveBeenCalledWith('10001', { code: 'L', name: '大码', sortOrder: 3 }));

    fireEvent.click(screen.getByRole('button', { name: '编辑尺码 M' }));
    fireEvent.change(screen.getByLabelText('尺码名称'), { target: { value: ' 中码 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateSize).toHaveBeenCalledWith('10102', expect.objectContaining({ name: '中码' })));

    fireEvent.click(screen.getByRole('button', { name: '删除尺码 S' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeleteSize).toHaveBeenCalledWith('10101'));
  });

  it('hides size group actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });
    mockedListSizeGroups.mockResolvedValue(groups);

    renderPage();

    await screen.findByText('女装标准码');
    expect(screen.queryByRole('button', { name: '新建尺码组' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 女装标准码' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <SizeGroupManagementPage />
    </AntdApp>,
  );
}

function openSelect(label: string) {
  const select = screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

function openDialogSelect(label: string) {
  const matches = within(screen.getByRole('dialog')).getAllByLabelText(label);
  const select = matches.find((element) => element.classList.contains('ant-select')) ?? matches.at(-1)!;
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
