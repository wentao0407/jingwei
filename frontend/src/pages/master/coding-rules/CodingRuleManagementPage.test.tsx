import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CodingRuleManagementPage } from './CodingRuleManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  createCodingRule,
  deleteCodingRule,
  generateCode,
  listCodingRules,
  previewCode,
  updateCodingRule,
} from '@/services/master/codingRuleService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/codingRuleService', () => ({
  createCodingRule: vi.fn(),
  deleteCodingRule: vi.fn(),
  generateCode: vi.fn(),
  listCodingRules: vi.fn(),
  previewCode: vi.fn(),
  updateCodingRule: vi.fn(),
}));

const mockedCreateCodingRule = vi.mocked(createCodingRule);
const mockedDeleteCodingRule = vi.mocked(deleteCodingRule);
const mockedGenerateCode = vi.mocked(generateCode);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedListCodingRules = vi.mocked(listCodingRules);
const mockedPreviewCode = vi.mocked(previewCode);
const mockedUpdateCodingRule = vi.mocked(updateCodingRule);

const permissions = [
  'master:codingRule:create',
  'master:codingRule:update',
  'master:codingRule:delete',
  'master:codingRule:generate',
];

const codingRules = [
  {
    id: '1',
    code: 'SALES_ORDER',
    name: '销售订单编号',
    businessType: 'ORDER',
    description: '销售订单自动编号',
    status: 'ACTIVE',
    used: true,
    segments: [
      { id: '11', segmentType: 'FIXED', segmentValue: 'SO-', sortOrder: 1 },
      { id: '12', segmentType: 'DATE', segmentValue: 'yyyyMM', sortOrder: 2, connector: '-' },
      { id: '13', segmentType: 'SEQUENCE', seqLength: 5, seqResetType: 'MONTHLY', sortOrder: 3 },
    ],
    createdAt: '2026-05-01T10:00:00',
    updatedAt: '2026-05-01T10:00:00',
  },
  {
    id: '2',
    code: 'QA_RULE',
    name: 'QA编号',
    businessType: 'MASTER',
    description: '',
    status: 'INACTIVE',
    used: false,
    segments: [{ id: '21', segmentType: 'SEQUENCE', seqLength: 4, seqResetType: 'NEVER', sortOrder: 1 }],
    createdAt: '2026-05-02T10:00:00',
    updatedAt: '2026-05-02T10:00:00',
  },
];

describe('CodingRuleManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedCreateCodingRule.mockReset();
    mockedCreateCodingRule.mockResolvedValue(codingRules[1]);
    mockedDeleteCodingRule.mockReset();
    mockedDeleteCodingRule.mockResolvedValue(undefined);
    mockedGenerateCode.mockReset();
    mockedGenerateCode.mockResolvedValue('SO-202605-00002');
    mockedListCodingRules.mockReset();
    mockedListCodingRules.mockResolvedValue(codingRules);
    mockedPreviewCode.mockReset();
    mockedPreviewCode.mockResolvedValue({ previewCode: 'SO-202605-00001' });
    mockedUpdateCodingRule.mockReset();
    mockedUpdateCodingRule.mockResolvedValue(codingRules[0]);
  });

  it('loads, filters and previews coding rules', async () => {
    renderPage();

    expect(screen.getByText('正在加载编码规则')).toBeInTheDocument();
    expect(await screen.findByText('销售订单编号')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('搜索规则编码或名称'), { target: { value: ' qa ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    expect(screen.queryByText('销售订单编号')).not.toBeInTheDocument();
    expect(screen.getByText('QA编号')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '预览 QA_RULE' }));
    expect(await screen.findByText('SO-202605-00001')).toBeInTheDocument();
    expect(mockedPreviewCode).toHaveBeenCalledWith({ ruleCode: 'QA_RULE' });
  });

  it('generates official code for a coding rule', async () => {
    renderPage();

    await screen.findByText('销售订单编号');
    fireEvent.click(screen.getByRole('button', { name: '正式生成 SALES_ORDER' }));

    await waitFor(() => expect(mockedGenerateCode).toHaveBeenCalledWith({ ruleCode: 'SALES_ORDER' }));
    expect(await screen.findByText('SO-202605-00002')).toBeInTheDocument();
  });

  it('creates, updates and deletes coding rules', async () => {
    renderPage();

    await screen.findByText('销售订单编号');
    fireEvent.click(screen.getByRole('button', { name: '新建规则' }));
    fireEvent.change(screen.getByLabelText('规则编码'), { target: { value: ' QA_RULE ' } });
    fireEvent.change(screen.getByLabelText('规则名称'), { target: { value: ' QA编号 ' } });
    fireEvent.change(screen.getByLabelText('业务类型'), { target: { value: ' MASTER ' } });
    selectLastDialogOption('段类型', '流水号');
    fireEvent.change(screen.getByLabelText('流水号长度'), { target: { value: '4' } });
    selectLastDialogOption('重置方式', '不重置');
    fireEvent.click(screen.getByRole('button', { name: /保\s*存/ }));

    await waitFor(() =>
      expect(mockedCreateCodingRule).toHaveBeenCalledWith(
        expect.objectContaining({
          code: 'QA_RULE',
          name: 'QA编号',
          businessType: 'MASTER',
          segments: [expect.objectContaining({ segmentType: 'SEQUENCE', seqLength: 4, sortOrder: 1 })],
        }),
      ),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 SALES_ORDER' }));
    fireEvent.change(screen.getByLabelText('规则名称'), { target: { value: ' 销售订单编号更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: /保\s*存/ }));
    await waitFor(() => expect(mockedUpdateCodingRule).toHaveBeenCalledWith('1', expect.objectContaining({ name: '销售订单编号更新' })));

    fireEvent.click(screen.getByRole('button', { name: '删除 QA_RULE' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(mockedDeleteCodingRule).toHaveBeenCalledWith('2'));
  });

  it('validates required fields and sequence segment before submit', async () => {
    renderPage();

    await screen.findByText('销售订单编号');
    fireEvent.click(screen.getByRole('button', { name: '新建规则' }));
    fireEvent.click(screen.getByRole('button', { name: /保\s*存/ }));

    expect(await screen.findByText('请输入规则编码')).toBeInTheDocument();
    expect(screen.getByText('请输入规则名称')).toBeInTheDocument();
    expect(screen.getByText('至少需要一个流水号段')).toBeInTheDocument();
    expect(mockedCreateCodingRule).not.toHaveBeenCalled();
  });

  it('hides actions without coding rule permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });

    renderPage();

    await screen.findByText('销售订单编号');
    expect(screen.queryByRole('button', { name: '新建规则' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 SALES_ORDER' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '删除 QA_RULE' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <CodingRuleManagementPage />
    </AntdApp>,
  );
}

function selectLastDialogOption(label: string, option: string) {
  const dialog = screen.getAllByRole('dialog').at(-1)!;
  const select = within(dialog).getAllByLabelText(label).at(-1)!;
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
  fireEvent.click(screen.getAllByText(option).at(-1)!);
}
