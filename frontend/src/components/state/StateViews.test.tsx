import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { EmptyState } from './EmptyState';
import { ErrorState } from './ErrorState';
import { LoadingState } from './LoadingState';

describe('shared state views', () => {
  it('renders a loading message for async content', () => {
    render(<LoadingState message="正在加载订单数据" />);

    expect(screen.getByText('正在加载订单数据')).toBeInTheDocument();
  });

  it('renders an error message and supports retry action', () => {
    const handleRetry = vi.fn();

    render(<ErrorState message="订单加载失败" onRetry={handleRetry} />);

    expect(screen.getByText('订单加载失败')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '重新加载' }));
    expect(handleRetry).toHaveBeenCalledTimes(1);
  });

  it('renders an empty message', () => {
    render(<EmptyState message="暂无用户数据" />);

    expect(screen.getByText('暂无用户数据')).toBeInTheDocument();
  });
});
