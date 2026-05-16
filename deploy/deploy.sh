#!/bin/bash
# JingWei 一键部署脚本
# 使用方式: sudo bash deploy/deploy.sh
set -euo pipefail

APP_NAME="jingwei"
APP_USER="jingwei"
APP_GROUP="jingwei"
DEPLOY_DIR="/opt/${APP_NAME}"
JAR_NAME="jingwei-1.0.0-SNAPSHOT.jar"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"

echo "=== JingWei 生产部署 ==="

if [ "$(id -u)" -ne 0 ]; then
    echo "请使用 root 权限运行: sudo bash deploy/deploy.sh"
    exit 1
fi

if ! id "${APP_USER}" >/dev/null 2>&1; then
    echo "系统用户 ${APP_USER} 不存在，请先执行: sudo useradd -r -s /sbin/nologin ${APP_USER}"
    exit 1
fi

if [ ! -f "target/${JAR_NAME}" ]; then
    echo "未找到后端 JAR: target/${JAR_NAME}"
    echo "请先执行: mvn clean package"
    exit 1
fi

if [ ! -f "frontend/dist/index.html" ]; then
    echo "未找到前端构建产物: frontend/dist/index.html"
    echo "请先执行: cd frontend && pnpm build"
    exit 1
fi

# 1. 创建部署目录
echo "[1/5] 创建部署目录..."
mkdir -p "${DEPLOY_DIR}/logs" "${DEPLOY_DIR}/tmp"

# 2. 部署后端 JAR
echo "[2/5] 部署后端 JAR..."
cp target/"${JAR_NAME}" "${DEPLOY_DIR}/"
echo "  -> ${DEPLOY_DIR}/${JAR_NAME}"

# 3. 部署前端产物
echo "[3/5] 部署前端产物..."
rm -rf "${DEPLOY_DIR}/frontend"
cp -r frontend/dist "${DEPLOY_DIR}/frontend"
echo "  -> ${DEPLOY_DIR}/frontend/"

# 4. 部署配置文件
echo "[4/5] 部署配置文件..."
if [ ! -f "${DEPLOY_DIR}/.env" ]; then
    cp deploy/.env.example "${DEPLOY_DIR}/.env"
    echo "  -> .env 已创建，请编辑填入实际密码: ${DEPLOY_DIR}/.env"
else
    echo "  -> .env 已存在，跳过"
fi

chown -R "${APP_USER}:${APP_GROUP}" "${DEPLOY_DIR}"
chown root:"${APP_GROUP}" "${DEPLOY_DIR}/.env"
chmod 640 "${DEPLOY_DIR}/.env"
echo "  -> 运行目录和 .env 权限已收口"

cp deploy/jingwei.service "${SERVICE_FILE}"
systemctl daemon-reload
echo "  -> systemd 服务已注册"

# 5. Nginx 配置
echo "[5/5] Nginx 配置..."
if [ -d /etc/nginx/conf.d ]; then
    cp deploy/nginx-jingwei.conf /etc/nginx/conf.d/jingwei.conf
    nginx -t && systemctl reload nginx
    echo "  -> Nginx 配置已生效"
else
    echo "  -> 未检测到 Nginx，请手动部署 deploy/nginx-jingwei.conf"
fi

echo ""
echo "=== 部署完成 ==="
echo "启动服务: sudo systemctl start ${APP_NAME}"
echo "查看日志: sudo journalctl -u ${APP_NAME} -f"
echo "访问地址: http://$(hostname -I | awk '{print $1}')"
