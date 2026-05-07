import { ReloadOutlined, SearchOutlined, TeamOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { Button, Input, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { listRoles, type RoleRecord } from '@/services/system/roleService';
import type { PageResult } from '@/services/system/userService';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const roleColumns: ColumnsType<RoleRecord> = [
  {
    title: '角色编码',
    dataIndex: 'roleCode',
  },
  {
    title: '角色名称',
    dataIndex: 'roleName',
  },
  {
    title: '描述',
    dataIndex: 'description',
    render: (value) => value || '-',
  },
  {
    title: '状态',
    dataIndex: 'status',
    render: (status) => <RoleStatusTag status={String(status)} />,
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    render: (value) => formatDateTime(String(value || '')),
  },
  {
    title: '更新时间',
    dataIndex: 'updatedAt',
    render: (value) => formatDateTime(String(value || '')),
  },
];

export function RoleManagementPage() {
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<RoleRecord> | null>(null);

  const loadRoles = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      const result = await listRoles({
        current: currentPage,
        size: pageSize,
        ...(keyword ? { keyword } : {}),
        ...(status ? { status } : {}),
      });
      setPageResult(result);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, keyword, status]);

  useEffect(() => {
    void loadRoles();
  }, [loadRoles]);

  const handleSearch = () => {
    setKeyword(keywordInput.trim());
    setCurrentPage(INITIAL_PAGE);
  };

  const handleStatusChange = (nextStatus: string) => {
    setStatus(nextStatus);
    setCurrentPage(INITIAL_PAGE);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  };

  if (loading && !pageResult) {
    return <LoadingState message="正在加载角色数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadRoles} />;
  }

  const roles = pageResult?.records ?? [];

  return (
    <div className="system-page role-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>角色管理</h1>
          <p>查看角色编码、名称、状态和维护时间。</p>
        </div>
        <TeamOutlined className="system-page-topbar-icon" />
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input
            allowClear
            className="system-search-input"
            prefix={<SearchOutlined />}
            placeholder="搜索角色编码/角色名称"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select
            className="system-status-select"
            options={statusOptions}
            value={status}
            onChange={handleStatusChange}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} aria-label="查询">
            查询
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadRoles}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadRoles} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<RoleRecord>
          rowKey="id"
          columns={roleColumns}
          dataSource={roles}
          loading={loading}
          locale={{
            emptyText: <EmptyState message="暂无角色数据" />,
          }}
          pagination={{
            current: pageResult?.current ?? currentPage,
            pageSize: pageResult?.size ?? pageSize,
            total: pageResult?.total ?? 0,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={handleTableChange}
        />
      </ProCard>
    </div>
  );
}

function RoleStatusTag({ status }: { status: string }) {
  if (status === 'ACTIVE') {
    return <Tag color="green">启用</Tag>;
  }

  if (status === 'INACTIVE') {
    return <Tag color="default">停用</Tag>;
  }

  return <Tag>{status}</Tag>;
}

function formatDateTime(value: string): string {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}
