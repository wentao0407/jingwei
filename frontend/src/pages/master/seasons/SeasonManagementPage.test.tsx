import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SeasonManagementPage } from './SeasonManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
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
} from '@/services/master/seasonService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/seasonService', () => ({
  closeSeason: vi.fn(),
  createSeason: vi.fn(),
  createWave: vi.fn(),
  deleteSeason: vi.fn(),
  deleteWave: vi.fn(),
  getSeasonDetail: vi.fn(),
  listSeasons: vi.fn(),
  updateSeason: vi.fn(),
  updateWave: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCloseSeason = vi.mocked(closeSeason);
const mockedCreateSeason = vi.mocked(createSeason);
const mockedCreateWave = vi.mocked(createWave);
const mockedDeleteSeason = vi.mocked(deleteSeason);
const mockedDeleteWave = vi.mocked(deleteWave);
const mockedGetSeasonDetail = vi.mocked(getSeasonDetail);
const mockedListSeasons = vi.mocked(listSeasons);
const mockedUpdateSeason = vi.mocked(updateSeason);
const mockedUpdateWave = vi.mocked(updateWave);

const permissions = [
  'master:season:create',
  'master:season:update',
  'master:season:close',
  'master:season:delete',
  'master:wave:create',
  'master:wave:update',
  'master:wave:delete',
];

const seasons = [
  {
    id: '10001',
    code: '2026SS',
    name: '2026春夏',
    year: 2026,
    seasonType: 'SPRING_SUMMER',
    startDate: '2026-03-01',
    endDate: '2026-08-31',
    status: 'ACTIVE',
  },
  {
    id: '10002',
    code: '2026AW',
    name: '2026秋冬',
    year: 2026,
    seasonType: 'AUTUMN_WINTER',
    startDate: '2026-09-01',
    endDate: '2027-02-28',
    status: 'CLOSED',
  },
];

const seasonDetail = {
  ...seasons[0],
  waves: [
    { id: '20001', seasonId: '10001', code: 'W1', name: '第一波', deliveryDate: '2026-04-01', sortOrder: 1 },
    { id: '20002', seasonId: '10001', code: 'W2', name: '第二波', deliveryDate: '2026-05-01', sortOrder: 2 },
  ],
};

describe('SeasonManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedCloseSeason.mockReset();
    mockedCloseSeason.mockResolvedValue(undefined);
    mockedCreateSeason.mockReset();
    mockedCreateSeason.mockResolvedValue(seasons[0]);
    mockedCreateWave.mockReset();
    mockedCreateWave.mockResolvedValue(seasonDetail.waves[0]);
    mockedDeleteSeason.mockReset();
    mockedDeleteSeason.mockResolvedValue(undefined);
    mockedDeleteWave.mockReset();
    mockedDeleteWave.mockResolvedValue(undefined);
    mockedGetSeasonDetail.mockReset();
    mockedGetSeasonDetail.mockResolvedValue(seasonDetail);
    mockedListSeasons.mockReset();
    mockedListSeasons.mockResolvedValue(seasons);
    mockedUpdateSeason.mockReset();
    mockedUpdateSeason.mockResolvedValue(seasons[0]);
    mockedUpdateWave.mockReset();
    mockedUpdateWave.mockResolvedValue(seasonDetail.waves[1]);
  });

  it('loads and filters seasons', async () => {
    renderPage();

    expect(screen.getByText('正在加载季节数据')).toBeInTheDocument();
    expect(await screen.findByText('2026春夏')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('年份筛选'), { target: { value: '2026' } });
    openSelect('季节类型筛选');
    await chooseOption('春夏');

    await waitFor(() => expect(mockedListSeasons).toHaveBeenLastCalledWith({ year: 2026, seasonType: 'SPRING_SUMMER' }));
  });

  it('creates, updates, closes and deletes seasons', async () => {
    renderPage();

    await screen.findByText('2026春夏');
    fireEvent.click(screen.getByRole('button', { name: '新建季节' }));
    fireEvent.change(screen.getByLabelText('季节编码'), { target: { value: ' 2027SS ' } });
    fireEvent.change(screen.getByLabelText('季节名称'), { target: { value: ' 2027春夏 ' } });
    fireEvent.change(screen.getByLabelText('年份'), { target: { value: '2027' } });
    openDialogSelect('季节类型');
    await chooseOption('春夏');
    fireEvent.change(screen.getByLabelText('开始日期'), { target: { value: '2027-03-01' } });
    fireEvent.change(screen.getByLabelText('结束日期'), { target: { value: '2027-08-31' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateSeason).toHaveBeenCalledWith(
        expect.objectContaining({ code: '2027SS', name: '2027春夏', seasonType: 'SPRING_SUMMER' }),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 2026春夏' }));
    fireEvent.change(screen.getByLabelText('季节名称'), { target: { value: ' 春夏更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateSeason).toHaveBeenCalledWith('10001', expect.objectContaining({ name: '春夏更新' })));

    fireEvent.click(screen.getByRole('button', { name: '关闭 2026春夏' }));
    fireEvent.click(screen.getByRole('button', { name: '确认关闭' }));
    await waitFor(() => expect(mockedCloseSeason).toHaveBeenCalledWith('10001'));

    fireEvent.click(screen.getByRole('button', { name: '删除 2026秋冬' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(mockedDeleteSeason).toHaveBeenCalledWith('10002'));
  });

  it('manages waves inside selected season', async () => {
    renderPage();

    await screen.findByText('2026春夏');
    fireEvent.click(screen.getByRole('button', { name: '波段 2026春夏' }));
    expect(await screen.findByText('第一波')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '新增波段' }));
    fireEvent.change(screen.getByLabelText('波段编码'), { target: { value: ' W3 ' } });
    fireEvent.change(screen.getByLabelText('波段名称'), { target: { value: ' 第三波 ' } });
    fireEvent.change(screen.getByLabelText('交货日期'), { target: { value: '2026-06-01' } });
    fireEvent.change(screen.getByLabelText('排序号'), { target: { value: '3' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedCreateWave).toHaveBeenCalledWith('10001', expect.objectContaining({ code: 'W3', name: '第三波' })));

    fireEvent.click(screen.getByRole('button', { name: '编辑波段 W2' }));
    fireEvent.change(screen.getByLabelText('波段名称'), { target: { value: ' 第二波更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateWave).toHaveBeenCalledWith('20002', expect.objectContaining({ name: '第二波更新' })));

    fireEvent.click(screen.getByRole('button', { name: '删除波段 W1' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeleteWave).toHaveBeenCalledWith('20001'));
  });

  it('hides season actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });

    renderPage();

    await screen.findByText('2026春夏');
    expect(screen.queryByRole('button', { name: '新建季节' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 2026春夏' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <SeasonManagementPage />
    </AntdApp>,
  );
}

function openSelect(label: string) {
  const select = screen.getAllByLabelText(label).find((element) => element.classList.contains('ant-select')) ?? screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

function openDialogSelect(label: string) {
  const dialog = screen.getAllByRole('dialog').at(-1)!;
  const matches = within(dialog).getAllByLabelText(label);
  const select = matches.find((element) => element.classList.contains('ant-select')) ?? matches.at(-1)!;
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
