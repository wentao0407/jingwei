import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { createSystemConfig, listSystemConfigs, updateSystemConfig } from './configService';

describe('configService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads all system configs', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: [
          {
            id: '160',
            configKey: 'password.expiry.days',
            configValue: '90',
            configGroup: 'PASSWORD',
          },
        ],
      },
    });

    const result = await listSystemConfigs();

    expect(postSpy).toHaveBeenCalledWith('/system/config/list');
    expect(result).toHaveLength(1);
  });

  it('updates system configs with configId query param and trims optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: {
          id: '160',
          configKey: 'password.expiry.days',
          configValue: '120',
          configGroup: 'PASSWORD',
        },
      },
    });

    await updateSystemConfig('160', {
      configValue: ' 120 ',
      description: '',
      needRestart: true,
      remark: ' 调整密码策略 ',
    });

    expect(postSpy).toHaveBeenCalledWith(
      '/system/config/update',
      {
        configValue: '120',
        needRestart: true,
        remark: '调整密码策略',
      },
      {
        params: { configId: '160' },
      },
    );
  });

  it('creates system configs and trims optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: {
          id: '162',
          configKey: 'order.lock.enabled',
          configValue: 'true',
          configGroup: 'OTHER',
        },
      },
    });

    await createSystemConfig({
      configKey: ' order.lock.enabled ',
      configValue: ' true ',
      configGroup: 'OTHER',
      description: '',
      needRestart: false,
    });

    expect(postSpy).toHaveBeenCalledWith('/system/config/create', {
      configKey: 'order.lock.enabled',
      configValue: 'true',
      configGroup: 'OTHER',
      needRestart: false,
    });
  });
});
