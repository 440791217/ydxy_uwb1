#!/bin/bash
# 万家乐仓库部署
echo "====================连接服务端=================="
ssh asus@223.108.172.110 -p 33336 "cd /opt/lsw_local/app && sudo docker-compose pull ydxy-uwb && sudo docker-compose up -d ydxy-uwb && sudo docker-compose logs -f --tail 1000 ydxy-uwb"
echo "====================部署完毕===================="
echo 按任意键退出
read -n 1
