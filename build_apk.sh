#!/bin/bash

# Paradise HTML Viewer 本地构建脚本
# 用于本地构建Debug和Release APK（未签名）

set -e  # 遇到错误立即退出

# 脚本信息
SCRIPT_NAME="Paradise HTML Viewer 构建脚本"
VERSION="1.0.0"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT=$(dirname "$(readlink -f "$0")")
cd "$PROJECT_ROOT"

# 输出目录
OUTPUT_DIR="$PROJECT_ROOT/build_output"

# 打印带颜色的消息
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# 打印标题
print_title() {
    echo
    print_message $CYAN "=================================="
    print_message $CYAN "$1"
    print_message $CYAN "=================================="
    echo
}

# 打印步骤
print_step() {
    print_message $BLUE "🔄 $1"
}

# 打印成功
print_success() {
    print_message $GREEN "✅ $1"
}

# 打印警告
print_warning() {
    print_message $YELLOW "⚠️  $1"
}

# 打印错误
print_error() {
    print_message $RED "❌ $1"
}

# 检查Java环境
check_java() {
    print_step "检查Java环境..."
    
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_success "Java版本: $JAVA_VERSION"
        
        # 检查Java 17
        if [[ "$JAVA_VERSION" == 17.* ]] || [[ "$JAVA_VERSION" == 1.8.* ]]; then
            print_success "Java版本兼容"
        else
            print_warning "建议使用Java 17，当前版本: $JAVA_VERSION"
        fi
    else
        print_error "未找到Java，请安装Java 17"
        exit 1
    fi
    
    # 设置JAVA_HOME（macOS）
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v /usr/libexec/java_home &> /dev/null; then
            # 优先使用Java 17
            if /usr/libexec/java_home -v 17 &> /dev/null; then
                export JAVA_HOME=$(/usr/libexec/java_home -v 17)
                print_success "JAVA_HOME设置为Java 17: $JAVA_HOME"
            elif /usr/libexec/java_home -v 11 &> /dev/null; then
                export JAVA_HOME=$(/usr/libexec/java_home -v 11)
                print_warning "使用Java 11: $JAVA_HOME (建议升级到Java 17)"
            else
                export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
                print_warning "使用默认Java: $JAVA_HOME"
            fi
        fi
        
        # 检查Homebrew安装的OpenJDK 17
        if [ -d "/opt/homebrew/opt/openjdk@17" ]; then
            export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
            export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
            print_success "使用Homebrew OpenJDK 17: $JAVA_HOME"
        fi
    fi
}

# 检查Gradle
check_gradle() {
    print_step "检查Gradle..."
    
    if [ -f "./gradlew" ]; then
        chmod +x ./gradlew
        print_success "Gradle Wrapper已就绪"
    else
        print_error "未找到gradlew，请确保在项目根目录运行"
        exit 1
    fi
}

# 下载游戏资源
download_resources() {
    print_step "下载游戏资源..."
    
    if [ -f "./scripts/download_resources.sh" ]; then
        chmod +x ./scripts/download_resources.sh
        ./scripts/download_resources.sh
        print_success "游戏资源下载完成"
    else
        print_warning "未找到资源下载脚本，跳过资源下载"
    fi
}

# 清理构建
clean_build() {
    print_step "清理之前的构建..."
    ./gradlew clean
    print_success "构建清理完成"
}

# 构建Debug APK
build_debug() {
    print_step "构建Debug APK..."
    ./gradlew app:assembleDebug
    
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        print_success "Debug APK构建成功"
        
        # 复制到输出目录
        mkdir -p "$OUTPUT_DIR"
        cp "app/build/outputs/apk/debug/app-debug.apk" "$OUTPUT_DIR/paradise-html-viewer-debug.apk"
        print_success "Debug APK已复制到: $OUTPUT_DIR/paradise-html-viewer-debug.apk"
        
        # 显示文件信息
        DEBUG_SIZE=$(du -h "$OUTPUT_DIR/paradise-html-viewer-debug.apk" | cut -f1)
        print_message $CYAN "📱 Debug APK大小: $DEBUG_SIZE"
    else
        print_error "Debug APK构建失败"
        return 1
    fi
}

# 构建Release APK（未签名）
build_release() {
    print_step "构建Release APK（未签名）..."
    ./gradlew app:assembleRelease
    
    if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
        print_success "Release APK构建成功"
        
        # 复制到输出目录
        mkdir -p "$OUTPUT_DIR"
        cp "app/build/outputs/apk/release/app-release-unsigned.apk" "$OUTPUT_DIR/paradise-html-viewer-release-unsigned.apk"
        print_success "Release APK已复制到: $OUTPUT_DIR/paradise-html-viewer-release-unsigned.apk"
        
        # 显示文件信息
        RELEASE_SIZE=$(du -h "$OUTPUT_DIR/paradise-html-viewer-release-unsigned.apk" | cut -f1)
        print_message $CYAN "📱 Release APK大小: $RELEASE_SIZE"
    else
        print_error "Release APK构建失败"
        return 1
    fi
}

