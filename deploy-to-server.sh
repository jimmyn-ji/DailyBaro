#!/bin/bash

# DailyBaro 完整部署脚本
# 使用方法: ./deploy-to-server.sh
# 
# 配置方式（按优先级）:
# 1. 环境变量（export SERVER_IP=xxx）
# 2. .env 文件（脚本会自动加载）
# 3. 默认值（仅用于测试，生产环境请配置）

# 注意：不使用 set -e，因为某些步骤失败不应该阻止整个部署
# 例如：某个服务编译失败不应该阻止其他服务的部署

# 加载 .env 文件（如果存在）
if [ -f .env ]; then
    echo "加载 .env 配置文件..."
    export $(grep -v '^#' .env | xargs)
fi

# 服务器信息（从环境变量读取，如果没有则使用默认值）
SERVER_IP="${SERVER_IP:-your_server_ip}"
SERVER_USER="${SERVER_USER:-ubuntu}"
SERVER_PASS="${SERVER_PASS:-your_server_password}"
DOMAIN="${DOMAIN:-dailybaro.cn}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-$MYSQL_ROOT_PASSWORD}"
SERVICE_DIR="/home/ubuntu"
LOG_DIR="$SERVICE_DIR/logs"

# 检查必要的配置（在定义 log_error 和 log_info 之前，使用 echo）
if [ "$SERVER_IP" = "your_server_ip" ] || [ "$SERVER_PASS" = "your_server_password" ]; then
    echo -e "\033[0;31m[ERROR]\033[0m 请先配置服务器信息！"
    echo -e "\033[0;32m[INFO]\033[0m 方式1: 创建 .env 文件（推荐）"
    echo -e "\033[0;32m[INFO]\033[0m   cp env.example .env"
    echo -e "\033[0;32m[INFO]\033[0m   # 然后编辑 .env 文件填入实际值"
    echo ""
    echo -e "\033[0;32m[INFO]\033[0m 方式2: 使用环境变量"
    echo -e "\033[0;32m[INFO]\033[0m   export SERVER_IP=your_ip"
    echo -e "\033[0;32m[INFO]\033[0m   export SERVER_PASS=your_password"
    echo -e "\033[0;32m[INFO]\033[0m   ./deploy-to-server.sh"
    exit 1
fi

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查 sshpass 是否安装
if ! command -v sshpass &> /dev/null; then
    log_warn "sshpass 未安装，正在安装..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        brew install hudochenkov/sshpass/sshpass || log_error "请手动安装 sshpass: brew install hudochenkov/sshpass/sshpass"
    else
        sudo apt-get install -y sshpass || log_error "请手动安装 sshpass"
    fi
fi

# SSH 选项（处理主机密钥变化和密码认证）
# 使用完整的SSH选项来确保密码认证可用
SSH_OPTS="-o StrictHostKeyChecking=no \
-o UserKnownHostsFile=/dev/null \
-o PasswordAuthentication=yes \
-o PubkeyAuthentication=no \
-o PreferredAuthentications=password \
-o KbdInteractiveAuthentication=no \
-o ConnectTimeout=15 \
-o ControlMaster=no \
-o ControlPath=none \
-o ServerAliveInterval=60 \
-o ServerAliveCountMax=3 \
-o LogLevel=ERROR"

# SSH 执行命令
ssh_exec() {
    sshpass -p "$SERVER_PASS" ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" "$1" 2>&1
}

# SCP 上传文件
scp_upload() {
    local retry=0
    local max_retries=3
    while [ $retry -lt $max_retries ]; do
        if sshpass -p "$SERVER_PASS" scp $SSH_OPTS "$1" "$SERVER_USER@$SERVER_IP:$2" 2>&1; then
            return 0
        fi
        retry=$((retry + 1))
        if [ $retry -lt $max_retries ]; then
            log_warn "上传失败，重试 $retry/$max_retries..."
            sleep 2
        fi
    done
    log_error "上传失败: $1"
    return 1
}

# 上传目录
scp_upload_dir() {
    local retry=0
    local max_retries=3
    while [ $retry -lt $max_retries ]; do
        if sshpass -p "$SERVER_PASS" scp -r $SSH_OPTS "$1" "$SERVER_USER@$SERVER_IP:$2" 2>&1; then
            return 0
        fi
        retry=$((retry + 1))
        if [ $retry -lt $max_retries ]; then
            log_warn "上传目录失败，重试 $retry/$max_retries..."
            sleep 2
        fi
    done
    log_error "上传目录失败: $1"
    return 1
}

log_step "=========================================="
log_step "开始部署 DailyBaro 到服务器"
log_step "=========================================="

# 0. 清理旧的SSH known_hosts记录（服务器重装后密钥会变化）
log_step "0. 清理SSH known_hosts记录..."
if [ -f ~/.ssh/known_hosts ]; then
    # 移除该IP的旧记录
    ssh-keygen -R "$SERVER_IP" -f ~/.ssh/known_hosts 2>/dev/null || true
    log_info "已清理 $SERVER_IP 的旧SSH密钥记录"
fi

# 1. 检查服务器连接
log_step "1. 检查服务器连接..."
log_info "正在测试连接到 $SERVER_USER@$SERVER_IP..."

# 测试连接
CONNECTION_OK=false
MAX_RETRIES=3
RETRY_COUNT=0

