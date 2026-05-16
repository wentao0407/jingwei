import { ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Table, type TablePaginationConfig } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { listAuditLogs, type AuditLogRecord } from '@/services/system/systemExtService';

const DEFAULT_PAGE_SIZE = 20;

export function AuditLogPage() {
  const [records, setRecords] = useState<AuditLogRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void loadLogs(1);
  }, []);

  const loadLogs = async (page: number) => {
    setLoading(true);
    try {
      const result = await listAuditLogs({ current: page, size: DEFAULT_PAGE_SIZE });
      setRecords(result.records ?? []);
      setTotal(result.total ?? 0);
      setCurrent(result.current ?? page);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    void loadLogs(pagination.current ?? 1);
  };

  return (
    <Card title="操作日志" extra={<Button icon={<ReloadOutlined />} loading={loading} onClick={() => void loadLogs(current)}>刷新</Button>}>
      <Table<AuditLogRecord>
        rowKey="id"
        loading={loading}
        columns={columns}
        dataSource={records}
        pagination={{ current, pageSize: DEFAULT_PAGE_SIZE, total, showSizeChanger: false }}
        onChange={handleTableChange}
      />
    </Card>
  );
}

const columns: ColumnsType<AuditLogRecord> = [
  { title: '用户', dataIndex: 'username', key: 'username', render: renderText },
  { title: '模块', dataIndex: 'module', key: 'module', render: renderText },
  { title: '操作', dataIndex: 'operationType', key: 'operationType', render: renderText },
  { title: '描述', dataIndex: 'description', key: 'description', render: renderText },
  { title: 'IP', dataIndex: 'ipAddress', key: 'ipAddress', render: renderText },
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt', render: renderText },
];

function renderText(value?: string | null) {
  return value || '-';
}
