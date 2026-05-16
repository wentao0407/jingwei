import { BellOutlined, LogoutOutlined, ShopOutlined } from '@ant-design/icons';
import { PageContainer, ProLayout } from '@ant-design/pro-components';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Badge, Button, Space, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { buildMenuItems, fallbackMenuItems } from './menuItems';
import { getUnreadNotificationCount } from '@/services/notification/notificationService';
import { clearAuthSession, getAuthSession } from '@/shared/storage/authSessionStorage';
import { clearAccessToken } from '@/shared/storage/tokenStorage';

export function DashboardLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const authSession = getAuthSession();
  const menuItems = buildMenuItems(authSession?.menuTree ?? []);
  const pageMeta = getPageMeta(location.pathname);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadUnreadCount() {
      try {
        const count = await getUnreadNotificationCount();
        if (active) {
          setUnreadCount(count);
        }
      } catch {
        if (active) {
          setUnreadCount(0);
        }
      }
    }

    void loadUnreadCount();

    return () => {
      active = false;
    };
  }, []);

  const handleLogout = () => {
    clearAccessToken();
    clearAuthSession();
    navigate('/login', { replace: true });
  };

  return (
    <ProLayout
      className="quiet-dashboard-layout"
      title="经纬"
      logo={<ShopOutlined />}
      layout="side"
      route={{
        path: '/',
        routes: menuItems.length > 0 ? menuItems : fallbackMenuItems,
      }}
      menuItemRender={(item, dom) => (
        <button className="layout-menu-button" onClick={() => navigate(item.path ?? '/')}>
          {dom}
        </button>
      )}
      actionsRender={() => [
        <Badge key="notice" count={unreadCount} size="small">
          <Button
            type="text"
            icon={<BellOutlined />}
            aria-label="通知"
            onClick={() => navigate('/notification/list')}
          />
        </Badge>,
        <Button
          key="logout"
          type="text"
          icon={<LogoutOutlined />}
          onClick={handleLogout}
          aria-label="退出登录"
        />,
      ]}
      avatarProps={{
        title: authSession?.realName || authSession?.username || '经纬用户',
      }}
    >
      <PageContainer
        title={pageMeta.title}
        subTitle={pageMeta.subTitle}
        extra={
          <Space>
            <Typography.Text type="secondary">开发环境</Typography.Text>
          </Space>
        }
      >
        <Outlet />
      </PageContainer>
    </ProLayout>
  );
}