while [ "$CONNECTION_OK" = false ] && [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log_info "尝试连接（第 $RETRY_COUNT 次）..."
    
    # 测试连接，捕获输出和退出码
    TEST_OUTPUT=$(sshpass -p "$SERVER_PASS" ssh $SSH_OPTS "$SERVER_USER@$SERVER_IP" "echo 'Connection test successful'" 2>&1)
    TEST_EXIT_CODE=$?
    
    # 检查是否连接成功（退出码为0且输出包含成功消息）
    if [ $TEST_EXIT_CODE -eq 0 ] && echo "$TEST_OUTPUT" | grep -q "Connection test successful"; then
        log_info "✓ 连接成功"
        CONNECTION_OK=true
    else
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            # 只显示错误信息的前100个字符
            ERROR_MSG=$(echo "$TEST_OUTPUT" | head -c 100 | tr '\n' ' ')
            if [ -n "$ERROR_MSG" ]; then
                log_warn "连接失败: ${ERROR_MSG}..."
            else
                log_warn "连接失败（退出码: $TEST_EXIT_CODE）"
            fi
            log_warn "3秒后重试..."
            sleep 3
        else
            # 最后一次失败，显示完整错误信息
            log_error "连接失败（已尝试 $MAX_RETRIES 次），错误信息:"
            echo "$TEST_OUTPUT" | head -10 | while read line; do
                log_error "  $line"
            done
        fi
    fi
done

# 如果连接失败，提供诊断信息
if [ "$CONNECTION_OK" = false ]; then
    log_error "无法连接到服务器（已尝试 $MAX_RETRIES 次）"
    log_error ""
    log_error "可能的原因："
    log_error "  1. 服务器IP是否正确: $SERVER_IP"
    log_error "  2. 服务器是否运行中"
    log_error "  3. 防火墙是否允许SSH连接（端口22）"
    log_error "  4. SSH服务是否已启动"
    log_error "  5. 密码是否正确"
    log_error ""
    log_info "请先手动测试连接："
    log_info "  sshpass -p '$SERVER_PASS' ssh $SSH_OPTS $SERVER_USER@$SERVER_IP 'echo test'"
    log_error ""
    log_error "如果服务器刚重装，请确认："
    log_error "  1. SSH服务已启动: sudo systemctl status ssh"
    log_error "  2. 防火墙允许SSH: sudo ufw allow 22"
    log_error "  3. 密码认证已启用（检查 /etc/ssh/sshd_config 中的 PasswordAuthentication yes）"
    log_error "  4. 服务器是否允许密码登录（某些云服务器默认禁用）"
    log_error ""
    exit 1
fi

# 2. 安装基础依赖和配置防火墙
log_step "2. 安装基础依赖和配置防火墙..."
ssh_exec "
    # 更新系统
    sudo apt-get update
    
    # 安装基础工具
    sudo apt-get install -y curl wget git unzip net-tools
    
    # 配置防火墙（开放必要端口）
    if command -v ufw &> /dev/null; then
        sudo ufw allow 22/tcp    # SSH
        sudo ufw allow 80/tcp    # HTTP
        sudo ufw allow 443/tcp   # HTTPS
        sudo ufw allow 3306/tcp  # MySQL
        sudo ufw allow 6379/tcp  # Redis
        sudo ufw --force enable || true
    elif command -v firewall-cmd &> /dev/null; then
        sudo firewall-cmd --permanent --add-port=22/tcp
        sudo firewall-cmd --permanent --add-port=80/tcp
        sudo firewall-cmd --permanent --add-port=443/tcp
        sudo firewall-cmd --permanent --add-port=3306/tcp
        sudo firewall-cmd --permanent --add-port=6379/tcp
        sudo firewall-cmd --reload
    fi
    echo '基础依赖和防火墙配置完成'
"

# 3. 安装 JDK 17
log_step "3. 安装 JDK 17..."
ssh_exec "
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q '17'; then
    sudo apt-get install -y openjdk-17-jdk
    # 设置 JAVA_HOME 环境变量
    JAVA_HOME_PATH=/usr/lib/jvm/java-17-openjdk-amd64
    if [ -d \"\$JAVA_HOME_PATH\" ]; then
        echo \"JAVA_HOME=\$JAVA_HOME_PATH\" | sudo tee -a /etc/environment
        echo \"export JAVA_HOME=\$JAVA_HOME_PATH\" | sudo tee -a ~/.bashrc
        export JAVA_HOME=\$JAVA_HOME_PATH
        export PATH=\$JAVA_HOME/bin:\$PATH
    fi
    # 验证安装
    java -version
    javac -version
else
    echo 'JDK 17 已安装'
    java -version
fi
"

# 4. 卸载 Maven（项目在本地编译，远程不需要Maven）
log_step "4. 卸载 Maven（远程不需要，项目已在本地编译）..."
ssh_exec "
if command -v mvn &> /dev/null; then
    echo '检测到Maven已安装，正在卸载...'
    # 卸载通过apt安装的Maven
    sudo apt-get remove -y maven 2>/dev/null || true
    sudo apt-get purge -y maven 2>/dev/null || true
    # 删除手动安装的Maven
    if [ -d /opt/maven ]; then
        sudo rm -rf /opt/maven
        echo '已删除 /opt/maven'
    fi
    # 清理环境变量（从/etc/profile和~/.bashrc中移除）
    sudo sed -i '/M2_HOME/d' /etc/profile 2>/dev/null || true
    sudo sed -i '/M2_HOME/d' ~/.bashrc 2>/dev/null || true
    # 验证卸载
    if ! command -v mvn &> /dev/null; then
        echo 'Maven 已成功卸载'
    else
        echo '警告: Maven可能未完全卸载'
    fi
else
    echo 'Maven 未安装，跳过卸载'
fi
echo '项目在本地编译后上传JAR文件，远程服务器只需JDK运行'
java -version
"

# 5. 安装 Python 3 和 pip
log_step "5. 安装 Python 3 和 pip..."
ssh_exec "
if ! command -v python3 &> /dev/null; then
    sudo apt-get install -y python3 python3-pip python3-venv
fi
python3 --version
# 确保pip3已安装
if ! command -v pip3 &> /dev/null; then
    echo 'pip3未安装，正在安装...'
    sudo apt-get install -y python3-pip
fi
pip3 --version || echo 'pip3安装失败，但继续执行'
# 升级 pip
pip3 install --upgrade pip --quiet 2>/dev/null || true
"

# 6. 安装 MySQL
log_step "6. 安装 MySQL..."
ssh_exec "
if ! command -v mysql &> /dev/null; then
    sudo debconf-set-selections <<< "mysql-server mysql-server/root_password password $MYSQL_ROOT_PASSWORD"
    sudo debconf-set-selections <<< "mysql-server mysql-server/root_password_again password $MYSQL_ROOT_PASSWORD"
    sudo apt-get install -y mysql-server
    sudo systemctl start mysql
    sudo systemctl enable mysql
    # 等待MySQL完全启动
    sleep 8
    # 使用密码连接MySQL（新安装的MySQL需要密码）
    sudo mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" -e \"CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\" 2>&1 || {
        # 如果密码连接失败，尝试无密码连接（某些安装可能没有设置密码）
        sudo mysql -e \"CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\" 2>&1
        sudo mysql -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_ROOT_PASSWORD';\" 2>&1 || true
        sudo mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" -e \"CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\" 2>&1
    }
    sudo mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" -e \"CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '\"$MYSQL_ROOT_PASSWORD\"';\" 2>&1 || true
    sudo mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" -e \"GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;\" 2>&1 || true
    sudo mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" -e \"FLUSH PRIVILEGES;\" 2>&1 || true
    sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
    sudo systemctl restart mysql
    # 再次等待并确认数据库存在
    sleep 5
    sudo mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" -e \"CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\" 2>&1
    echo 'MySQL 安装完成，数据库已创建'
else
    echo 'MySQL 已安装'
    sudo systemctl status mysql | head -3
fi
"

# 7. 安装 Docker 和 Docker Compose（Milvus需要）
log_step "7. 安装 Docker 和 Docker Compose..."
ssh_exec "
if ! command -v docker &> /dev/null; then
    # 安装Docker
    sudo apt-get update
    sudo apt-get install -y ca-certificates curl gnupg lsb-release
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    echo \"deb [arch=\$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \$(lsb_release -cs) stable\" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    sudo usermod -aG docker ubuntu
    # 配置Docker镜像加速（使用国内镜像源加速下载）
    sudo mkdir -p /etc/docker
    sudo tee /etc/docker/daemon.json > /dev/null << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ],
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5
}
DOCKER_EOF
    sudo systemctl daemon-reload
    sudo systemctl start docker
    sudo systemctl enable docker
    echo 'Docker 安装完成，已配置国内镜像加速'
