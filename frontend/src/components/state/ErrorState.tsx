import { Button, Result } from 'antd';

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

const DEFAULT_ERROR_MESSAGE = '数据加载失败';

export function ErrorState({ message = DEFAULT_ERROR_MESSAGE, onRetry }: ErrorStateProps) {
  return (
    <Result
      className="state-view state-view-error"
      status="error"
      title={message}
      extra={
        onRetry ? (
          <Button type="primary" onClick={onRetry}>
            重新加载
          </Button>
        ) : null
      }
    />
  );
}
