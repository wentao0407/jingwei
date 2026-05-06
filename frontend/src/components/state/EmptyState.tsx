import { Empty } from 'antd';

interface EmptyStateProps {
  message?: string;
}

const DEFAULT_EMPTY_MESSAGE = '暂无数据';

export function EmptyState({ message = DEFAULT_EMPTY_MESSAGE }: EmptyStateProps) {
  return (
    <div className="state-view state-view-empty">
      <Empty description={message} />
    </div>
  );
}