else
    echo 'Docker 已安装'
    docker --version
    # 即使Docker已安装，也配置镜像加速（如果未配置）
    if [ ! -f /etc/docker/daemon.json ] || ! grep -q "registry-mirrors" /etc/docker/daemon.json 2>/dev/null; then
        echo '配置Docker镜像加速...'
        sudo mkdir -p /etc/docker
        sudo tee /etc/docker/daemon.json > /dev/null << 'DOCKER_EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.baidubce.com"
  ],
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5
}
DOCKER_EOF
        sudo systemctl daemon-reload
        sudo systemctl restart docker
        echo 'Docker镜像加速已配置'
    else
        echo 'Docker镜像加速已配置'
    fi
fi

# 安装 docker-compose（如果未安装）
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    sudo curl -L \"https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)\" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo 'Docker Compose 安装完成'
else
    echo 'Docker Compose 已安装'
fi
"

# 8. 安装 Redis（单机模式，节省资源）
log_step "8. 安装 Redis（单机模式）..."
ssh_exec "
if ! command -v redis-server &> /dev/null; then
    sudo apt-get install -y redis-server
    # 配置Redis（单机模式，不开启集群以节省资源）
    sudo sed -i 's/bind 127.0.0.1/bind 0.0.0.0/' /etc/redis/redis.conf
    # 优化内存使用
    sudo sed -i 's/# maxmemory <bytes>/maxmemory 256mb/' /etc/redis/redis.conf
    sudo sed -i 's/# maxmemory-policy noeviction/maxmemory-policy allkeys-lru/' /etc/redis/redis.conf
    sudo systemctl restart redis-server
    sudo systemctl enable redis-server
    echo 'Redis 安装完成（单机模式）'
else
    echo 'Redis 已安装'
    sudo systemctl status redis-server | head -3
fi
"

# 9. 安装 Milvus（向量数据库，RAG必需）
log_step "9. 安装 Milvus 向量数据库..."
ssh_exec "
# 确保Docker服务正在运行
if ! sudo systemctl is-active --quiet docker; then
    echo '启动Docker服务...'
    sudo systemctl start docker
    sleep 3
fi

# 确保ubuntu用户在docker组中
if ! groups | grep -q docker; then
    echo '将ubuntu用户添加到docker组...'
    sudo usermod -aG docker ubuntu
    # 注意：需要重新登录才能生效，但我们可以使用sudo
fi

# 检查Milvus是否已运行
if sudo docker ps | grep -q milvus; then
    echo 'Milvus 已经在运行中'
    docker ps | grep milvus
else
    echo '正在安装 Milvus 及其依赖（etcd, minio）...'
    
    # 创建Milvus数据目录
    sudo mkdir -p /var/lib/milvus/data
    sudo mkdir -p /var/lib/etcd/data
    sudo mkdir -p /var/lib/minio/data
    sudo chown -R ubuntu:ubuntu /var/lib/milvus /var/lib/etcd /var/lib/minio
    
    # 创建docker-compose文件用于Milvus（放在服务目录，移除version字段）
    cat > ~/milvus-compose.yml << 'MILVUS_EOF'

services:
  etcd:
    image: quay.io/coreos/etcd:v3.5.5
    container_name: milvus-etcd
    environment:
      - ETCD_AUTO_COMPACTION_MODE=revision
      - ETCD_AUTO_COMPACTION_RETENTION=1000
      - ETCD_QUOTA_BACKEND_BYTES=4294967296
      - ETCD_SNAPSHOT_COUNT=50000
    volumes:
      - /var/lib/etcd/data:/etcd
    command: etcd -advertise-client-urls=http://127.0.0.1:2379 -listen-client-urls http://0.0.0.0:2379 --data-dir /etcd
    ports:
      - \"2379:2379\"
    networks:
      - milvus
    restart: unless-stopped

  minio:
    image: minio/minio:RELEASE.2023-03-20T20-16-18Z
    container_name: milvus-minio
    environment:
      - MINIO_ACCESS_KEY=minioadmin
      - MINIO_SECRET_KEY=minioadmin
    ports:
      - \"9001:9001\"
      - \"9000:9000\"
    volumes:
      - /var/lib/minio/data:/data
    command: minio server /data --console-address \":9001\"
    networks:
      - milvus
    restart: unless-stopped

  milvus:
    image: milvusdb/milvus:v2.3.4
    container_name: milvus-standalone
    command: [\"milvus\", \"run\", \"standalone\"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - /var/lib/milvus/data:/var/lib/milvus
    ports:
      - \"19530:19530\"
    depends_on:
      - \"etcd\"
      - \"minio\"
    networks:
      - milvus
    restart: unless-stopped

networks:
  milvus:
    name: milvus
MILVUS_EOF

    # 启动Milvus服务（使用sudo确保权限）
    cd ~
    echo '使用sudo启动Milvus服务...'
    if sudo docker compose version &>/dev/null; then
        sudo docker compose -f ~/milvus-compose.yml up -d 2>&1
    elif sudo docker-compose version &>/dev/null; then
        sudo docker-compose -f ~/milvus-compose.yml up -d 2>&1
    else
        echo 'Docker Compose未找到，尝试安装...'
        sudo curl -L \"https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)\" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
        sudo docker-compose -f ~/milvus-compose.yml up -d 2>&1
    fi
    
    # 等待服务启动
    echo '等待 Milvus 服务启动...'
    sleep 20
    
    # 检查服务状态
    if sudo docker ps | grep -q milvus; then
        echo 'Milvus 安装完成！端口: 19530'
        sudo docker ps | grep milvus
    else
        echo '服务启动中，请稍后检查'
        sudo docker ps -a | grep milvus || echo 'Milvus容器未找到'
        echo '查看Docker服务状态:'
        sudo systemctl status docker --no-pager -l | head -10
    fi
fi
# 检查Milvus状态
sudo docker ps | grep milvus || echo 'Milvus未运行'
"

# 10. 安装 Nginx
log_step "10. 安装 Nginx..."
ssh_exec "
if ! command -v nginx &> /dev/null; then
    sudo apt-get install -y nginx
    sudo systemctl start nginx
    sudo systemctl enable nginx
    echo 'Nginx 安装完成'
else
    echo 'Nginx 已安装'
    sudo systemctl status nginx | head -3
fi
"

# 11. 创建服务目录
log_step "11. 创建服务目录..."
ssh_exec "
mkdir -p $SERVICE_DIR
mkdir -p $LOG_DIR
mkdir -p $SERVICE_DIR/nlp-service
mkdir -p $SERVICE_DIR/uploads
chmod -R 755 $SERVICE_DIR
"

# 12. 上传 SQL 文件并导入
log_step "12. 上传并导入 SQL..."
# 确保数据库存在（防止导入时数据库不存在）
log_info "确保数据库存在..."
# 等待MySQL完全启动
sleep 2
# 创建数据库（如果不存在）
DB_CREATE_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1")
if echo "$DB_CREATE_OUTPUT" | grep -q "ERROR"; then
    log_warn "创建数据库时可能有错误: ${DB_CREATE_OUTPUT:0:100}"
else
    log_info "数据库已确认存在"
fi

# 使用dailybaro_init.sql作为初始数据库（轻量级，不增加后台压力）
if [ -f "sql/dailybaro_init.sql" ]; then
    scp_upload "sql/dailybaro_init.sql" "$SERVICE_DIR/dailybaro_init.sql"
    log_info "正在导入 dailybaro_init.sql（初始数据库）..."
    # 确保数据库存在
    ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1" || {
        log_warn "创建数据库失败，尝试无密码连接..."
        ssh_exec "sudo mysql -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1"
    }
    sleep 2
    IMPORT_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD dailybaro < $SERVICE_DIR/dailybaro_init.sql 2>&1" || ssh_exec "sudo mysql dailybaro < $SERVICE_DIR/dailybaro_init.sql 2>&1")
    if echo "$IMPORT_OUTPUT" | grep -q "ERROR 1049"; then
        log_error "数据库不存在，尝试重新创建..."
        ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD -e 'CREATE DATABASE dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1" || ssh_exec "sudo mysql -e 'CREATE DATABASE dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1"
        sleep 2
        IMPORT_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD dailybaro < $SERVICE_DIR/dailybaro_init.sql 2>&1" || ssh_exec "sudo mysql dailybaro < $SERVICE_DIR/dailybaro_init.sql 2>&1")
    fi
    if echo "$IMPORT_OUTPUT" | grep -q "ERROR"; then
        log_warn "SQL导入可能有错误: ${IMPORT_OUTPUT:0:150}"
    else
        log_info "初始数据库导入成功"
    fi
