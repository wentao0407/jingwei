import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { InventoryStockPlaceholderPage } from './InventoryStockPlaceholderPage';
import { pageInventoryMaterials, pageInventorySkus } from '@/services/inventory/inventoryService';

vi.mock('@/services/inventory/inventoryService', () => ({
  pageInventoryMaterials: vi.fn(),
  pageInventorySkus: vi.fn(),
}));

const mockedPageInventorySkus = vi.mocked(pageInventorySkus);
const mockedPageInventoryMaterials = vi.mocked(pageInventoryMaterials);

describe('InventoryStockPlaceholderPage', () => {
  beforeEach(() => {
    mockedPageInventorySkus.mockReset();
    mockedPageInventoryMaterials.mockReset();
  });

  it('loads SKU inventory records', async () => {
    mockedPageInventorySkus.mockResolvedValue({
      records: [{ id: '1', skuId: '90001', skuCode: 'SKU-RED-S', warehouseId: '30001', availableQty: 8, totalQty: 10 }],
      total: 1,
      current: 1,
      size: 20,
      pages: 1,
    });

    render(<InventoryStockPlaceholderPage inventoryType="SKU" />);

    expect(screen.getByText('库存 SKU')).toBeInTheDocument();
    expect(await screen.findByText('SKU-RED-S')).toBeInTheDocument();
    expect(mockedPageInventorySkus).toHaveBeenCalledWith({ current: 1, size: 20 });
  });

  it('loads material inventory records', async () => {
    mockedPageInventoryMaterials.mockResolvedValue({
      records: [
        { id: '2', materialId: '80001', materialName: 'Cotton Fabric', warehouseId: '30002', availableQty: 15, totalQty: 20 },
      ],
      total: 1,
      current: 1,
      size: 20,
      pages: 1,
    });

    render(<InventoryStockPlaceholderPage inventoryType="MATERIAL" />);

    expect(screen.getByText('库存物料')).toBeInTheDocument();
    expect(await screen.findByText('Cotton Fabric')).toBeInTheDocument();
    expect(mockedPageInventoryMaterials).toHaveBeenCalledWith({ current: 1, size: 20 });
  });
});
