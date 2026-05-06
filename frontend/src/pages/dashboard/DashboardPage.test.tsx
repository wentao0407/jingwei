import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { DashboardPage } from './DashboardPage';

describe('DashboardPage', () => {
  it('renders the quiet enterprise order workspace', () => {
    render(<DashboardPage />);

    expect(screen.getByRole('heading', { name: '销售订单' })).toBeInTheDocument();
    expect(screen.getByText('按客户、交期、状态跟踪订单履约进度')).toBeInTheDocument();
    expect(screen.getByText('今日新增')).toBeInTheDocument();
    expect(screen.getByText('本周交付')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '新建订单' })).toBeInTheDocument();
    expect(screen.getByText('SO20260506001')).toBeInTheDocument();
  });
});
