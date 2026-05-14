import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { DashboardPage } from './DashboardPage';

describe('DashboardPage', () => {
  it('renders the quiet enterprise operation workspace', () => {
    render(<DashboardPage />);

    expect(screen.getByRole('heading', { name: '工作台首页' })).toBeInTheDocument();
    expect(screen.getByText('集中查看订单、生产、库存、审批和发运待办')).toBeInTheDocument();
    expect(screen.getAllByText('待审批').length).toBeGreaterThan(0);
    expect(screen.getAllByText('库存预警').length).toBeGreaterThan(0);
    expect(screen.getAllByText('待发运').length).toBeGreaterThan(0);
    expect(screen.getByText('SO20260506001')).toBeInTheDocument();
    expect(screen.getByText(/库存低水位/)).toBeInTheDocument();
  });
});
