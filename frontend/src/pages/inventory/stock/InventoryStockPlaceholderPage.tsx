import { Alert, Card, Descriptions, Space, Typography } from 'antd';

interface InventoryStockPlaceholderPageProps {
  inventoryType: 'SKU' | 'MATERIAL';
}

export function InventoryStockPlaceholderPage({ inventoryType }: InventoryStockPlaceholderPageProps) {
  const isSku = inventoryType === 'SKU';
  const title = isSku ? '库存 SKU' : '库存物料';
  const target = isSku ? '库存 SKU ' : '库存物料';

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title={title}>
        <Alert
          type="warning"
          showIcon
          message={`当前后端尚未暴露${target}查询 REST 接口`}
          description="库存核心服务已有领域模型和仓储实现，但目前只通过入库、出库、盘点、预警等业务接口间接暴露库存数据。前端先保留入口，避免误接不存在的接口。"
        />
        <Descriptions size="small" column={1} style={{ marginTop: 16 }}>
          <Descriptions.Item label="后续接口建议">
            <Typography.Text code>
              {isSku ? 'POST /inventory/sku/page' : 'POST /inventory/material/page'}
            </Typography.Text>
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </Space>
  );
}
