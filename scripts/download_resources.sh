#!/bin/bash

# Paradise HTML游戏资源下载脚本
# 在构建APK之前执行，下载远程资源到本地

set -e  # 遇到错误立即退出

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🌟 Paradise HTML游戏资源下载器"
echo "=================================================="
echo "📁 项目根目录: $PROJECT_ROOT"
echo "📁 脚本目录: $SCRIPT_DIR"
echo ""

# 检查Python环境
echo "🐍 检查Python环境..."
if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
    echo "✅ 找到Python3: $(which python3)"
elif command -v python &> /dev/null; then
    PYTHON_CMD="python"
    echo "✅ 找到Python: $(which python)"
else
    echo "❌ 未找到Python，请安装Python 3.6+"
    exit 1
fi

# 检查Python版本
PYTHON_VERSION=$($PYTHON_CMD --version 2>&1 | cut -d' ' -f2)
echo "📋 Python版本: $PYTHON_VERSION"
echo "✅ 使用Python内置库，无外部依赖"

echo ""
echo "🚀 开始执行资源下载..."
echo "=================================================="

# 切换到项目根目录
cd "$PROJECT_ROOT"

# 执行Python下载脚本
$PYTHON_CMD "$SCRIPT_DIR/download_resources.py"

# 检查执行结果
if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 资源下载完成!"
    echo "📁 资源位置: app/src/main/assets/"
    echo "📄 配置文件: app/src/main/assets/apps.json"
    echo ""
    echo "✅ 现在可以继续构建APK了"
else
    echo ""
    echo "❌ 资源下载失败，请检查网络连接和API状态"
    exit 1
fi