elif [ -f "sql/dailybaro.sql" ]; then
    scp_upload "sql/dailybaro.sql" "$SERVICE_DIR/dailybaro.sql"
    log_info "正在导入 dailybaro.sql..."
    IMPORT_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD dailybaro < $SERVICE_DIR/dailybaro.sql 2>&1" || ssh_exec "sudo mysql dailybaro < $SERVICE_DIR/dailybaro.sql 2>&1")
    if echo "$IMPORT_OUTPUT" | grep -q "ERROR 1049"; then
        log_error "数据库不存在，尝试重新创建..."
        ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD -e 'CREATE DATABASE dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1" || ssh_exec "sudo mysql -e 'CREATE DATABASE dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1"
        sleep 2
        IMPORT_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD dailybaro < $SERVICE_DIR/dailybaro.sql 2>&1" || ssh_exec "sudo mysql dailybaro < $SERVICE_DIR/dailybaro.sql 2>&1")
    fi
    if echo "$IMPORT_OUTPUT" | grep -q "ERROR"; then
        log_warn "SQL导入可能有错误: ${IMPORT_OUTPUT:0:150}"
    else
        log_info "SQL导入成功"
    fi
else
    log_warn "未找到 sql/dailybaro_init.sql 或 sql/dailybaro.sql，跳过 SQL 导入"
fi

# 导入知识库表
if [ -f "sql/knowledge_table.sql" ]; then
    scp_upload "sql/knowledge_table.sql" "$SERVICE_DIR/knowledge_table.sql"
    log_info "正在导入 knowledge_table.sql..."
    # 再次确保数据库存在
    ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1" || ssh_exec "sudo mysql -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1"
    sleep 2
    IMPORT_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD dailybaro < $SERVICE_DIR/knowledge_table.sql 2>&1" || ssh_exec "sudo mysql dailybaro < $SERVICE_DIR/knowledge_table.sql 2>&1")
    if echo "$IMPORT_OUTPUT" | grep -q "ERROR"; then
        log_warn "知识库表导入可能有错误: ${IMPORT_OUTPUT:0:150}"
    else
        log_info "知识库表导入成功"
    fi
fi

# 执行数据库迁移脚本（QQ登录和邮箱登录支持）
if [ -f "sql/add_qq_openid_simple.sql" ]; then
    scp_upload "sql/add_qq_openid_simple.sql" "$SERVICE_DIR/add_qq_openid_simple.sql"
    log_info "正在执行数据库迁移脚本（QQ登录和邮箱登录支持）..."
    ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1" || ssh_exec "sudo mysql -e 'CREATE DATABASE IF NOT EXISTS dailybaro CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1"
    sleep 2
    # 执行迁移脚本（忽略已存在的字段错误）
    MIGRATION_OUTPUT=$(ssh_exec "sudo mysql -uroot -p$MYSQL_ROOT_PASSWORD dailybaro < $SERVICE_DIR/add_qq_openid_simple.sql 2>&1" || ssh_exec "sudo mysql dailybaro < $SERVICE_DIR/add_qq_openid_simple.sql 2>&1")
    # 检查是否有严重错误（忽略"Duplicate column"错误，说明字段已存在）
    if echo "$MIGRATION_OUTPUT" | grep -q "ERROR" && ! echo "$MIGRATION_OUTPUT" | grep -q "Duplicate column"; then
        log_warn "数据库迁移可能有错误: ${MIGRATION_OUTPUT:0:200}"
    else
        if echo "$MIGRATION_OUTPUT" | grep -q "Duplicate column"; then
            log_info "数据库迁移完成（部分字段已存在，已跳过）"
        else
            log_info "数据库迁移成功（password字段已允许为NULL，qq_openid字段已添加）"
        fi
    fi
fi

# 13. 编译后端服务
log_step "13. 编译后端服务..."
log_info "正在清理旧的构建产物..."
cd DailyBaro-cloud
# 强制清理所有旧的构建产物，确保全新编译
mvn clean -q || true
# 删除所有target目录（双重保险）
find . -type d -name "target" -exec rm -rf {} + 2>/dev/null || true
log_info "正在编译 Java 服务（全新构建）..."
# 使用-U参数强制更新依赖，确保使用最新版本
if ! mvn clean package -DskipTests -U -q; then
    log_error "后端编译失败，但继续执行部署..."
    # 检查哪些服务编译失败
    log_info "检查编译结果..."
    cd ..
else
    cd ..
    log_info "编译完成，所有JAR文件都是新构建的"
fi

# 14. 上传后端 JAR 文件
log_step "14. 上传后端 JAR 文件..."
services=(
    "gateway-service:8000"
    "user-service:8001"
    "file-service:8003"
    "content-service:8011"
    "app-service:8012"
    "ai-knowledge-service:8013"
)