# 生成构建报告
generate_report() {
    print_step "生成构建报告..."
    
    REPORT_FILE="$OUTPUT_DIR/build_report.txt"
    
    cat > "$REPORT_FILE" << EOF
Paradise HTML Viewer 构建报告
========================================

构建时间: $(date)
构建版本: $VERSION
Java版本: $JAVA_VERSION
构建环境: $(uname -s) $(uname -r)

APK文件:
EOF

    if [ -f "$OUTPUT_DIR/paradise-html-viewer-debug.apk" ]; then
        DEBUG_SIZE=$(du -h "$OUTPUT_DIR/paradise-html-viewer-debug.apk" | cut -f1)
        echo "✅ Debug APK: paradise-html-viewer-debug.apk ($DEBUG_SIZE)" >> "$REPORT_FILE"
    fi
    
    if [ -f "$OUTPUT_DIR/paradise-html-viewer-release-unsigned.apk" ]; then
        RELEASE_SIZE=$(du -h "$OUTPUT_DIR/paradise-html-viewer-release-unsigned.apk" | cut -f1)
        echo "✅ Release APK: paradise-html-viewer-release-unsigned.apk ($RELEASE_SIZE)" >> "$REPORT_FILE"
    fi
    
    cat >> "$REPORT_FILE" << EOF

游戏资源:
EOF
    
    if [ -f "app/src/main/assets/apps.json" ]; then
        GAME_COUNT=$(cat app/src/main/assets/apps.json | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "未知")
        echo "✅ 游戏数量: $GAME_COUNT 个" >> "$REPORT_FILE"
        echo "✅ 资源文件: apps.json, images/, html/" >> "$REPORT_FILE"
    else
        echo "⚠️  未找到游戏资源" >> "$REPORT_FILE"
    fi
    
    cat >> "$REPORT_FILE" << EOF

安装说明:
- Debug APK: 可直接安装，用于开发和测试
- Release APK: 未签名版本，需要启用"允许安装未知来源应用"

JavaScript接口:
- ParadiseGame.submitScore(score) - 提交游戏分数
- ParadiseGame.getHighScore() - 获取最高分
- ParadiseGame.getPlayCount() - 获取游玩次数
- ParadiseGame.gameStart() - 游戏开始通知
- ParadiseGame.gameEnd(score, time) - 游戏结束通知
- ParadiseGame.showMessage(msg) - 显示提示消息

构建完成！🎉
EOF

    print_success "构建报告已生成: $REPORT_FILE"
}

# 显示帮助信息
show_help() {
    cat << EOF
$SCRIPT_NAME v$VERSION

用法: $0 [选项]

选项:
  -h, --help          显示此帮助信息
  -c, --clean         清理构建后退出
  -d, --debug-only    只构建Debug APK
  -r, --release-only  只构建Release APK
  -s, --skip-resources 跳过资源下载
  --no-report         不生成构建报告

示例:
  $0                  # 完整构建（推荐）
  $0 -d               # 只构建Debug APK
  $0 -r               # 只构建Release APK
  $0 -s               # 跳过资源下载
  $0 -c               # 只清理构建

构建产物将保存在: $OUTPUT_DIR/
EOF
}

# 主函数
main() {
    # 解析命令行参数
    CLEAN_ONLY=false
    DEBUG_ONLY=false
    RELEASE_ONLY=false
    SKIP_RESOURCES=false
    NO_REPORT=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--clean)
                CLEAN_ONLY=true
                shift
                ;;
            -d|--debug-only)
                DEBUG_ONLY=true
                shift
                ;;
            -r|--release-only)
                RELEASE_ONLY=true
                shift
                ;;
            -s|--skip-resources)
                SKIP_RESOURCES=true
                shift
                ;;
            --no-report)
                NO_REPORT=true
                shift
                ;;
            *)
                print_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 显示标题
    print_title "$SCRIPT_NAME v$VERSION"
    
    # 检查环境
    check_java
    check_gradle
    
    # 清理构建
    clean_build
    
    if [ "$CLEAN_ONLY" = true ]; then
        print_success "清理完成，退出"
        exit 0
    fi
    
    # 下载资源
    if [ "$SKIP_RESOURCES" = false ]; then
        download_resources
    else
        print_warning "跳过资源下载"
    fi
    
    # 构建APK
    BUILD_SUCCESS=true
    
    if [ "$RELEASE_ONLY" = false ]; then
        if ! build_debug; then
            BUILD_SUCCESS=false
        fi
    fi
    
    if [ "$DEBUG_ONLY" = false ]; then
        if ! build_release; then
            BUILD_SUCCESS=false
        fi
    fi
    
    # 生成报告
    if [ "$NO_REPORT" = false ] && [ "$BUILD_SUCCESS" = true ]; then
        generate_report
    fi
    
    # 显示结果
    print_title "构建完成"
    
    if [ "$BUILD_SUCCESS" = true ]; then
        print_success "所有APK构建成功！"
        print_message $CYAN "📁 输出目录: $OUTPUT_DIR"
        
        if [ -d "$OUTPUT_DIR" ]; then
            print_message $CYAN "📱 构建产物:"
            ls -la "$OUTPUT_DIR" | grep -E "\\.apk$|\\.txt$" | while read line; do
                print_message $CYAN "   $line"
            done
        fi
        
        echo
        print_message $PURPLE "🎉 Paradise HTML Viewer 构建完成！"
        print_message $PURPLE "🌴 享受您的数字天堂！"
    else
        print_error "构建过程中出现错误"
        exit 1
    fi
}

# 运行主函数
main "$@"
