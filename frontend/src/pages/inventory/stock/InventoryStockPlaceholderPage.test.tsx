import { render, screen } from '@testing-library/react';
import { InventoryStockPlaceholderPage } from './InventoryStockPlaceholderPage';

describe('InventoryStockPlaceholderPage', () => {
  it('shows SKU inventory contract fallback', () => {
    render(<InventoryStockPlaceholderPage inventoryType="SKU" />);
    expect(screen.getByText('库存 SKU')).toBeInTheDocument();
    expect(screen.getByText(/当前后端尚未暴露库存 SKU 查询 REST 接口/)).toBeInTheDocument();
  });

  it('shows material inventory contract fallback', () => {
    render(<InventoryStockPlaceholderPage inventoryType="MATERIAL" />);
    expect(screen.getByText('库存物料')).toBeInTheDocument();
    expect(screen.getByText(/当前后端尚未暴露库存物料查询 REST 接口/)).toBeInTheDocument();
  });
});
