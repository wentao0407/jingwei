import { Spin, Typography } from 'antd';

interface LoadingStateProps {
  message?: string;
}

const DEFAULT_LOADING_MESSAGE = '数据加载中';

export function LoadingState({ message = DEFAULT_LOADING_MESSAGE }: LoadingStateProps) {
  return (
    <div className="state-view state-view-loading">
      <Spin />
      <Typography.Text type="secondary">{message}</Typography.Text>
    </div>
  );
}
