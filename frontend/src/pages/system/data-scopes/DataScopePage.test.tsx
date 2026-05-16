import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { DataScopePage } from './DataScopePage';
import { listWarehouses } from '@/services/master/warehouseService';
import { listRoles } from '@/services/system/roleService';
import { configureDataScope, queryDataScope } from '@/services/system/systemExtService';

vi.mock('@/services/master/warehouseService', () => ({
  listWarehouses: vi.fn(),
}));

vi.mock('@/services/system/roleService', () => ({
  listRoles: vi.fn(),
}));

vi.mock('@/services/system/systemExtService', () => ({
  configureDataScope: vi.fn(),
  queryDataScope: vi.fn(),
}));

const mockedConfigureDataScope = vi.mocked(configureDataScope);
const mockedListRoles = vi.mocked(listRoles);
const mockedListWarehouses = vi.mocked(listWarehouses);
const mockedQueryDataScope = vi.mocked(queryDataScope);

describe('DataScopePage', () => {
  beforeEach(() => {
    mockedConfigureDataScope.mockReset();
    mockedConfigureDataScope.mockResolvedValue(undefined);
    mockedListRoles.mockReset();
    mockedListRoles.mockResolvedValue({
      records: [{ id: '2', roleCode: 'WAREHOUSE_MANAGER', roleName: '仓库主管', status: 'ACTIVE' }],
      total: 1,
      current: 1,
      size: 100,
      pages: 1,
    });
    mockedListWarehouses.mockReset();
    mockedListWarehouses.mockResolvedValue([
      { id: '10001', code: 'WH-SH', name: '上海仓', type: 'FINISHED', status: 'ACTIVE' },
      { id: '10002', code: 'WH-HZ', name: '杭州仓', type: 'FINISHED', status: 'ACTIVE' },
    ]);
    mockedQueryDataScope.mockReset();
    mockedQueryDataScope.mockResolvedValue([{ id: '1', roleId: '2', scopeType: 'WAREHOUSE', scopeValue: '10001' }]);
  });

  it('queries role data scopes with role selector', async () => {
    render(
      <AntdApp>
        <DataScopePage />
      </AntdApp>,
    );

    openSelect('角色');
    fireEvent.click(await screen.findByText('仓库主管'));
    fireEvent.click(screen.getByRole('button', { name: /查询/ }));

    await waitFor(() => expect(mockedQueryDataScope).toHaveBeenCalledWith('2'));
    await waitFor(() => expect(screen.getAllByText('按仓库').length).toBeGreaterThan(0));
    expect(screen.getAllByText('上海仓').length).toBeGreaterThan(0);
  });

  it('saves structured warehouse data scopes', async () => {
    render(
      <AntdApp>
        <DataScopePage />
      </AntdApp>,
    );

    openSelect('角色');
    fireEvent.click(await screen.findByText('仓库主管'));
    fireEvent.click(screen.getByRole('button', { name: /查询/ }));
    await waitFor(() => expect(mockedQueryDataScope).toHaveBeenCalledWith('2'));

    openSelect('范围类型');
    await chooseOption('按仓库');
    openSelect('仓库范围');
    await chooseOption('杭州仓');
    fireEvent.click(screen.getByRole('button', { name: /保存/ }));

    await waitFor(() =>
      expect(mockedConfigureDataScope).toHaveBeenCalledWith('2', {
        scopes: [{ scopeType: 'WAREHOUSE', scopeValue: '10001,10002' }],
      }),
    );
  });
});

function openSelect(label: string) {
  const select = screen.getAllByLabelText(label).find((element) => element.classList.contains('ant-select')) ?? screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
