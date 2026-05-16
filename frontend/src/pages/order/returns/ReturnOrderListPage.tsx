import { ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Table, Tag, type TablePaginationConfig } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { pageReturnOrders, type ReturnOrderRecord } from '@/services/order/returnOrderService';

const DEFAULT_PAGE_SIZE = 20;

export function ReturnOrderListPage() {
  const [records, setRecords] = useState<ReturnOrderRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void loadReturns(1);
  }, []);

  const loadReturns = async (page: number) => {
    setLoading(true);
    try {
      const result = await pageReturnOrders({ current: page, size: DEFAULT_PAGE_SIZE });
      setRecords(result.records ?? []);
      setTotal(result.total ?? 0);
      setCurrent(result.current ?? page);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    void loadReturns(pagination.current ?? 1);
  };

  return (
    <Card
      title="退货单"
      extra={
        <Button icon={<ReloadOutlined />} loading={loading} onClick={() => void loadReturns(current)}>
          刷新
        </Button>
      }
    >
      <Table<ReturnOrderRecord>
        rowKey="id"
        loading={loading}
        columns={returnOrderColumns}
        dataSource={records}
        pagination={{ current, pageSize: DEFAULT_PAGE_SIZE, total, showSizeChanger: false }}
        onChange={handleTableChange}
      />
    </Card>
  );
}

const returnOrderColumns: ColumnsType<ReturnOrderRecord> = [
  { title: '退货单号', dataIndex: 'returnNo', key: 'returnNo', render: renderText },
  { title: '销售订单', dataIndex: 'salesOrderNo', key: 'salesOrderNo', render: renderText },
  { title: '客户 ID', dataIndex: 'customerId', key: 'customerId', render: renderText },
  { title: '类型', dataIndex: 'returnTypeLabel', key: 'returnTypeLabel', render: renderText },
  { title: '数量', dataIndex: 'totalQuantity', key: 'totalQuantity', render: (value?: number | null) => value ?? 0 },
  {
    title: '状态',
    dataIndex: 'statusLabel',
    key: 'statusLabel',
    render: (value?: string | null, record?: ReturnOrderRecord) => <Tag>{value || record?.status || '-'}</Tag>,
  },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: renderText },
];

function renderText(value?: string | null) {
  return value || '-';
}
