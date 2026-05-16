import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SpuManagementPage } from './SpuManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { listCategoryTree } from '@/services/master/categoryService';
import { listSizeGroups } from '@/services/master/sizeGroupService';
import {
  addSpuColors,
  batchUpdateSkuPrice,
  createSpu,
  deactivateSku,
  deleteSpu,
  getSpuDetail,
  listSpus,
  updateSkuPrice,
  updateSpu,
} from '@/services/master/spuService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/categoryService', () => ({
  listCategoryTree: vi.fn(),
}));

vi.mock('@/services/master/sizeGroupService', () => ({
  listSizeGroups: vi.fn(),
}));

vi.mock('@/services/master/spuService', () => ({
  addSpuColors: vi.fn(),
  batchUpdateSkuPrice: vi.fn(),
  createSpu: vi.fn(),
  deactivateSku: vi.fn(),
  deleteSpu: vi.fn(),
  getSpuDetail: vi.fn(),
  listSpus: vi.fn(),
  updateSkuPrice: vi.fn(),
  updateSpu: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedListCategoryTree = vi.mocked(listCategoryTree);
const mockedListSizeGroups = vi.mocked(listSizeGroups);
const mockedAddSpuColors = vi.mocked(addSpuColors);
const mockedBatchUpdateSkuPrice = vi.mocked(batchUpdateSkuPrice);
const mockedCreateSpu = vi.mocked(createSpu);
const mockedDeactivateSku = vi.mocked(deactivateSku);
const mockedDeleteSpu = vi.mocked(deleteSpu);
const mockedGetSpuDetail = vi.mocked(getSpuDetail);
const mockedListSpus = vi.mocked(listSpus);
const mockedUpdateSkuPrice = vi.mocked(updateSkuPrice);
const mockedUpdateSpu = vi.mocked(updateSpu);

const permissions = [
  'master:spu:create',
  'master:spu:update',
  'master:spu:deactivate',
  'master:spu:addColor',
  'master:sku:updatePrice',
  'master:sku:deactivate',
];

const sizeGroups = [{ id: '10001', code: 'WOMEN_STD', name: '女装标准码', category: 'WOMEN', status: 'ACTIVE', sizes: [] }];
const categories = [{ id: '270', parentId: null, code: 'FABRIC', name: '面料', level: 1, sortOrder: 1, status: 'ACTIVE', children: [] }];
const spus = [
  { id: '1', code: 'SP-000001', name: '春款衬衫', categoryId: '270', sizeGroupId: '10001', status: 'DRAFT', colorWays: [], skus: [] },
  { id: '2', code: 'SP-000002', name: '夏款T恤', categoryId: '270', sizeGroupId: '10001', status: 'ACTIVE', colorWays: [], skus: [] },
];
const detail = {
  ...spus[0],
  colorWays: [{ id: '11', spuId: '1', colorName: '黑色', colorCode: 'BK', sortOrder: 1 }],
  skus: [{ id: '21', code: 'SP-000001-BK-S', spuId: '1', colorWayId: '11', sizeId: '10101', salePrice: 199, status: 'ACTIVE' }],
};

describe('SpuManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedListCategoryTree.mockReset();
    mockedListCategoryTree.mockResolvedValue(categories);
    mockedListSizeGroups.mockReset();
    mockedListSizeGroups.mockResolvedValue(sizeGroups);
    mockedAddSpuColors.mockReset();
    mockedBatchUpdateSkuPrice.mockReset();
    mockedBatchUpdateSkuPrice.mockResolvedValue(3);
    mockedCreateSpu.mockReset();
    mockedDeactivateSku.mockReset();
    mockedDeleteSpu.mockReset();
    mockedGetSpuDetail.mockReset();
    mockedGetSpuDetail.mockResolvedValue(detail);
    mockedListSpus.mockReset();
    mockedUpdateSkuPrice.mockReset();
    mockedUpdateSpu.mockReset();
  });

  it('loads and filters SPUs', async () => {
    mockedListSpus.mockResolvedValue(spus);

    renderPage();

    expect(screen.getByText('正在加载款式数据')).toBeInTheDocument();
    expect(await screen.findByText('春款衬衫')).toBeInTheDocument();
    openSelect('款式状态筛选');
    await chooseOption('启用');

    await waitFor(() => expect(mockedListSpus).toHaveBeenLastCalledWith({ status: 'ACTIVE' }));
  });

  it('creates, updates and deletes SPUs', async () => {
    mockedListSpus.mockResolvedValue(spus);
    mockedCreateSpu.mockResolvedValue(detail);
    mockedUpdateSpu.mockResolvedValue(detail);
    mockedDeleteSpu.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('春款衬衫');
    fireEvent.click(screen.getByRole('button', { name: '新建款式' }));
    fireEvent.change(screen.getByLabelText('款式名称'), { target: { value: ' 春款外套 ' } });
    openDialogSelect('尺码组');
    await chooseOption('女装标准码');
    fireEvent.change(screen.getByLabelText('颜色名称'), { target: { value: ' 黑色 ' } });
    fireEvent.change(screen.getByLabelText('颜色编码'), { target: { value: ' BK ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateSpu).toHaveBeenCalledWith({
        name: '春款外套',
        sizeGroupId: '10001',
        colors: [{ colorName: '黑色', colorCode: 'BK' }],
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 春款衬衫' }));
    fireEvent.change(screen.getByLabelText('款式名称'), { target: { value: ' 春款衬衫更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateSpu).toHaveBeenCalledWith('1', expect.objectContaining({ name: '春款衬衫更新' })));

    fireEvent.click(screen.getByRole('button', { name: '删除 夏款T恤' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeleteSpu).toHaveBeenCalledWith('2'));
  });

  it('shows detail and manages colors and SKU price/status', async () => {
    mockedListSpus.mockResolvedValue(spus);
    mockedAddSpuColors.mockResolvedValue(detail);
    mockedUpdateSkuPrice.mockResolvedValue(detail.skus[0]);
    mockedDeactivateSku.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('春款衬衫');
    fireEvent.click(screen.getByRole('button', { name: '详情 春款衬衫' }));
    expect(await screen.findByText('SP-000001-BK-S')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '追加颜色' }));
    fireEvent.change(screen.getByLabelText('颜色名称'), { target: { value: ' 白色 ' } });
    fireEvent.change(screen.getByLabelText('颜色编码'), { target: { value: ' WT ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedAddSpuColors).toHaveBeenCalledWith('1', [{ colorName: '白色', colorCode: 'WT' }]));

    fireEvent.click(screen.getByRole('button', { name: '改价 SP-000001-BK-S' }));
    fireEvent.change(screen.getByLabelText('销售价'), { target: { value: '219' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateSkuPrice).toHaveBeenCalledWith({ skuId: '21', salePrice: 219 }));

    fireEvent.click(screen.getByRole('button', { name: '停用SKU SP-000001-BK-S' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));

    await waitFor(() => expect(mockedDeactivateSku).toHaveBeenCalledWith('21'));
  });

  it('batch updates SKU prices by color way', async () => {
    mockedListSpus.mockResolvedValue(spus);

    renderPage();

    await screen.findByText('春款衬衫');
    fireEvent.click(screen.getByRole('button', { name: '详情 春款衬衫' }));
    expect(await screen.findByText('SP-000001-BK-S')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '批量改价' }));
    openDialogSelect('颜色范围');
    await chooseOption('黑色');
    fireEvent.change(screen.getByLabelText('批量销售价'), { target: { value: '239' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedBatchUpdateSkuPrice).toHaveBeenCalledWith({
        spuId: '1',
        colorWayId: '11',
        salePrice: 239,
      }),
    );
  });

  it('hides SPU actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });
    mockedListSpus.mockResolvedValue(spus);

    renderPage();

    await screen.findByText('春款衬衫');
    expect(screen.queryByRole('button', { name: '新建款式' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 春款衬衫' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <SpuManagementPage />
    </AntdApp>,
  );
}

function openSelect(label: string) {
  const select = screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

function openDialogSelect(label: string) {
  const matches = within(screen.getAllByRole('dialog').at(-1)!).getAllByLabelText(label);
  const select = matches.find((element) => element.classList.contains('ant-select')) ?? matches.at(-1)!;
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
