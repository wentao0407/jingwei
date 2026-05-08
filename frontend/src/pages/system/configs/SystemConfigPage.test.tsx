import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SystemConfigPage } from './SystemConfigPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { createSystemConfig, listSystemConfigs, updateSystemConfig } from '@/services/system/configService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/system/configService', () => ({
  createSystemConfig: vi.fn(),
  listSystemConfigs: vi.fn(),
  updateSystemConfig: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCreateSystemConfig = vi.mocked(createSystemConfig);
const mockedListSystemConfigs = vi.mocked(listSystemConfigs);
const mockedUpdateSystemConfig = vi.mocked(updateSystemConfig);
const configPermissionCodes = ['system:config:create', 'system:config:update'];

const systemConfigs = [
  {
    id: '160',
    configKey: 'password.expiry.days',
    configValue: '90',
    configGroup: 'PASSWORD',
    description: '密码过期天数',
    needRestart: false,
    remark: '默认策略',
    updatedAt: '2026-05-06T11:00:00',
  },
  {
    id: '161',
    configKey: 'inventory.lock.enabled',
    configValue: 'true',
    configGroup: 'INVENTORY',
    description: '库存锁定开关',
    needRestart: true,
    remark: '库存初始化',
    updatedAt: '2026-05-06T12:00:00',
  },
];

describe('SystemConfigPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: configPermissionCodes,
    });
    mockedCreateSystemConfig.mockReset();
    mockedListSystemConfigs.mockReset();
    mockedUpdateSystemConfig.mockReset();
  });

  it('loads and renders system configs', async () => {
    mockedListSystemConfigs.mockResolvedValue(systemConfigs);

    renderPage();

    expect(screen.getByText('正在加载系统配置')).toBeInTheDocument();
    expect(await screen.findByText('password.expiry.days')).toBeInTheDocument();
    expect(screen.getByText('90')).toBeInTheDocument();
    expect(screen.getByText('密码配置')).toBeInTheDocument();
    expect(screen.queryByText('PASSWORD')).not.toBeInTheDocument();
    expect(screen.getByText('无需重启')).toBeInTheDocument();
    expect(mockedListSystemConfigs).toHaveBeenCalledTimes(1);
  });

  it('shows empty state when there are no configs', async () => {
    mockedListSystemConfigs.mockResolvedValue([]);

    renderPage();

    expect(await screen.findByText('暂无系统配置')).toBeInTheDocument();
  });

  it('shows backend error message and retries loading', async () => {
    mockedListSystemConfigs.mockRejectedValueOnce(new Error('配置加载失败'));
    mockedListSystemConfigs.mockResolvedValueOnce([]);

    renderPage();

    expect(await screen.findByText('配置加载失败')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '重新加载' }));

    await waitFor(() => expect(mockedListSystemConfigs).toHaveBeenCalledTimes(2));
  });

  it('filters configs by group', async () => {
    mockedListSystemConfigs.mockResolvedValue(systemConfigs);

    renderPage();

    expect(await screen.findByText('password.expiry.days')).toBeInTheDocument();
    fireEvent.mouseDown(screen.getAllByRole('combobox', { name: '配置分组' })[0]);
    fireEvent.click((await screen.findAllByText('库存配置')).at(-1)!);

    expect(screen.queryByText('password.expiry.days')).not.toBeInTheDocument();
    expect(screen.getByText('inventory.lock.enabled')).toBeInTheDocument();
  });

  it('updates a config and reloads the list', async () => {
    mockedListSystemConfigs.mockResolvedValue(systemConfigs);
    mockedUpdateSystemConfig.mockResolvedValue({
      ...systemConfigs[0],
      configValue: '120',
      remark: '调整密码策略',
    });

    renderPage();

    expect(await screen.findByText('password.expiry.days')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '编辑 password.expiry.days' }));
    fireEvent.change(screen.getByLabelText('配置值'), { target: { value: ' 120 ' } });
    fireEvent.change(screen.getByLabelText('配置说明'), { target: { value: ' 密码过期天数 ' } });
    fireEvent.mouseDown(screen.getByLabelText('修改后重启'));
    fireEvent.click((await screen.findAllByText('需要重启')).at(-1)!);
    fireEvent.change(screen.getByLabelText('修改原因'), { target: { value: ' 调整密码策略 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateSystemConfig).toHaveBeenCalledWith('160', {
        configValue: '120',
        description: '密码过期天数',
        needRestart: true,
        remark: '调整密码策略',
      }),
    );
    expect(mockedListSystemConfigs).toHaveBeenCalledTimes(2);
  });

  it('creates a config and reloads the list', async () => {
    mockedListSystemConfigs.mockResolvedValue(systemConfigs);
    mockedCreateSystemConfig.mockResolvedValue({
      id: '162',
      configKey: 'order.lock.enabled',
      configValue: 'true',
      configGroup: 'OTHER',
      description: '订单锁定开关',
      needRestart: false,
      remark: null,
      updatedAt: '2026-05-08T10:00:00',
    });

    renderPage();

    expect(await screen.findByText('password.expiry.days')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '新增配置' }));
    fireEvent.change(screen.getByLabelText('配置键'), { target: { value: ' order.lock.enabled ' } });
    fireEvent.change(screen.getByLabelText('配置值'), { target: { value: ' true ' } });
    const createGroupSelect = screen.getAllByLabelText('新增配置分组')[0];
    fireEvent.mouseDown(createGroupSelect.querySelector('.ant-select-selector') ?? createGroupSelect);
    fireEvent.click((await screen.findAllByText('其他配置')).at(-1)!);
    fireEvent.change(screen.getByLabelText('配置说明'), { target: { value: ' 订单锁定开关 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateSystemConfig).toHaveBeenCalledWith({
        configKey: 'order.lock.enabled',
        configValue: 'true',
        configGroup: 'OTHER',
        description: '订单锁定开关',
        needRestart: false,
      }),
    );
    expect(mockedListSystemConfigs).toHaveBeenCalledTimes(2);
  });

  it('requires remark when updating configs', async () => {
    mockedListSystemConfigs.mockResolvedValue(systemConfigs);

    renderPage();

    expect(await screen.findByText('password.expiry.days')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '编辑 password.expiry.days' }));
    fireEvent.change(screen.getByLabelText('修改原因'), { target: { value: ' ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('请输入修改原因')).toBeInTheDocument();
    expect(mockedUpdateSystemConfig).not.toHaveBeenCalled();
  });

  it('hides config actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: [],
    });
    setAuthSession({
      userId: '1',
      username: 'viewer',
      realName: '只读用户',
      roleIds: [],
      permissions: [],
      menuTree: [],
    });
    mockedListSystemConfigs.mockResolvedValue(systemConfigs);

    renderPage();

    expect(await screen.findByText('password.expiry.days')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '新增配置' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 password.expiry.days' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <SystemConfigPage />
    </AntdApp>,
  );
}
