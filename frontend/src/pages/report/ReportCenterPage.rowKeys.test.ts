import { describe, expect, it } from 'vitest';
import { attachReportRowKeys, getAgeRowKey, getLedgerRowKey, getTurnoverRowKey } from './reportRowKeys';
import type { InventoryAgeRecord, InventoryLedgerRecord, TurnoverRecord } from '@/services/report/reportService';

describe('ReportCenterPage row keys', () => {
  it('builds unique fallback keys when backend rows do not include inventoryId', () => {
    const ledgerRow = { skuCode: null, materialCode: null, warehouseName: null, batchNo: null } as InventoryLedgerRecord;
    const ageRow = { skuCode: null, materialCode: null, warehouseName: null, ageRange: null } as InventoryAgeRecord;
    const turnoverRow = { skuCode: null, materialCode: null } as TurnoverRecord;

    const ledgerRows = attachReportRowKeys([ledgerRow, ledgerRow], getLedgerRowKey);
    const ageRows = attachReportRowKeys([ageRow, ageRow], getAgeRowKey);
    const turnoverRows = attachReportRowKeys([turnoverRow, turnoverRow], getTurnoverRowKey);

    expect(ledgerRows[0].clientRowKey).not.toBe(ledgerRows[1].clientRowKey);
    expect(ageRows[0].clientRowKey).not.toBe(ageRows[1].clientRowKey);
    expect(turnoverRows[0].clientRowKey).not.toBe(turnoverRows[1].clientRowKey);
  });
});
