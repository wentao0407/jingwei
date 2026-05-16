import { Alert, Card, Table, type TablePaginationConfig } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  pageInventoryMaterials,
  pageInventorySkus,
  type InventoryMaterialRecord,
  type InventorySkuRecord,
} from '@/services/inventory/inventoryService';

interface InventoryStockPlaceholderPageProps {
  inventoryType: 'SKU' | 'MATERIAL';
}

type StockRecord = InventorySkuRecord | InventoryMaterialRecord;

const DEFAULT_PAGE_SIZE = 20;

export function InventoryStockPlaceholderPage({ inventoryType }: InventoryStockPlaceholderPageProps) {
  const isSku = inventoryType === 'SKU';
  const title = isSku ? '库存 SKU' : '库存物料';
  const [records, setRecords] = useState<StockRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadStock = useCallback(async (page: number) => {
    setLoading(true);
    setErrorMessage(null);
    try {
      const result = isSku
        ? await pageInventorySkus({ current: page, size: DEFAULT_PAGE_SIZE })
        : await pageInventoryMaterials({ current: page, size: DEFAULT_PAGE_SIZE });
      setRecords(result.records ?? []);
      setTotal(result.total ?? 0);
      setCurrent(result.current ?? page);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '库存查询失败');
    } finally {
      setLoading(false);
    }
  }, [isSku]);

  useEffect(() => {
    void loadStock(1);
  }, [loadStock]);

  const handleTableChange = (pagination: TablePaginationConfig) => {
    void loadStock(pagination.current ?? 1);
  };

  return (
    <Card title={title}>
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} style={{ marginBottom: 16 }} /> : null}
      <Table<StockRecord>
        rowKey="id"
        loading={loading}
        columns={buildColumns(isSku)}
        dataSource={records}
        pagination={{ current, pageSize: DEFAULT_PAGE_SIZE, total, showSizeChanger: false }}
        onChange={handleTableChange}
      />
    </Card>
  );
}

function buildColumns(isSku: boolean): ColumnsType<StockRecord> {
  return [
    {
      title: isSku ? 'SKU' : '物料',
      key: 'item',
      render: (_, record) => {
        if (isSku) {
          const skuRecord = record as InventorySkuRecord;
          return skuRecord.skuCode || skuRecord.skuId || '-';
        }

        const materialRecord = record as InventoryMaterialRecord;
        return materialRecord.materialName || materialRecord.materialCode || materialRecord.materialId || '-';
      },
    },
    { title: '仓库 ID', dataIndex: 'warehouseId', key: 'warehouseId', render: renderText },
    { title: '库位 ID', dataIndex: 'locationId', key: 'locationId', render: renderText },
    { title: '批次', dataIndex: 'batchNo', key: 'batchNo', render: renderText },
    { title: '可用', dataIndex: 'availableQty', key: 'availableQty', render: renderNumber },
    { title: '锁定', dataIndex: 'lockedQty', key: 'lockedQty', render: renderNumber },
    { title: '质检', dataIndex: 'qcQty', key: 'qcQty', render: renderNumber },
    { title: '在途', dataIndex: 'inTransitQty', key: 'inTransitQty', render: renderNumber },
    { title: '总量', dataIndex: 'totalQty', key: 'totalQty', render: renderNumber },
  ];
}

function renderText(value?: string | null) {
  return value || '-';
}

function renderNumber(value?: number | null) {
  return value ?? 0;
}