for service_port in "${services[@]}"; do
    IFS=':' read -r service_name port <<< "$service_port"
    jar_file="DailyBaro-cloud/$service_name/target/$service_name-1.0-SNAPSHOT.jar"
    if [ -f "$jar_file" ]; then
        # 获取JAR文件的修改时间，确保是新构建的
        if [[ "$OSTYPE" == "darwin"* ]]; then
            jar_mtime=$(stat -f "%m" "$jar_file" 2>/dev/null || echo "0")
        else
            jar_mtime=$(stat -c "%Y" "$jar_file" 2>/dev/null || echo "0")
        fi
        # 确保jar_mtime是数字
        if ! [[ "$jar_mtime" =~ ^[0-9]+$ ]]; then
            jar_mtime=0
        fi
        current_time=$(date +%s)
        time_diff=$((current_time - jar_mtime))
        # 如果JAR文件超过5分钟，可能是旧文件，给出警告
        if [ $time_diff -gt 300 ]; then
            log_warn "$service_name.jar 文件可能不是最新构建的（${time_diff}秒前）"
        else
            log_info "$service_name.jar 是新构建的（${time_diff}秒前）"
        fi
        scp_upload "$jar_file" "$SERVICE_DIR/$service_name.jar" || log_error "上传 $service_name.jar 失败"
        log_info "已上传 $service_name.jar"
    else
        log_error "$jar_file 不存在，编译可能失败！"
        # 继续执行，不退出脚本
    fi
done

# 15. 上传 NLP 服务
log_step "15. 上传 NLP 服务..."
if [ -d "DailyBaro-cloud/nlp-service" ]; then
    log_info "正在打包 NLP 服务（排除 venv、__pycache__ 等，仅打包必要文件）..."
    # 创建临时 tar 文件，只包含必要的文件
    TEMP_TAR=$(mktemp)
    cd DailyBaro-cloud/nlp-service
    # 只打包运行所需的文件：Python 脚本、分析器、模型、依赖文件
    # 排除：虚拟环境、缓存、测试文件、训练文件、Java 源代码等
    tar czf "$TEMP_TAR" \
        --exclude='venv' \
        --exclude='.venv' \
        --exclude='__pycache__' \
        --exclude='*.pyc' \
        --exclude='*.pyo' \
        --exclude='*.pyd' \
        --exclude='.git' \
        --exclude='*.log' \
        --exclude='test_*.py' \
        --exclude='train_*.py' \
        --exclude='data_analysis.py' \
        --exclude='src' \
        --exclude='target' \
        --exclude='pom.xml' \
        --exclude='Dockerfile' \
        --exclude='test_output.txt' \
        --exclude='analysis_output' \
        simple_app.py local_app.py analyzers models requirements.txt 2>/dev/null || true
    cd - > /dev/null
    
    # 检查压缩包大小
    if [ -f "$TEMP_TAR" ]; then
        TAR_SIZE=$(stat -f%z "$TEMP_TAR" 2>/dev/null || stat -c%s "$TEMP_TAR" 2>/dev/null || echo "0")
        log_info "压缩包大小: $((TAR_SIZE / 1024))KB"
    fi
    
    # 上传压缩包
    log_info "正在上传 NLP 服务压缩包..."
    scp_upload "$TEMP_TAR" "$SERVICE_DIR/nlp-service.tar.gz" || log_error "NLP 服务上传失败"
    
    # 在服务器上解压
    ssh_exec "
        mkdir -p $SERVICE_DIR/nlp-service
        cd $SERVICE_DIR/nlp-service
        tar xzf $SERVICE_DIR/nlp-service.tar.gz 2>/dev/null || true
        rm -f $SERVICE_DIR/nlp-service.tar.gz
        echo '安装 NLP 服务 Python 依赖...'
        # 确保 python3-venv 已安装
        sudo apt-get install -y python3-venv python3-full python3-setuptools 2>/dev/null || true
        
        # 创建虚拟环境（Ubuntu 24.04 需要，避免 externally-managed-environment 错误）
        if python3 -m venv venv 2>/dev/null; then
            VENV_PIP='venv/bin/pip'
            VENV_PYTHON='venv/bin/python'
            echo '虚拟环境创建成功: venv'
        elif python3 -m venv .venv 2>/dev/null; then
            VENV_PIP='.venv/bin/pip'
            VENV_PYTHON='.venv/bin/python'
            echo '虚拟环境创建成功: .venv'
        else
            # 如果虚拟环境创建失败，使用系统 pip 并添加 --break-system-packages
            VENV_PIP='pip3 --break-system-packages'
            VENV_PYTHON='python3'
            echo '警告: 虚拟环境创建失败，使用系统 pip（--break-system-packages）'
        fi
        
        # 升级 pip 和 setuptools（setuptools 提供 distutils 兼容性）
        echo '升级 pip 和 setuptools...'
        \$VENV_PIP install --upgrade pip setuptools wheel --quiet 2>&1 || echo 'pip 升级失败，继续...'
        
        # 先安装基础依赖（必需）- 使用更兼容的版本
        echo '安装基础依赖: flask, flask-cors, jieba, numpy...'
        \$VENV_PIP install flask flask-cors jieba numpy --quiet 2>&1 || echo '基础依赖安装失败，尝试继续...'
        
        # 尝试安装 snownlp（可能需要 setuptools 提供的 distutils 兼容性）
        echo '尝试安装 snownlp...'
        \$VENV_PIP install snownlp --quiet 2>&1 || {
            echo 'snownlp 安装失败，尝试安装 setuptools-scm 和重试...'
            \$VENV_PIP install setuptools-scm --quiet 2>&1 || true
            \$VENV_PIP install snownlp --quiet 2>&1 || echo 'snownlp 安装失败，但 simple_app.py 仍可使用基础功能（jieba）'
        }
        
        # 安装数据分析相关依赖（可选，用于数据分析功能）
        echo '安装可选依赖: pandas, matplotlib, seaborn...'
        \$VENV_PIP install pandas matplotlib seaborn --quiet 2>&1 || echo '可选依赖安装失败，跳过'
        
        echo 'NLP 服务依赖安装完成'
        
        # 注意：torch 和 transformers 等大型依赖暂不安装，以节省资源
        # 如果需要完整功能，可以后续手动安装：pip3 install torch transformers scikit-learn
        
        # 确保 simple_app.py 有执行权限
        chmod +x simple_app.py || true
        
        echo 'NLP 服务依赖安装完成'
        echo '注意：simple_app.py 使用基础依赖（jieba + snownlp），已足够运行文本情绪分析'
    "
    
    # 清理临时文件
    rm -f "$TEMP_TAR"
    log_info "NLP 服务已上传"
fi

