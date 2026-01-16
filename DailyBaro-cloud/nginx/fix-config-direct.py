#!/usr/bin/env python3
"""
直接在服务器上执行的配置修复脚本
"""

import subprocess
import sys
from datetime import datetime

def run_cmd(cmd, check=True):
    """执行命令"""
    print(f"执行: {cmd}")
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"❌ 错误: {result.stderr}")
        sys.exit(1)
    return result

# 配置内容
nginx_config = """server {
    server_name dailybaro.cn;

    root /home/ubuntu/web-dist;
    index index.html;

    listen 443 ssl; # managed by Certbot
    ssl_certificate /etc/letsencrypt/live/dailybaro.cn/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/dailybaro.cn/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8000/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /app/ {
        proxy_pass http://127.0.0.1:8000/app/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    if ($host = dailybaro.cn) {
        return 301 https://$host$request_uri;
    } # managed by Certbot

    listen 80;
    server_name dailybaro.cn;
    return 404; # managed by Certbot
}
"""

print("==========================================")
print("修复 Nginx HTTPS 配置")
print("==========================================")

# 备份
backup_file = f"/tmp/dailybaro.conf.backup.{datetime.now().strftime('%Y%m%d_%H%M%S')}"
run_cmd(f"sudo cp /etc/nginx/sites-enabled/dailybaro.conf {backup_file}")
print(f"✅ 已备份到: {backup_file}")

# 写入配置
print("\n📝 写入新配置...")
with open("/tmp/dailybaro.conf.new", "w") as f:
    f.write(nginx_config)

run_cmd("sudo cp /tmp/dailybaro.conf.new /etc/nginx/sites-enabled/dailybaro.conf")

# 测试配置
print("\n🧪 测试配置...")
result = run_cmd("sudo nginx -t", check=False)
if result.returncode != 0:
    print("❌ 配置测试失败，恢复备份...")
    run_cmd(f"sudo cp {backup_file} /etc/nginx/sites-enabled/dailybaro.conf")
    sys.exit(1)

print("✅ 配置测试通过")

# 重新加载
print("\n🔄 重新加载 Nginx...")
run_cmd("sudo systemctl reload nginx")
print("✅ Nginx 已重新加载")

# 测试 HTTPS
print("\n🧪 测试 HTTPS...")
import time
time.sleep(1)
result = run_cmd("curl -I https://dailybaro.cn 2>&1 | head -5", check=False)
print(result.stdout)

print("\n==========================================")
print("✅ 修复完成！")
print("==========================================")
