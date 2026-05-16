import { PrinterOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Select, Space } from 'antd';
import { useState } from 'react';
import {
  generateDocPrint,
  generateSkuLabelPrint,
  openBrowserPrint,
  renderPrintHtml,
  type PrintData,
} from '@/services/warehouse/printService';
import { getApiErrorMessage } from '@/services/http/apiClient';

const docTypeOptions = [
  { label: 'SKU 标签', value: 'sku-label' },
  { label: '入库单', value: 'inbound' },
  { label: '出库单', value: 'outbound' },
  { label: '拣货单', value: 'pick-list' },
  { label: '装箱单', value: 'packing-list' },
];

export function PrintPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<{ docType: string; docId: string }>();
  const [loading, setLoading] = useState(false);
  const [previewData, setPreviewData] = useState<PrintData | null>(null);

  async function handleGenerate() {
    try {
      const values = await form.validateFields();
      setLoading(true);
      let data: PrintData;
      if (values.docType === 'sku-label') {
        data = await generateSkuLabelPrint(values.docId);
      } else {
        data = await generateDocPrint(values.docType, values.docId);
      }
      setPreviewData(data);
    } catch (error) {
      if (typeof error === 'object' && error !== null && 'errorFields' in error) return;
      message.error(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }

  function handlePrint() {
    if (!previewData) return;
    const html = renderPrintHtml(previewData);
    openBrowserPrint(html);
  }

  return (
    <div className="system-page">
      <section className="system-page-topbar">
        <div><h1>条码打印</h1><p>生成 SKU 标签、入库单、拣货单等打印数据。</p></div>
      </section>
      <Card>
        <Form form={form} layout="inline" initialValues={{ docType: 'sku-label' }}>
          <Form.Item label="打印类型" name="docType" rules={[{ required: true }]}>
            <Select options={docTypeOptions} style={{ width: 160 }} />
          </Form.Item>
          <Form.Item label="单据ID" name="docId" rules={[{ required: true, message: '请输入单据ID' }]}>
            <Input placeholder="输入单据ID" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={handleGenerate}>
              生成打印数据
            </Button>
          </Form.Item>
          {previewData ? (
            <Form.Item>
              <Button icon={<PrinterOutlined />} onClick={handlePrint}>打印</Button>
            </Form.Item>
          ) : null}
        </Form>
      </Card>
      {previewData ? (
        <Card title="打印预览" style={{ marginTop: 16 }}>
          <div dangerouslySetInnerHTML={{ __html: renderPrintHtml(previewData) }} />
        </Card>
      ) : null}
    </div>
  );
}
