import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  createCodingRule,
  deleteCodingRule,
  generateCode,
  getCodingRuleDetail,
  listCodingRules,
  previewCode,
  updateCodingRule,
} from './codingRuleService';
import { apiClient } from '@/services/http/apiClient';

vi.mock('@/services/http/apiClient', async () => {
  const actual = await vi.importActual<typeof import('@/services/http/apiClient')>('@/services/http/apiClient');
  return {
    ...actual,
    apiClient: {
      post: vi.fn(),
    },
  };
});

const mockedPost = vi.mocked(apiClient.post);

describe('codingRuleService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries rules and details through coding rule endpoints', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [] } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } });

    await listCodingRules();
    await getCodingRuleDetail(' SALES_ORDER ');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/codingRule/list');
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/master/codingRule/detail', null, {
      params: { code: 'SALES_ORDER' },
    });
  });

  it('creates, updates and deletes coding rules with normalized payloads', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await createCodingRule({
      code: ' QA_RULE ',
      name: ' QA规则 ',
      businessType: '',
      description: ' 描述 ',
      segments: [
        { segmentType: ' FIXED ', segmentValue: ' QA-', sortOrder: 1, connector: '' },
        { segmentType: 'SEQUENCE', seqLength: 5, seqResetType: 'MONTHLY', sortOrder: 2 },
      ],
    });
    await updateCodingRule('1', { name: ' 更新 ', businessType: '', description: ' 新描述 ', status: 'ACTIVE' });
    await deleteCodingRule('1');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/codingRule/create', {
      code: 'QA_RULE',
      name: 'QA规则',
      description: '描述',
      segments: [
        { segmentType: 'FIXED', segmentValue: 'QA-', sortOrder: 1 },
        { segmentType: 'SEQUENCE', seqLength: 5, seqResetType: 'MONTHLY', sortOrder: 2 },
      ],
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/codingRule/update',
      { name: '更新', description: '新描述', status: 'ACTIVE' },
      { params: { ruleId: '1' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/codingRule/delete', null, {
      params: { ruleId: '1' },
    });
  });

  it('previews and generates codes with trimmed context values', async () => {
    mockedPost
      .mockResolvedValueOnce({
        data: { code: 0, message: 'success', success: true, data: { previewCode: 'SO-202605-00001' } },
      })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: 'SO-202605-00002' } });

    await previewCode({ ruleCode: ' SALES_ORDER ', context: { season: ' 2026SS ', warehouse: '' } });
    await generateCode({ ruleCode: 'SALES_ORDER', context: {} });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/codingRule/preview', {
      ruleCode: 'SALES_ORDER',
      context: { season: '2026SS' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/master/codingRule/generate', {
      ruleCode: 'SALES_ORDER',
    });
  });
});
