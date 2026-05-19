import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { DashboardPage } from './DashboardPage';

describe('DashboardPage', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders the quiet enterprise operation workspace', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-05-17T09:30:00+08:00'));

    render(<DashboardPage />);

    expect(screen.getByRole('heading', { name: '工作台首页' })).toBeInTheDocument();
    expect(screen.getByText('集中查看订单、生产、库存、审批和发运待办')).toBeInTheDocument();
    expect(screen.getByText('2026-05-17 09:30')).toBeInTheDocument();
    expect(screen.getAllByText('待审批').length).toBeGreaterThan(0);
    expect(screen.getAllByText('库存预警').length).toBeGreaterThan(0);
    expect(screen.getAllByText('待发运').length).toBeGreaterThan(0);
    expect(screen.getByText('SO20260506001')).toBeInTheDocument();
    expect(screen.getByText('高优先级')).toBeInTheDocument();
    expect(screen.getAllByText(/库存复核/).length).toBeGreaterThan(0);
  });
});
