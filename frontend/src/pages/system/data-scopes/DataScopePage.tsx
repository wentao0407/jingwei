import { SaveOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { listWarehouses, type WarehouseRecord } from '@/services/master/warehouseService';
import { listRoles, type RoleRecord } from '@/services/system/roleService';
import { configureDataScope, queryDataScope, type DataScopePayload, type DataScopeRecord } from '@/services/system/systemExtService';

const scopeTypeOptions = [
  { label: '全部数据', value: 'ALL' },
  { label: '按仓库', value: 'WAREHOUSE' },
  { label: '按部门', value: 'DEPT' },
];

interface DataScopeFormValues {
  roleId?: string;
  scopeType?: string;
  warehouseIds?: string[];
  deptIds?: string;
}

export function DataScopePage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<DataScopeFormValues>();
  const [roleId, setRoleId] = useState('');
  const [roles, setRoles] = useState<RoleRecord[]>([]);
  const [warehouses, setWarehouses] = useState<WarehouseRecord[]>([]);
  const [records, setRecords] = useState<DataScopeRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const scopeType = Form.useWatch('scopeType', form);
  const warehouseNameById = useMemo(() => buildWarehouseNameMap(warehouses), [warehouses]);

  const loadOptions = useCallback(async () => {
    setLoading(true);
    try {
      const [rolePage, warehouseList] = await Promise.all([
        listRoles({ current: 1, size: 100, status: 'ACTIVE' }),
        listWarehouses({ status: 'ACTIVE' }),
      ]);
      setRoles(rolePage.records ?? []);
      setWarehouses(warehouseList);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '基础选项加载失败');
    } finally {
      setLoading(false);
    }
  }, [message]);

  useEffect(() => {
    void loadOptions();
  }, [loadOptions]);

  const handleQuery = async () => {
    const values = await form.validateFields(['roleId']).catch(() => null);
    if (!values?.roleId) return;

    setLoading(true);
    try {
      const scopes = await queryDataScope(values.roleId);
      setRoleId(values.roleId);
      setRecords(scopes);
      form.setFieldsValue(toFormValues(values.roleId, scopes));
    } catch (error) {
      message.error(error instanceof Error ? error.message : '数据范围查询失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values?.roleId) return;

    const scopes = toDataScopePayload(values);
    setLoading(true);
    try {
      await configureDataScope(values.roleId, { scopes });
      message.success('数据范围已保存');
      setRoleId(values.roleId);
      setRecords(scopes.map((scope, index) => ({ id: String(index), roleId: values.roleId, ...scope })));
    } catch (error) {
      message.error(error instanceof Error ? error.message : '数据范围保存失败');
    } finally {
      setLoading(false);
    }
  };

  const columns = useMemo(() => buildColumns(warehouseNameById), [warehouseNameById]);

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="数据范围">
        <Form<DataScopeFormValues> form={form} layout="inline" initialValues={{ scopeType: 'ALL' }}>
          <Form.Item label="角色" name="roleId" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              allowClear
              aria-label="角色"
              options={roles.map((role) => ({ label: role.roleName, value: role.id }))}
              placeholder="选择角色"
              style={{ width: 220 }}
            />
          </Form.Item>
          <Form.Item>
            <Button icon={<SearchOutlined />} loading={loading} onClick={() => void handleQuery()}>查询</Button>
          </Form.Item>
        </Form>
      </Card>
      <Card
        title={roleId ? `范围配置：${getRoleName(roles, roleId)}` : '范围配置'}
        extra={<Button type="primary" icon={<SaveOutlined />} loading={loading} onClick={() => void handleSave()}>保存</Button>}
      >
        <Form<DataScopeFormValues> form={form} layout="vertical" initialValues={{ scopeType: 'ALL' }}>
          <Form.Item label="范围类型" name="scopeType" rules={[{ required: true, message: '请选择范围类型' }]}>
            <Select aria-label="范围类型" options={scopeTypeOptions} />
          </Form.Item>
          {scopeType === 'WAREHOUSE' ? (
            <Form.Item label="仓库范围" name="warehouseIds" rules={[{ required: true, message: '请选择仓库范围' }]}>
              <Select
                mode="multiple"
                aria-label="仓库范围"
                options={warehouses.map((warehouse) => ({ label: warehouse.name, value: warehouse.id }))}
                placeholder="选择仓库"
              />
            </Form.Item>
          ) : null}
          {scopeType === 'DEPT' ? (
            <Form.Item label="部门 ID" name="deptIds" normalize={normalizeTextInput} rules={[{ required: true, whitespace: true, message: '请输入部门 ID' }]}>
              <Input allowClear placeholder="多个部门用英文逗号分隔" />
            </Form.Item>
          ) : null}
        </Form>
        <Table<DataScopeRecord> rowKey={getScopeRowKey} columns={columns} dataSource={records} pagination={false} loading={loading} />
      </Card>
    </Space>
  );
}

