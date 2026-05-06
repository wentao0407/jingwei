import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { App, Typography } from 'antd';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { login } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
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
      message.success('登录成功');
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
    <main className="login-page">
      <section className="login-panel">
        <Typography.Title level={1}>JingWei</Typography.Title>
        <Typography.Paragraph type="secondary">
          服装生产销售全链路管理系统
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
      </section>
    </main>
  );
}
