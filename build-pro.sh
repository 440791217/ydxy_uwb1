#!/bin/bash
echo "====================连接服务端=================="
ssh root@172.19.1.31 "cd /root/limited-space-work/app && docker-compose pull ydxy-uwb && docker-compose up -d ydxy-uwb && docker-compose logs -f --tail 1000 ydxy-uwb"
echo "====================部署完毕===================="
echo 按任意键退出
read -n 1
