# JingWei Deployment

## 1. Build Artifacts

Run these commands from the project root before deployment:

```bash
mvn clean package
cd frontend
pnpm install
pnpm build
cd ..
```

Expected artifacts:

- Backend JAR: `target/jingwei-1.0.0-SNAPSHOT.jar`
- Frontend assets: `frontend/dist/`

## 2. Prepare Server

```bash
sudo useradd -r -s /sbin/nologin jingwei
sudo mkdir -p /opt/jingwei/logs /opt/jingwei/tmp
sudo chown -R jingwei:jingwei /opt/jingwei
sudo apt install openjdk-17-jre nginx postgresql redis
```

If PostgreSQL 18 is required, install it from the official PostgreSQL APT repository instead of relying on the distribution default package.

## 3. Prepare Database

Create the application database and user before starting the service:

```sql
CREATE USER jingwei WITH PASSWORD 'replace_with_strong_password';
CREATE DATABASE jingwei OWNER jingwei;
```

## 4. Configure Environment

```bash
sudo cp deploy/.env.example /opt/jingwei/.env
sudo vim /opt/jingwei/.env
sudo chown root:jingwei /opt/jingwei/.env
sudo chmod 640 /opt/jingwei/.env
```

Required values:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `JWT_SECRET`

## 5. Deploy And Start

```bash
sudo bash deploy/deploy.sh
sudo systemctl enable jingwei
sudo systemctl start jingwei
```

## 6. Verify

```bash
sudo systemctl status jingwei
sudo journalctl -u jingwei -f
curl http://localhost/api/actuator/health
```

If the health check fails after deploying new migrations, inspect the journal first:

```bash
sudo journalctl -u jingwei -n 200 --no-pager
```
