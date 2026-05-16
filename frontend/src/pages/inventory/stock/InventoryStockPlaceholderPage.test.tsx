import { fireEvent, render, screen, waitFor } from '@testing-library/react';
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

  it('filters SKU inventory records', async () => {
    mockedPageInventorySkus.mockResolvedValue({ records: [], total: 0, current: 1, size: 20, pages: 0 });

    render(<InventoryStockPlaceholderPage inventoryType="SKU" />);

    fireEvent.change(screen.getByLabelText('SKU ID'), { target: { value: ' 90001 ' } });
    fireEvent.change(screen.getByLabelText('仓库 ID'), { target: { value: ' 30001 ' } });
    fireEvent.change(screen.getByLabelText('批次号'), { target: { value: ' B-01 ' } });
    fireEvent.click(screen.getByRole('button', { name: '搜索库存' }));

    await waitFor(() => expect(mockedPageInventorySkus).toHaveBeenLastCalledWith({
      current: 1,
      size: 20,
      skuId: '90001',
      warehouseId: '30001',
      batchNo: 'B-01',
    }));
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

  it('filters material inventory records', async () => {
    mockedPageInventoryMaterials.mockResolvedValue({ records: [], total: 0, current: 1, size: 20, pages: 0 });

    render(<InventoryStockPlaceholderPage inventoryType="MATERIAL" />);

    fireEvent.change(screen.getByLabelText('物料 ID'), { target: { value: ' 80001 ' } });
    fireEvent.change(screen.getByLabelText('仓库 ID'), { target: { value: ' 30002 ' } });
    fireEvent.change(screen.getByLabelText('批次号'), { target: { value: ' M-01 ' } });
    fireEvent.click(screen.getByRole('button', { name: '搜索库存' }));

    await waitFor(() => expect(mockedPageInventoryMaterials).toHaveBeenLastCalledWith({
      current: 1,
      size: 20,
      materialId: '80001',
      warehouseId: '30002',
      batchNo: 'M-01',
    }));
  });
});
