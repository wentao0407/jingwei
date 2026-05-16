import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuditLogPage } from './AuditLogPage';
import { listAuditLogs } from '@/services/system/systemExtService';

vi.mock('@/services/system/systemExtService', () => ({
  listAuditLogs: vi.fn(),
}));

const mockedListAuditLogs = vi.mocked(listAuditLogs);

describe('AuditLogPage', () => {
  beforeEach(() => {
    mockedListAuditLogs.mockReset();
    mockedListAuditLogs.mockResolvedValue({
      records: [{ id: '1', username: 'admin', module: 'SYSTEM', operationType: 'UPDATE', description: '更新配置' }],
      total: 1,
      current: 1,
      size: 20,
      pages: 1,
    });
  });

  it('loads audit logs', async () => {
    render(<AuditLogPage />);

    expect(await screen.findByText('更新配置')).toBeInTheDocument();
    expect(mockedListAuditLogs).toHaveBeenCalledWith({ current: 1, size: 20 });
  });
});