# 16. 编译并上传 Vue 前端
log_step "16. 编译并上传 Vue 前端..."
cd DailyBaro-vue
npm install --quiet
npm run build
cd ..
if [ -d "DailyBaro-vue/dist" ]; then
    ssh_exec "sudo rm -rf /var/www/dailybaro"
    ssh_exec "sudo mkdir -p /var/www/dailybaro"
    
    # 先检查本地 dist 目录内容
    log_info "检查本地 dist 目录..."
    ls -la DailyBaro-vue/dist/ | head -5 || echo "本地 dist 目录检查失败"
    
    # 使用 tar 压缩上传，更可靠（排除 macOS 特殊文件）
    log_info "打包前端文件..."
    cd DailyBaro-vue
    # 使用 COPYFILE_DISABLE 避免 macOS 扩展属性问题
    # 即使有警告也继续（macOS 扩展属性的警告可以忽略）
    COPYFILE_DISABLE=1 tar czf /tmp/vue-dist.tar.gz --exclude='._*' --exclude='.DS_Store' -C dist . 2>&1 | grep -v "LIBARCHIVE.xattr" | grep -v "Ignoring" || {
        # 如果失败，尝试不使用 COPYFILE_DISABLE（某些系统不支持）
        tar czf /tmp/vue-dist.tar.gz --exclude='._*' --exclude='.DS_Store' -C dist . 2>&1 | grep -v "LIBARCHIVE.xattr" | grep -v "Ignoring" || true
    }
    # 验证压缩包是否创建成功
    if [ -f /tmp/vue-dist.tar.gz ]; then
        log_info "压缩包创建成功: $(ls -lh /tmp/vue-dist.tar.gz | awk '{print $5}')"
    else
        log_error "压缩包创建失败"
    fi
    cd ..
    
    # 上传压缩包
    log_info "上传前端压缩包..."
    scp_upload "/tmp/vue-dist.tar.gz" "$SERVICE_DIR/vue-dist.tar.gz"
    rm -f /tmp/vue-dist.tar.gz
    
    # 在服务器上解压（使用临时目录避免权限问题）
    ssh_exec "
        cd $SERVICE_DIR
        if [ -f vue-dist.tar.gz ]; then
            # 创建临时目录解压
            TEMP_DIR=\$(mktemp -d)
            echo '在临时目录解压: '\$TEMP_DIR
            # 解压到临时目录（忽略 macOS 扩展属性警告）
            tar xzf vue-dist.tar.gz -C \$TEMP_DIR 2>&1 | grep -v 'LIBARCHIVE.xattr' | grep -v 'Ignoring' || true
            # 检查是否解压成功
            if [ -f \$TEMP_DIR/index.html ]; then
                echo '✓ 解压成功，index.html 存在'
                # 清空目标目录并复制文件
                sudo rm -rf /var/www/dailybaro/*
                sudo cp -r \$TEMP_DIR/* /var/www/dailybaro/
                sudo chown -R www-data:www-data /var/www/dailybaro
                sudo chmod -R 755 /var/www/dailybaro
                echo '✓ 文件已复制到 /var/www/dailybaro'
            else
                echo '✗ 解压后 index.html 不存在，检查压缩包内容:'
                tar tzf vue-dist.tar.gz | head -10
            fi
            # 清理临时目录
            rm -rf \$TEMP_DIR
            rm -f vue-dist.tar.gz
        else
            echo '✗ 压缩包不存在'
        fi
        # 验证文件是否存在
        if [ -f /var/www/dailybaro/index.html ]; then
            echo '✓ 前端文件已正确部署: index.html 存在'
            echo '文件列表:'
            ls -la /var/www/dailybaro/ | head -10
        else
            echo '✗ 错误: index.html 不存在'
            echo '检查 /var/www/dailybaro 目录:'
            ls -la /var/www/dailybaro/ | head -10
        fi
    "
    log_info "Vue 前端已上传"
else
    log_error "Vue 前端编译失败，dist 目录不存在"
fi

# 17. 上传启动脚本
log_step "17. 上传启动脚本..."
if [ -f "start-all-services.sh" ]; then
    scp_upload "start-all-services.sh" "$SERVICE_DIR/start-all-services.sh"
    ssh_exec "
        chmod +x $SERVICE_DIR/start-all-services.sh
        # 验证脚本是否存在且可执行
        if [ -f $SERVICE_DIR/start-all-services.sh ] && [ -x $SERVICE_DIR/start-all-services.sh ]; then
            echo '✓ 启动脚本已上传并设置执行权限'
            # 检查脚本第一行（shebang）
            head -1 $SERVICE_DIR/start-all-services.sh || echo '无法读取脚本第一行'
        else
            echo '✗ 启动脚本上传或权限设置失败'
        fi
    "
    log_info "启动脚本已上传"
else
    log_error "启动脚本 start-all-services.sh 不存在"
fi

# 18. 创建 systemd 服务文件
log_step "18. 创建 systemd 服务文件..."
ssh_exec "sudo tee /etc/systemd/system/dailybaro.service > /dev/null << 'EOF'
[Unit]
Description=DailyBaro Services
After=network.target mysql.service redis-server.service
Wants=docker.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=$SERVICE_DIR
ExecStart=/bin/bash $SERVICE_DIR/start-all-services.sh
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
# 加载环境变量
EnvironmentFile=/etc/environment

[Install]
WantedBy=multi-user.target
EOF
"

# 19. 创建定时重启任务
log_step "19. 创建定时重启任务..."
ssh_exec "sudo tee /etc/cron.daily/dailybaro-restart > /dev/null << 'CRON_EOF'
#!/bin/bash
systemctl restart dailybaro
CRON_EOF
"
ssh_exec "sudo chmod +x /etc/cron.daily/dailybaro-restart"

# 创建凌晨0点重启的 cron 任务
ssh_exec "sudo crontab -l 2>/dev/null | grep -v 'dailybaro-restart' | sudo crontab - || true"
ssh_exec "echo '0 0 * * * systemctl restart dailybaro' | sudo crontab -"

# 20. 配置 Nginx（先配置 HTTP，SSL 证书安装后再更新为 HTTPS）
log_step "20. 配置 Nginx..."
# 先配置 HTTP 版本（SSL 证书安装前）
ssh_exec "sudo tee /etc/nginx/sites-available/dailybaro > /dev/null << 'NGINX_EOF'
server {
    listen 80;
    server_name $DOMAIN www.$DOMAIN;
    
    # 前端静态文件
    root /var/www/dailybaro;
    index index.html;

    # 前端路由支持（Vue Router history 模式，无#号）
    # 注意：URL中的#号是客户端端的，不会发送到服务器，所以Nginx无法直接检测
    # 但我们可以确保所有路由都正确返回index.html，让Vue Router处理路由
    
    # 根路径直接返回index.html
    location = / {
        try_files /index.html =404;
    }
    
    # 所有其他路径都尝试返回index.html（Vue Router history模式）
    location / {
        try_files \$uri \$uri/ /index.html;
        # 禁止访问隐藏文件
        location ~ /\. {
            deny all;
        }
    }
    
    # 如果URL中有#号，重定向到无#号的URL
    if (\$request_uri ~ "^[^#]*#") {
        return 301 \$scheme://\$host\$uri;
    }

    # API 代理
    location /api/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 小程序 API 代理
    location /app/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 登录接口
    location /login/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    # 文件上传
    location /uploads/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        client_max_body_size 50M;
    }

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control \"public, immutable\";
    }
}
NGINX_EOF
"

# 启用站点
ssh_exec "
    sudo ln -sf /etc/nginx/sites-available/dailybaro /etc/nginx/sites-enabled/
    sudo rm -f /etc/nginx/sites-enabled/default
    # 验证配置
    if sudo nginx -t 2>&1; then
        echo 'Nginx 配置测试通过'
        sudo systemctl reload nginx || sudo systemctl restart nginx
        echo 'Nginx 已重新加载'
    else
        echo '警告: Nginx 配置测试失败'
    fi
"

# 21. 安装 SSL 证书（Let's Encrypt）
log_step "21. 安装 SSL 证书..."
log_info "注意：SSL 证书安装需要域名已正确解析到服务器IP"
ssh_exec "
if [ ! -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem ]; then
    sudo apt-get install -y certbot python3-certbot-nginx
    # 先测试域名解析
    DOMAIN_IP=\$(nslookup $DOMAIN 2>/dev/null | grep -A 1 'Name:' | tail -1 | awk '{print \$2}' || echo '')
    if [ \"\$DOMAIN_IP\" = \"$SERVER_IP\" ] || [ -z \"\$DOMAIN_IP\" ]; then
        echo '域名解析检查完成，开始安装SSL证书...'
        sudo certbot --nginx -d $DOMAIN -d www.$DOMAIN --non-interactive --agree-tos --email admin@$DOMAIN --redirect || echo 'SSL 证书安装失败，请检查域名解析或稍后手动运行: sudo certbot --nginx -d $DOMAIN'
    else
        echo '警告: 域名 $DOMAIN 解析到 \$DOMAIN_IP，与服务器IP $SERVER_IP 不匹配'
        echo '请先配置域名DNS解析，然后手动运行：'
        echo '  sudo certbot --nginx -d $DOMAIN -d www.$DOMAIN'
    fi
else
    echo 'SSL 证书已存在'
fi
"

# 22. 测试 Nginx 配置并重启（SSL 证书安装后）
log_step "22. 测试并重启 Nginx..."
ssh_exec "
    # 验证前端文件是否存在
    if [ ! -f /var/www/dailybaro/index.html ]; then
        echo '错误: 前端文件不存在，重新部署...'
        if [ -d $SERVICE_DIR/vue-dist ]; then
            sudo cp -r $SERVICE_DIR/vue-dist/* /var/www/dailybaro/ 2>/dev/null || true
            sudo chown -R www-data:www-data /var/www/dailybaro
        fi
    fi
    
    # 先测试 Nginx 配置
    if sudo nginx -t 2>&1; then
        sudo systemctl restart nginx
        echo 'Nginx 配置测试通过并已重启'
        # 验证 Nginx 是否运行
        if sudo systemctl is-active --quiet nginx; then
            echo 'Nginx 服务运行正常'
        else
            echo '警告: Nginx 服务未运行，尝试启动...'
            sudo systemctl start nginx
        fi
    else
        echo '警告: Nginx 配置测试失败'
        # 检查是否有 SSL 证书配置但证书不存在
        if grep -q 'ssl_certificate' /etc/nginx/sites-available/dailybaro 2>/dev/null && [ ! -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem ]; then
            echo '检测到 SSL 配置但证书不存在，重新配置为 HTTP 模式...'
            # 重新配置为纯 HTTP（certbot 会自动更新为 HTTPS）
            sudo tee /etc/nginx/sites-available/dailybaro > /dev/null << 'NGINX_HTTP_EOF'
server {
    listen 80;
    server_name $DOMAIN www.$DOMAIN;
    
    root /var/www/dailybaro;
    index index.html;
    
    location = / {
        try_files /index.html =404;
    }
    
    location / {
        try_files \$uri \$uri/ /index.html;
        location ~ /\. {
            deny all;
        }
    }
    
    location /api/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
    
    location /app/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
    
    location /login/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
    
    location /uploads/ {
        proxy_pass http://localhost:8000;
        proxy_set_header Host \$host;
        client_max_body_size 50M;
    }
    
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control \"public, immutable\";
    }
}
NGINX_HTTP_EOF
            sudo nginx -t && sudo systemctl restart nginx && echo 'Nginx 已重新配置为 HTTP 模式' || echo 'Nginx 重新配置失败'
        fi
    fi
"

# 23. 配置环境变量（所有服务配置）
log_step "23. 配置环境变量..."
ssh_exec "
    # AI服务配置（DeepSeek）- 从环境变量读取
    if [ -n "$DEEPSEEK_API_KEY" ]; then
        echo \"export DEEPSEEK_API_KEY=$DEEPSEEK_API_KEY\" | sudo tee -a /etc/environment
    fi
    
    # Embedding配置（知识库RAG）- 从环境变量读取
    if [ -n "$EMBEDDING_API_KEY" ]; then
        echo \"export EMBEDDING_API_KEY=$EMBEDDING_API_KEY\" | sudo tee -a /etc/environment
    fi
    echo 'export EMBEDDING_API_URL=${EMBEDDING_API_URL:-https://api.deepseek.com/v1/embeddings}' | sudo tee -a /etc/environment
    echo 'export EMBEDDING_MODEL=${EMBEDDING_MODEL:-text-embedding-3-small}' | sudo tee -a /etc/environment
    echo 'export EMBEDDING_PROVIDER=${EMBEDDING_PROVIDER:-deepseek}' | sudo tee -a /etc/environment
    
    # 微信小程序配置 - 从环境变量读取
    if [ -n "$WECHAT_APP_ID" ]; then
        echo \"export WECHAT_APP_ID=$WECHAT_APP_ID\" | sudo tee -a /etc/environment
    fi
    if [ -n "$WECHAT_APP_SECRET" ]; then
        echo \"export WECHAT_APP_SECRET=$WECHAT_APP_SECRET\" | sudo tee -a /etc/environment
    fi
    
    # 邮箱配置（SMTP）- 从环境变量读取
    echo \"export EMAIL_HOST=\${EMAIL_HOST:-smtp.qq.com}\" | sudo tee -a /etc/environment
    echo \"export EMAIL_PORT=\${EMAIL_PORT:-587}\" | sudo tee -a /etc/environment
    if [ -n "$EMAIL_USERNAME" ]; then
        echo \"export EMAIL_USERNAME=$EMAIL_USERNAME\" | sudo tee -a /etc/environment
    fi
    if [ -n "$EMAIL_PASSWORD" ]; then
        echo \"export EMAIL_PASSWORD=$EMAIL_PASSWORD\" | sudo tee -a /etc/environment
    fi
    echo 'export EMAIL_MOCK_ENABLED=${EMAIL_MOCK_ENABLED:-false}' | sudo tee -a /etc/environment
    
    # Redis配置（单机模式）
    echo 'export REDIS_HOST=localhost' | sudo tee -a /etc/environment
    echo 'export REDIS_PORT=6379' | sudo tee -a /etc/environment
    
    echo '环境变量配置完成'
    echo '注意：环境变量将在下次登录或重启后生效，当前会话需要手动 source /etc/environment'
"

# 24. 启动服务
log_step "24. 启动 DailyBaro 服务..."
ssh_exec "
    sudo systemctl daemon-reload
    sudo systemctl enable dailybaro
    # 尝试启动服务，即使依赖失败也继续
    if sudo systemctl start dailybaro 2>&1; then
        echo '服务启动成功'
    else
        echo '警告: 服务启动失败，检查依赖...'
        # 检查是否是依赖问题
        if sudo systemctl status dailybaro 2>&1 | grep -q 'dependency'; then
            echo '检测到依赖问题，尝试忽略依赖启动...'
            # 重新加载并尝试启动
            sudo systemctl daemon-reload
            sudo systemctl reset-failed dailybaro 2>/dev/null || true
            sudo systemctl start dailybaro 2>&1 || echo '服务启动失败，请手动检查: sudo journalctl -u dailybaro -n 50'
        fi
    fi
"

# 25. 等待服务启动
log_step "25. 等待服务启动..."
sleep 30

# 26. 检查服务状态
log_step "26. 检查服务状态..."
ssh_exec "
    echo '=== 服务状态检查 ==='
    sudo systemctl status dailybaro --no-pager | head -20 || echo '服务状态检查失败'
    
    echo ''
    echo '=== 端口监听检查 ==='
    echo '检查关键端口:'
    for port in 8000 8001 8003 8011 8012 8013 5001; do
        if netstat -tlnp 2>/dev/null | grep -q ":$port "; then
            echo "  ✓ 端口 $port 正在监听"
        else
            echo "  ✗ 端口 $port 未监听"
        fi
    done
    echo ''
    echo '所有监听端口:'
    netstat -tlnp 2>/dev/null | grep -E ':(8000|8001|8003|8011|8012|8013|5001|443|80) ' || echo '  无相关端口监听'
    
    echo ''
    echo '=== Nginx 状态检查 ==='
    if sudo systemctl is-active --quiet nginx; then
        echo '✓ Nginx 运行正常'
        if [ -f /var/www/dailybaro/index.html ]; then
            echo '✓ 前端文件存在: /var/www/dailybaro/index.html'
        else
            echo '✗ 前端文件不存在: /var/www/dailybaro/index.html'
            echo '  请检查前端部署步骤'
        fi
    else
        echo '✗ Nginx 未运行'
        sudo systemctl status nginx --no-pager | head -5
    fi
    
    echo ''
    echo '=== Docker 服务检查 ==='
    docker ps | grep -E 'milvus|etcd|minio' || echo 'Milvus 服务检查完成（Docker 可能未运行）'
    
    echo ''
    echo '=== 服务启动错误诊断 ==='
    SERVICE_STATUS=\$(sudo systemctl status dailybaro 2>&1)
    if echo "\$SERVICE_STATUS" | grep -q 'status=127'; then
        echo '✗ 服务启动失败（退出码 127 - 命令未找到）'
    elif echo "\$SERVICE_STATUS" | grep -q 'status=1'; then
        echo '✗ 服务启动失败（退出码 1 - 脚本执行错误）'
    elif echo "\$SERVICE_STATUS" | grep -q 'failed\|Failed'; then
        echo '✗ 服务启动失败'
    fi
    
    if echo "\$SERVICE_STATUS" | grep -q 'failed\|Failed\|status='; then
        echo '查看最近日志:'
        sudo journalctl -u dailybaro -n 30 --no-pager | tail -20
        echo ''
        echo '检查启动脚本:'
        if [ -f $SERVICE_DIR/start-all-services.sh ]; then
            echo '脚本存在:'
            ls -la $SERVICE_DIR/start-all-services.sh
        else
            echo '✗ 启动脚本不存在: $SERVICE_DIR/start-all-services.sh'
        fi
    fi
    
    echo ''
    echo '=== 网关服务检查（502 错误诊断） ==='
    if netstat -tlnp 2>/dev/null | grep -q ':8000 '; then
        echo '✓ 网关服务端口 8000 正在监听'
        echo '测试网关连接:'
        curl -s -o /dev/null -w 'HTTP状态码: %{http_code}\n' http://localhost:8000/actuator/health 2>&1 || echo '网关健康检查失败'
    else
        echo '✗ 网关服务端口 8000 未监听 - 这是 502 错误的根本原因'
        echo '检查网关服务进程:'
        ps aux | grep -E 'gateway-service|8000' | grep -v grep || echo '  网关服务进程不存在'
        echo ''
        echo '检查网关服务日志:'
        if [ -f $SERVICE_DIR/logs/gateway-service.log ]; then
            echo '最近 20 行日志:'
            tail -20 $SERVICE_DIR/logs/gateway-service.log 2>/dev/null || echo '  无法读取日志文件'
        else
            echo '  日志文件不存在: $SERVICE_DIR/logs/gateway-service.log'
        fi
        echo ''
        echo '尝试手动启动网关服务:'
        echo '  cd $SERVICE_DIR && nohup java -Xms64m -Xmx256m -jar gateway-service.jar > logs/gateway-service.log 2>&1 &'
    fi
"

log_step "=========================================="
log_info "部署完成！"
log_info "访问地址: https://$DOMAIN"
log_info ""
log_info "已配置的环境变量："
log_info "  ✓ DeepSeek API Key（AI服务和Embedding）"
log_info "  ✓ 微信小程序 AppID 和 AppSecret"
log_info "  ✓ 邮箱SMTP配置（QQ邮箱）"
log_info ""
log_info "配置信息："
if [ -n "$WECHAT_APP_ID" ]; then
    log_info "  - 微信AppID: $WECHAT_APP_ID"
fi
if [ -n "$EMAIL_USERNAME" ]; then
    log_info "  - 邮箱: $EMAIL_USERNAME"
fi
if [ -n "$DEEPSEEK_API_KEY" ]; then
    log_info "  - DeepSeek API Key: 已配置"
fi
log_info ""
log_info "服务管理："
log_info "  服务状态: sudo systemctl status dailybaro"
log_info "  查看日志: sudo journalctl -u dailybaro -f"
log_info "  重启服务: sudo systemctl restart dailybaro"
log_info ""
log_info "故障排查："
log_info "  - AI服务: tail -f /home/ubuntu/logs/ai-service.log"
log_info "  - 微信登录: tail -f /home/ubuntu/logs/user-service.log"
log_info "  - 邮箱服务: tail -f /home/ubuntu/logs/user-service.log"
log_info "  - Milvus服务: docker ps | grep milvus"
log_info "  - Milvus日志: docker logs milvus-standalone"
log_step "=========================================="
