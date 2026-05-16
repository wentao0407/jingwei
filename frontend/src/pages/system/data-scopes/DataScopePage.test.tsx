import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { DataScopePage } from './DataScopePage';
import { queryDataScope } from '@/services/system/systemExtService';

vi.mock('@/services/system/systemExtService', () => ({
  configureDataScope: vi.fn(),
  queryDataScope: vi.fn(),
}));

const mockedQueryDataScope = vi.mocked(queryDataScope);

describe('DataScopePage', () => {
  beforeEach(() => {
    mockedQueryDataScope.mockReset();
    mockedQueryDataScope.mockResolvedValue([{ id: '1', roleId: '2', scopeType: 'DEPARTMENT', scopeValue: '10001' }]);
  });

  it('queries role data scopes', async () => {
    render(
      <AntdApp>
        <DataScopePage />
      </AntdApp>,
    );

    fireEvent.change(screen.getByLabelText('角色 ID'), { target: { value: '2' } });
    fireEvent.click(screen.getByRole('button', { name: /查询/ }));

    await waitFor(() => expect(mockedQueryDataScope).toHaveBeenCalledWith('2'));
    expect(await screen.findByText('DEPARTMENT')).toBeInTheDocument();
    expect(screen.getByText('10001')).toBeInTheDocument();
  });
});
