import type { InventoryAgeRecord, InventoryLedgerRecord, TurnoverRecord } from '@/services/report/reportService';

export type ReportRowWithKey<T> = T & { clientRowKey: string };

export function attachReportRowKeys<T>(rows: T[], getBaseKey: (record: T) => string): ReportRowWithKey<T>[] {
  const keyCounts = new Map<string, number>();
  return rows.map((row) => {
    const baseKey = getBaseKey(row);
    const count = keyCounts.get(baseKey) ?? 0;
    keyCounts.set(baseKey, count + 1);
    return {
      ...row,
      clientRowKey: count === 0 ? baseKey : `${baseKey}-${count}`,
    };
  });
}

export function getLedgerRowKey(record: InventoryLedgerRecord): string {
  return String(
    record.inventoryId ??
      `${record.skuCode ?? record.materialCode ?? 'item'}-${record.warehouseName ?? 'warehouse'}-${record.batchNo ?? 'batch'}`,
  );
}

export function getAgeRowKey(record: InventoryAgeRecord): string {
  return String(
    record.inventoryId ??
      `${record.skuCode ?? record.materialCode ?? 'item'}-${record.warehouseName ?? 'warehouse'}-${record.ageRange ?? 'age'}`,
  );
}

export function getTurnoverRowKey(record: TurnoverRecord): string {
  return record.skuCode ?? record.materialCode ?? 'turnover';
}
