import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { App, Typography } from 'antd';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { login } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { setAuthSession } from '@/shared/storage/authSessionStorage';
import { setAccessToken } from '@/shared/storage/tokenStorage';

interface LocationState {
  from?: string;
}

interface LoginFormValues {
  username: string;
  password: string;
}

export function LoginPage() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const location = useLocation();
  const [submitting, setSubmitting] = useState(false);

  const state = location.state as LocationState | null;
  const redirectPath = state?.from ?? '/';

  const handleSubmit = async (values: LoginFormValues) => {
    if (!values.username.trim() || !values.password) {
      message.warning('请输入用户名和密码');
      return false;
    }

    setSubmitting(true);
    try {
      const response = await login({
        username: values.username.trim(),
        password: values.password,
      });
      setAccessToken(response.token);
      setAuthSession({
        userId: response.userId,
        username: response.username,
        realName: response.realName,
        roleIds: response.roleIds,
        permissions: response.permissions,
        menuTree: response.menuTree,
      });
      if (response.passwordExpired) {
        message.warning('密码已过期，请尽快修改密码');
      } else {
        message.success('登录成功');
      }
      navigate(redirectPath, { replace: true });
      return true;
    } catch (error) {
      message.error(getApiErrorMessage(error));
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="login-page quiet-login-page">
      <section className="login-visual-panel">
        <div className="quiet-brand-row">
          <span className="quiet-brand-mark">JW</span>
          <span>JingWei 经纬</span>
        </div>
        <div className="login-hero-copy">
          <Typography.Title level={1}>
            把订单、生产、仓储和物流放在一张清晰的工作台上。
          </Typography.Title>
          <Typography.Paragraph>
            安静克制的企业后台风格，适合长时间办公、表格录入、跨部门协作和稳定交付。
          </Typography.Paragraph>
          <div className="login-metric-grid">
            <div className="login-metric-card">
              <strong>128</strong>
              <span>在制订单</span>
            </div>
            <div className="login-metric-card">
              <strong>96%</strong>
              <span>准交率</span>
            </div>
            <div className="login-metric-card">
              <strong>18</strong>
              <span>待审批</span>
            </div>
          </div>
        </div>
      </section>
      <section className="login-panel">
        <Typography.Title level={2}>登录系统</Typography.Title>
        <Typography.Paragraph className="login-panel-hint">
          使用企业账号进入经纬生产管理系统
        </Typography.Paragraph>
        <LoginForm<LoginFormValues>
          submitter={{
            searchConfig: {
              submitText: '登录',
            },
            submitButtonProps: {
              loading: submitting,
              block: true,
            },
          }}
          onFinish={handleSubmit}
        >
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined />,
              autoComplete: 'username',
            }}
            placeholder="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined />,
              autoComplete: 'current-password',
            }}
            placeholder="密码"
            rules={[{ required: true, message: '请输入密码' }]}
          />
        </LoginForm>
        <div className="login-meta-row">
          <span>开发环境</span>
          <span>v0.1.0</span>
        </div>
      </section>
    </main>
  );
}
