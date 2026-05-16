import { AuditOutlined, CheckOutlined, HistoryOutlined, ReloadOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Modal, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  approveApprovalTask,
  listApprovalRecords,
  listMyPendingApprovalTasks,
  type ApprovalTaskRecord,
} from '@/services/approval/approvalService';

export function ApprovalCenterPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<{ opinion: string }>();
  const [tasks, setTasks] = useState<ApprovalTaskRecord[]>([]);
  const [selectedTask, setSelectedTask] = useState<ApprovalTaskRecord | null>(null);
  const [historyTask, setHistoryTask] = useState<ApprovalTaskRecord | null>(null);
  const [historyRecords, setHistoryRecords] = useState<ApprovalTaskRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);

  const columns: ColumnsType<ApprovalTaskRecord> = [
    { title: '业务类型', dataIndex: 'businessType', render: (value) => value || '-' },
    { title: '业务单号', dataIndex: 'businessNo', render: (value) => value || '-' },
    { title: '业务 ID', dataIndex: 'businessId', width: 120, render: (value) => value || '-' },
    { title: '审批模式', dataIndex: 'approvalMode', width: 120, render: (value) => value || '-' },
    { title: '状态', dataIndex: 'status', width: 120, render: renderStatus },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size={8}>
          <Button
            size="small"
            icon={<AuditOutlined />}
            aria-label={`审批 ${record.id}`}
            onClick={() => {
              form.resetFields();
              setSelectedTask(record);
            }}
          >
            审批
          </Button>
          <Button
            size="small"
            icon={<HistoryOutlined />}
            aria-label={`审批历史 ${record.id}`}
            onClick={() => void openHistory(record)}
          >
            历史
          </Button>
        </Space>
      ),
    },
  ];

  const loadTasks = useCallback(async () => {
    setLoading(true);
    try {
      const records = await listMyPendingApprovalTasks();
      setTasks(records);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询待审批任务失败');
    } finally {
      setLoading(false);
    }
  }, [message]);

  useEffect(() => {
    void loadTasks();
  }, [loadTasks]);

  const submitDecision = async (approved: boolean) => {
    if (!selectedTask) return;

    const values = await form.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      await approveApprovalTask({
        taskId: selectedTask.id,
        approved,
        opinion: values.opinion.trim(),
      });
      message.success(approved ? '审批已通过' : '审批已驳回');
      setSelectedTask(null);
      await loadTasks();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '提交审批结果失败');
    } finally {
      setSubmitting(false);
    }
  };

  const openHistory = async (record: ApprovalTaskRecord) => {
    if (!record.businessType || !record.businessId) {
      message.error('缺少业务类型或业务 ID，无法查询审批历史');
      return;
    }

    setHistoryTask(record);
    setHistoryRecords([]);
    setHistoryLoading(true);
    try {
      const records = await listApprovalRecords({
        businessType: record.businessType,
        businessId: record.businessId,
        businessNo: record.businessNo ?? undefined,
      });
      setHistoryRecords(records);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询审批历史失败');
    } finally {
      setHistoryLoading(false);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card
        title="审批中心"
        extra={
          <Button icon={<ReloadOutlined />} loading={loading} onClick={loadTasks}>
            刷新待办
          </Button>
        }
      >
        {tasks.length === 0 ? (
          <Typography.Text type="secondary">暂无待审批任务。</Typography.Text>
        ) : (
          <Table rowKey="id" columns={columns} dataSource={tasks} pagination={false} loading={loading} />
        )}
      </Card>

      <Modal
        title={selectedTask?.businessNo || '审批任务'}
        open={Boolean(selectedTask)}
        onCancel={() => setSelectedTask(null)}
        footer={[
          <Button key="reject" danger loading={submitting} onClick={() => void submitDecision(false)}>
            驳回
          </Button>,
          <Button key="approve" type="primary" icon={<CheckOutlined />} loading={submitting} onClick={() => void submitDecision(true)}>
            通过
          </Button>,
        ]}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Form.Item label="审批意见" name="opinion" rules={[{ required: true, message: '请输入审批意见' }]}>
            <Input.TextArea rows={4} maxLength={200} showCount />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="审批历史"
        open={Boolean(historyTask)}
        onCancel={() => setHistoryTask(null)}
        footer={null}
        destroyOnHidden
        width={760}
      >
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Typography.Text type="secondary">
            {historyTask?.businessNo || historyTask?.businessId || '-'}
          </Typography.Text>
          <Table
            rowKey="id"
            columns={historyColumns}
            dataSource={historyRecords}
            pagination={false}
            loading={historyLoading}
            size="small"
          />
        </Space>
      </Modal>
    </Space>
  );
}

function renderStatus(value?: string | null) {
  return value ? <Tag color="processing">{value}</Tag> : '-';
}

const historyColumns: ColumnsType<ApprovalTaskRecord> = [
  { title: '状态', dataIndex: 'status', width: 120, render: renderStatus },
  { title: '审批意见', dataIndex: 'opinion', render: (value) => value || '-' },
  { title: '审批人', dataIndex: 'approverId', width: 140, render: (value) => value || '-' },
  { title: '审批时间', dataIndex: 'approvedAt', width: 180, render: (_, record) => record.approvedAt || record.createdAt || '-' },
];