function getPageMeta(pathname: string) {
  if (pathname === '/system/users') {
    return {
      title: '用户管理',
      subTitle: '维护账号、状态和角色授权入口',
    };
  }

  if (pathname === '/system/roles') {
    return {
      title: '角色管理',
      subTitle: '维护角色、状态和权限配置入口',
    };
  }

  if (pathname === '/system/menus') {
    return {
      title: '菜单管理',
      subTitle: '维护导航菜单、按钮权限点和前端路由配置',
    };
  }

  if (pathname === '/system/configs') {
    return {
      title: '系统配置',
      subTitle: '维护运行参数、密码策略和库存规则配置',
    };
  }

  if (pathname === '/system/audit-logs') {
    return {
      title: '操作日志',
      subTitle: '查询系统关键操作记录和审计追踪信息',
    };
  }

  if (pathname === '/system/data-scopes') {
    return {
      title: '数据范围',
      subTitle: '维护角色可访问的数据范围规则',
    };
  }

  if (pathname === '/master/materials') {
    return {
      title: '物料管理',
      subTitle: '维护面料、辅料和包装物料主数据',
    };
  }

  if (pathname === '/master/spus') {
    return {
      title: '款式管理',
      subTitle: '维护 SPU、颜色和 SKU 价格状态',
    };
  }

  if (pathname === '/master/size-groups') {
    return {
      title: '尺码组管理',
      subTitle: '维护尺码组和尺码明细',
    };
  }

  if (pathname === '/master/seasons') {
    return {
      title: '季节波段',
      subTitle: '维护季节周期、上市波段和关闭状态',
    };
  }

  if (pathname === '/master/warehouses') {
    return {
      title: '仓库库位',
      subTitle: '维护仓库档案、库位容量和冻结状态',
    };
  }

  if (pathname === '/master/coding-rules') {
    return {
      title: '编码规则',
      subTitle: '维护编码规则、规则段和流水号预览',
    };
  }

  if (pathname === '/master/categories') {
    return {
      title: '物料分类',
      subTitle: '维护物料分类树和启停状态',
    };
  }

  if (pathname === '/master/customers') {
    return {
      title: '客户管理',
      subTitle: '维护客户档案、等级、结算方式和启停状态',
    };
  }

  if (pathname === '/master/suppliers') {
    return {
      title: '供应商管理',
      subTitle: '维护供应商档案、资质状态和交货周期',
    };
  }

  if (pathname === '/order/sales') {
    return {
      title: '销售订单',
      subTitle: '按客户、交期、状态跟踪订单履约进度',
    };
  }

  if (pathname === '/order/production') {
    return {
      title: '生产订单',
      subTitle: '跟踪生产计划、工序状态、完工和入库进度',
    };
  }

  if (pathname === '/order/returns') {
    return {
      title: '退货单',
      subTitle: '查询客户退货单、状态和质检入库进度',
    };
  }

  if (pathname === '/procurement/orders') {
    return {
      title: '采购订单',
      subTitle: '跟踪采购审批、下发、收货和完成状态',
    };
  }

  if (pathname === '/procurement/asns') {
    return {
      title: '到货通知',
      subTitle: '跟踪 ASN 到货、收货和质检状态',
    };
  }

  if (pathname === '/procurement/bom-mrp') {
    return {
      title: 'BOM 与 MRP',
      subTitle: '查看物料清单和 MRP 采购建议',
    };
  }

  if (pathname === '/procurement/receiving') {
    return {
      title: '收货管理',
      subTitle: '从 ASN 创建收货单并逐行确认实收数量',
    };
  }

  if (pathname === '/procurement/putaway') {
    return {
      title: '上架管理',
      subTitle: '推荐库位并确认收货行上架位置',
    };
  }

  if (pathname === '/inventory/skus') {
    return { title: '库存 SKU', subTitle: '查看成品库存数量、锁定数量和批次分布' };
  }

  if (pathname === '/inventory/materials') {
    return { title: '库存物料', subTitle: '查看面辅料库存数量、质检数量和批次分布' };
  }

  if (pathname === '/inventory/inbounds') {
    return { title: '入库单', subTitle: '查询入库单、查看明细并确认入库' };
  }

  if (pathname === '/inventory/outbounds') {
    return { title: '出库单', subTitle: '查询出库单、查看明细并确认出库' };
  }

  if (pathname === '/inventory/stocktaking') {
    return { title: '盘点单', subTitle: '查询盘点单、开始盘点并录入实盘数量' };
  }

  if (pathname === '/inventory/alerts') {
    return { title: '库存预警', subTitle: '扫描库存阈值、查询预警记录并确认处理' };
  }

  if (pathname === '/warehouse/waves') {
    return { title: '波次拣货', subTitle: '创建波次、确认拣货、完成拣货单和取消波次' };
  }

  if (pathname === '/warehouse/shipments') {
    return { title: '发运单', subTitle: '按出库单确认发运并关联销售订单' };
  }

  if (pathname === '/approval/tasks') {
    return { title: '审批中心', subTitle: '查看我的待审批任务并提交审批意见' };
  }

  if (pathname === '/approval/configs') {
    return { title: '审批配置', subTitle: '维护业务审批模式、审批角色和启停状态' };
  }

  if (pathname === '/notification/list' || pathname === '/notification/preference') {
    return { title: '通知中心', subTitle: '查看站内通知、标记已读并维护通知偏好' };
  }

  if (pathname.startsWith('/report')) {
    return { title: '报表中心', subTitle: '查询库存台账、出入库流水、库龄和畅滞销分析' };
  }

  if (pathname.startsWith('/cost')) {
    return { title: '成本核算', subTitle: '查询生产订单成本归集和领料成本明细' };
  }

  return {
    title: '工作台首页',
    subTitle: '集中查看订单、生产、库存、审批和发运待办',
  };
}
