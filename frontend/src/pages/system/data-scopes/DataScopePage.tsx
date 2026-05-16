import { SaveOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { configureDataScope, queryDataScope, type DataScopePayload, type DataScopeRecord } from '@/services/system/systemExtService';

export function DataScopePage() {
  const { message } = App.useApp();
  const [queryForm] = Form.useForm<{ roleId: string }>();
  const [saveForm] = Form.useForm<{ scopes: string }>();
  const [roleId, setRoleId] = useState('');
  const [records, setRecords] = useState<DataScopeRecord[]>([]);
  const [loading, setLoading] = useState(false);

  const handleQuery = async () => {
    const values = await queryForm.validateFields().catch(() => null);
    if (!values) return;

    const nextRoleId = values.roleId.trim();
    setLoading(true);
    try {
      const scopes = await queryDataScope(nextRoleId);
      setRoleId(nextRoleId);
      setRecords(scopes);
      saveForm.setFieldsValue({ scopes: scopes.map((scope) => `${scope.scopeType}:${scope.scopeValue}`).join('\n') });
    } catch (error) {
      message.error(error instanceof Error ? error.message : '数据范围查询失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!roleId) {
      message.error('请先查询角色');
      return;
    }

    const values = await saveForm.validateFields().catch(() => null);
    if (!values) return;

    setLoading(true);
    try {
      const scopes = parseScopes(values.scopes);
      await configureDataScope(roleId, { scopes });
      message.success('数据范围已保存');
      setRecords(scopes.map((scope, index) => ({ id: String(index), roleId, ...scope })));
    } catch (error) {
      message.error(error instanceof Error ? error.message : '数据范围保存失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="数据范围">
        <Form form={queryForm} layout="inline">
          <Form.Item label="角色 ID" name="roleId" rules={[{ required: true, whitespace: true, message: '请输入角色 ID' }]}>
            <Input placeholder="角色 ID" allowClear />
          </Form.Item>
          <Form.Item>
            <Button icon={<SearchOutlined />} loading={loading} onClick={() => void handleQuery()}>查询</Button>
          </Form.Item>
        </Form>
      </Card>
      <Card
        title="范围配置"
        extra={<Button type="primary" icon={<SaveOutlined />} loading={loading} onClick={() => void handleSave()}>保存</Button>}
      >
        <Form form={saveForm} layout="vertical">
          <Form.Item label="范围规则" name="scopes">
            <Input.TextArea rows={5} placeholder="DEPARTMENT:10001" />
          </Form.Item>
        </Form>
        <Table<DataScopeRecord> rowKey={getScopeRowKey} columns={columns} dataSource={records} pagination={false} loading={loading} />
      </Card>
    </Space>
  );
}

const columns: ColumnsType<DataScopeRecord> = [
  { title: '范围类型', dataIndex: 'scopeType', key: 'scopeType', render: renderText },
  { title: '范围值', dataIndex: 'scopeValue', key: 'scopeValue', render: renderText },
];

function parseScopes(value?: string): DataScopePayload[] {
  return (value ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [scopeType, ...scopeValueParts] = line.split(':');
      return { scopeType: scopeType.trim(), scopeValue: scopeValueParts.join(':').trim() };
    })
    .filter((scope) => Boolean(scope.scopeType && scope.scopeValue));
}

function renderText(value?: string | null) {
  return value || '-';
}

function getScopeRowKey(record: DataScopeRecord) {
  return record.id || `${record.roleId || 'role'}-${record.scopeType || 'scope'}-${record.scopeValue || 'value'}`;
}