function buildColumns(warehouseNameById: Map<string, string>): ColumnsType<DataScopeRecord> {
  return [
    { title: '范围类型', dataIndex: 'scopeType', key: 'scopeType', render: renderScopeType },
    { title: '范围值', dataIndex: 'scopeValue', key: 'scopeValue', render: (value, record) => renderScopeValue(record, warehouseNameById, value) },
  ];
}

function toFormValues(roleId: string, scopes: DataScopeRecord[]): DataScopeFormValues {
  const scope = scopes[0];
  if (!scope) {
    return { roleId, scopeType: 'ALL', warehouseIds: [], deptIds: '' };
  }
  if (scope.scopeType === 'WAREHOUSE') {
    return { roleId, scopeType: 'WAREHOUSE', warehouseIds: splitScopeValue(scope.scopeValue), deptIds: '' };
  }
  if (scope.scopeType === 'DEPT' || scope.scopeType === 'DEPARTMENT') {
    return { roleId, scopeType: 'DEPT', deptIds: scope.scopeValue ?? '', warehouseIds: [] };
  }
  return { roleId, scopeType: 'ALL', warehouseIds: [], deptIds: '' };
}

function toDataScopePayload(values: DataScopeFormValues): DataScopePayload[] {
  if (values.scopeType === 'WAREHOUSE') {
    return [{ scopeType: 'WAREHOUSE', scopeValue: (values.warehouseIds ?? []).join(',') }];
  }
  if (values.scopeType === 'DEPT') {
    return [{ scopeType: 'DEPT', scopeValue: values.deptIds ?? '' }];
  }
  return [{ scopeType: 'ALL', scopeValue: 'ALL' }];
}

function splitScopeValue(value?: string | null): string[] {
  return (value ?? '').split(',').map((item) => item.trim()).filter(Boolean);
}

function renderScopeType(value?: string | null) {
  const labelByType: Record<string, string> = { ALL: '全部数据', WAREHOUSE: '按仓库', DEPT: '按部门', DEPARTMENT: '按部门' };
  return <Tag>{labelByType[value ?? ''] ?? value ?? '-'}</Tag>;
}

function renderScopeValue(record: DataScopeRecord, warehouseNameById: Map<string, string>, value?: string | null) {
  if (record.scopeType === 'WAREHOUSE') {
    return splitScopeValue(value).map((id) => warehouseNameById.get(id) ?? id).join('、') || '-';
  }
  return value || '-';
}

function buildWarehouseNameMap(warehouses: WarehouseRecord[]): Map<string, string> {
  return new Map(warehouses.map((warehouse) => [warehouse.id, warehouse.name]));
}

function getRoleName(roles: RoleRecord[], roleId: string) {
  return roles.find((role) => role.id === roleId)?.roleName ?? roleId;
}

function normalizeTextInput(value?: string) {
  return typeof value === 'string' ? value.trim() : value;
}

function getScopeRowKey(record: DataScopeRecord) {
  return record.id || `${record.roleId || 'role'}-${record.scopeType || 'scope'}-${record.scopeValue || 'value'}`;
}
