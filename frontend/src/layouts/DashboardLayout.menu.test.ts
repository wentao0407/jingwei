import { describe, expect, it } from 'vitest';
import { buildMenuItems } from './menuItems';
import type { AuthMenuItem } from '@/shared/storage/authSessionStorage';

describe('DashboardLayout menu mapping', () => {
  it('uses backend menu ids as stable keys when normalized paths overlap', () => {
    const menuTree: AuthMenuItem[] = [
      {
        id: 'approval-root',
        parentId: '0',
        name: '审批中心',
        type: 'DIRECTORY',
        path: '/approval',
        children: [
          {
            id: 'approval-task',
            parentId: 'approval-root',
            name: '我的审批',
            type: 'MENU',
            path: '/approval/task',
          },
        ],
      },
    ];

    const menuItems = buildMenuItems(menuTree);

    expect(menuItems[0].path).toBe('/approval/tasks');
    expect(menuItems[0].key).toBe('approval-root');
    expect(menuItems[0].children?.[0].path).toBe('/approval/tasks');
    expect(menuItems[0].children?.[0].key).toBe('approval-task');
  });
});
