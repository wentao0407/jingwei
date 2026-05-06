import { render, screen } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { LoginPage } from './LoginPage';

describe('LoginPage', () => {
  it('renders the quiet enterprise login hero', () => {
    render(
      <AntdApp>
        <MemoryRouter future={{ v7_relativeSplatPath: true, v7_startTransition: true }}>
          <LoginPage />
        </MemoryRouter>
      </AntdApp>,
    );

    expect(screen.getByText('JingWei 经纬')).toBeInTheDocument();
    expect(screen.getByText('把订单、生产、仓储和物流放在一张清晰的工作台上。')).toBeInTheDocument();
    expect(screen.getByText('在制订单')).toBeInTheDocument();
    expect(screen.getByText('准交率')).toBeInTheDocument();
    expect(screen.getByText('待审批')).toBeInTheDocument();
  });
});
