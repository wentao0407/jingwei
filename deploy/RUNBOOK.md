# JingWei 运维手册

## 1. 启动命令

### 开发环境（dev）

```bash
# 使用默认 dev profile（application.yml 中 spring.profiles.active=dev）
mvn spring-boot:run

# 或指定 profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- 端口：8080
- 上下文路径：/api
- 数据库：localhost:5432/jingwei_dev（用户 jingwei / jingwei123）
- Redis：localhost:6379（密码 root123）
- JWT：硬编码开发密钥
- 日志级别：DEBUG

### 生产环境（prod）

```bash
# 方式一：systemd 管理（推荐）
sudo systemctl start jingwei
sudo systemctl stop jingwei
sudo systemctl restart jingwei
sudo systemctl status jingwei

# 方式二：手动启动
cd /opt/jingwei
source .env
java -Xms256m -Xmx512m \
    -Djava.io.tmpdir=/opt/jingwei/tmp \
    -Dspring.profiles.active=prod \
    -jar jingwei-1.0.0-SNAPSHOT.jar
```

## 2. 环境变量（.env）

| 变量 | 说明 | 示例 |
|------|------|------|
| `DB_HOST` | PostgreSQL 地址 | `localhost` |
| `DB_PORT` | PostgreSQL 端口 | `5432` |
| `DB_NAME` | PostgreSQL 数据库名 | `jingwei` |
| `DB_USERNAME` | PostgreSQL 用户名 | `jingwei` |
| `DB_PASSWORD` | PostgreSQL 密码 | `your_db_password` |
| `REDIS_HOST` | Redis 地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `your_redis_password` |
| `JWT_SECRET` | JWT 签名密钥（≥256 位） | `your_jwt_secret_here...` |

环境变量文件位置：`/opt/jingwei/.env`

## 3. 健康检查

```bash
# 基础健康检查
curl http://localhost:8080/api/actuator/health

# 返回示例
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

- 端点已配置为白名单，无需认证
- `show-details: always` 显示各组件详情
- 状态为 `DOWN` 时需排查对应组件

## 4. 日志路径

| 环境 | 路径 | 说明 |
|------|------|------|
| dev | 控制台输出 | IDE 或终端直接查看 |
| prod | `/opt/jingwei/logs/jingwei.log` | 文件日志（100MB 轮转，保留 30 天，上限 1GB） |
| prod | `journalctl -u jingwei` | systemd 日志（与文件日志同步输出） |

### 常用日志命令

```bash
# 实时跟踪日志
sudo journalctl -u jingwei -f

# 查看最近 100 行
sudo journalctl -u jingwei -n 100

# 查看今天的日志
sudo journalctl -u jingwei --since today

# 查看错误日志
sudo journalctl -u jingwei -p err

# 直接查看文件日志
tail -f /opt/jingwei/logs/jingwei.log
```

## 5. Profile 对比

| 配置项 | dev | prod |
|--------|-----|------|
| 数据库 | localhost:5432/jingwei_dev | ${DB_HOST}:${DB_PORT}/${DB_NAME} |
| 数据库用户 | 硬编码 jingwei | ${DB_USERNAME} |
| 数据库密码 | 硬编码 | ${DB_PASSWORD} |
| Redis 密码 | 硬编码 | ${REDIS_PASSWORD} |
| JWT 密钥 | 硬编码 | ${JWT_SECRET} |
| 连接池最大 | 10 | 20 |
| 连接池空闲 | 5 | 10 |
| 日志级别 | DEBUG | INFO |
| 文件日志 | 无 | /opt/jingwei/logs/ |
| Flyway | 启用 | 启用 |

## 6. 快速诊断

```bash
# 检查服务状态
sudo systemctl status jingwei

# 检查端口占用
ss -tlnp | grep 8080

# 检查数据库连接
psql -h localhost -U jingwei -d jingwei -c "SELECT 1;"

# 检查 Redis 连接
redis-cli ping

# 检查 Nginx 配置
sudo nginx -t

# 重新加载 Nginx
sudo systemctl reload nginx
```
