#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
资源预下载脚本
在打包APK之前执行，从API获取游戏数据并下载相关资源到本地
使用Python内置库，无外部依赖
"""

import os
import sys
import json
import urllib.request
import urllib.parse
import urllib.error
import shutil
from pathlib import Path
import time
import ssl

# 配置
API_URL = "http://47.112.166.202:8080/userapi/allGame2"
ASSETS_DIR = "app/src/main/assets"
IMAGES_DIR = "images"
HTML_DIR = "html"
JSON_FILE = "apps.json"

# 请求超时设置
REQUEST_TIMEOUT = 30

class ResourceDownloader:
    def __init__(self):
        self.project_root = Path(__file__).parent.parent
        self.assets_path = self.project_root / ASSETS_DIR
        self.images_path = self.assets_path / IMAGES_DIR
        self.html_path = self.assets_path / HTML_DIR
        self.json_path = self.assets_path / JSON_FILE
        
        # 创建SSL上下文（用于HTTPS请求）
        self.ssl_context = ssl.create_default_context()
        # 对于开发环境，允许未验证的证书
        self.ssl_context.check_hostname = False
        self.ssl_context.verify_mode = ssl.CERT_NONE
        
        # 创建必要的目录
        self.setup_directories()
    
    def setup_directories(self):
        """创建必要的目录结构"""
        print("📁 设置目录结构...")
        
        # 清空assets目录
        if self.assets_path.exists():
            print(f"🗑️  清空assets目录: {self.assets_path}")
            shutil.rmtree(self.assets_path)
        
        # 重新创建目录
        self.assets_path.mkdir(parents=True, exist_ok=True)
        self.images_path.mkdir(parents=True, exist_ok=True)
        self.html_path.mkdir(parents=True, exist_ok=True)
        
        print(f"✅ 目录创建完成:")
        print(f"   📂 Assets: {self.assets_path}")
        print(f"   📂 Images: {self.images_path}")
        print(f"   📂 HTML: {self.html_path}")
    
    def fetch_games_data(self):
        """从API获取游戏数据"""
        print(f"🌐 正在从API获取数据: {API_URL}")
        
        try:
            # 创建请求
            request = urllib.request.Request(API_URL)
            request.add_header('User-Agent', 'Paradise-HTML-Downloader/1.0')
            
            # 发送请求
            with urllib.request.urlopen(request, timeout=REQUEST_TIMEOUT, context=self.ssl_context) as response:
                if response.getcode() != 200:
                    raise Exception(f"HTTP错误: {response.getcode()}")
                
                # 读取响应数据
                data = response.read().decode('utf-8')
                games_data = json.loads(data)
                
                print(f"✅ 成功获取 {len(games_data)} 个游戏数据")
                return games_data
                
        except urllib.error.HTTPError as e:
            print(f"❌ HTTP请求失败: {e.code} {e.reason}")
            sys.exit(1)
        except urllib.error.URLError as e:
            print(f"❌ 网络请求失败: {e.reason}")
            sys.exit(1)
        except json.JSONDecodeError as e:
            print(f"❌ JSON解析失败: {e}")
            sys.exit(1)
        except Exception as e:
            print(f"❌ API请求失败: {e}")
            sys.exit(1)
    
    def download_file(self, url, local_path):
        """下载单个文件"""
        try:
            print(f"⬇️  下载: {url}")
            
            # 创建请求
            request = urllib.request.Request(url)
            request.add_header('User-Agent', 'Paradise-HTML-Downloader/1.0')
            
            # 确保目录存在
            local_path.parent.mkdir(parents=True, exist_ok=True)
            
            # 下载文件
            with urllib.request.urlopen(request, timeout=REQUEST_TIMEOUT, context=self.ssl_context) as response:
                if response.getcode() != 200:
                    print(f"❌ HTTP错误: {response.getcode()}")
                    return False
                
                # 写入文件
                with open(local_path, 'wb') as f:
                    # 分块下载
                    while True:
                        chunk = response.read(8192)
                        if not chunk:
                            break
                        f.write(chunk)
            
            print(f"✅ 下载完成: {local_path.name}")
            return True
            
        except urllib.error.HTTPError as e:
            print(f"❌ HTTP下载失败 {url}: {e.code} {e.reason}")
            return False
        except urllib.error.URLError as e:
            print(f"❌ 网络下载失败 {url}: {e.reason}")
            return False
        except Exception as e:
            print(f"❌ 下载失败 {url}: {e}")
            return False
    
    def get_file_extension(self, url):
        """从URL获取文件扩展名"""
        parsed = urllib.parse.urlparse(url)
        path = parsed.path
        
        # 获取扩展名
        _, ext = os.path.splitext(path)
        
        # 如果没有扩展名，根据URL内容推测
        if not ext:
            if 'image' in url or any(img_ext in url.lower() for img_ext in ['.jpg', '.png', '.jpeg', '.gif', '.webp']):
                ext = '.png'  # 默认图片格式
            elif 'html' in url or '.html' in url.lower():
                ext = '.html'
        
        return ext or '.png'  # 默认返回.png
    
    def generate_local_filename(self, title, author, url, file_type):
        """生成本地文件名"""
        # 清理标题和作者名，移除特殊字符
        clean_title = "".join(c for c in title if c.isalnum() or c in (' ', '-', '_')).strip()
        clean_author = "".join(c for c in author if c.isalnum() or c in (' ', '-', '_')).strip()
        
        # 替换空格为下划线
        clean_title = clean_title.replace(' ', '_')
        clean_author = clean_author.replace(' ', '_')
        
        # 获取文件扩展名
        ext = self.get_file_extension(url)
        
        # 生成文件名: 作者_标题.扩展名
        filename = f"{clean_author}_{clean_title}{ext}"
        
        return filename
    
    def download_resources(self, games_data):
        """下载所有资源"""
        print(f"📥 开始下载资源...")
        
        processed_games = []
        success_count = 0
        total_count = len(games_data)
        
        for i, game in enumerate(games_data, 1):
            print(f"\n🎮 处理游戏 {i}/{total_count}: {game['title']}")
            
            # 生成本地文件名
            image_filename = self.generate_local_filename(
                game['title'], game['author'], game['image'], 'image'
            )
            html_filename = self.generate_local_filename(
                game['title'], game['author'], game['url'], 'html'
            )
            
            # 本地文件路径
            local_image_path = self.images_path / image_filename
            local_html_path = self.html_path / html_filename
            
            # 下载图片
            image_success = self.download_file(game['image'], local_image_path)
            
            # 下载HTML
            html_success = self.download_file(game['url'], local_html_path)
            
            # 只有两个文件都下载成功才添加到最终列表
            if image_success and html_success:
                processed_game = {
                    "title": game['title'],
                    "author": game['author'],
                    "image": f"{IMAGES_DIR}/{image_filename}",
                    "type": game['type'],
                    "url": f"{HTML_DIR}/{html_filename}"
                }
                processed_games.append(processed_game)
                success_count += 1
                print(f"✅ 游戏处理成功: {game['title']}")
            else:
                print(f"❌ 游戏处理失败: {game['title']}")
        
        print(f"\n📊 下载统计:")
        print(f"   总数: {total_count}")
        print(f"   成功: {success_count}")
        print(f"   失败: {total_count - success_count}")
        
        return processed_games
    
    def save_apps_json(self, games_data):
        """保存apps.json文件"""
        print(f"\n💾 保存apps.json文件...")
        
        try:
            with open(self.json_path, 'w', encoding='utf-8') as f:
                json.dump(games_data, f, ensure_ascii=False, indent=2)
            
            print(f"✅ apps.json保存成功: {self.json_path}")
            print(f"📄 包含 {len(games_data)} 个游戏")
            
        except Exception as e:
            print(f"❌ 保存apps.json失败: {e}")
            sys.exit(1)
    
    def print_summary(self, games_data):
        """打印下载摘要"""
        print(f"\n🎯 下载完成摘要:")
        print(f"{'='*50}")
        
        for game in games_data:
            print(f"🎮 {game['title']} ({game['type']})")
            print(f"   👤 作者: {game['author']}")
            print(f"   🖼️  图片: {game['image']}")
            print(f"   📄 HTML: {game['url']}")
            print()
        
        print(f"{'='*50}")
        print(f"✅ 总计: {len(games_data)} 个游戏资源下载完成")
    
    def run(self):
        """执行完整的下载流程"""
        print("🚀 开始资源预下载流程...")
        print(f"⏰ 开始时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
        
        try:
            # 1. 获取API数据
            games_data = self.fetch_games_data()
            
            # 2. 下载资源
            processed_games = self.download_resources(games_data)
            
            # 3. 保存JSON文件
            self.save_apps_json(processed_games)
            
            # 4. 打印摘要
            self.print_summary(processed_games)
            
            print(f"\n🎉 资源预下载完成!")
            print(f"⏰ 结束时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
            
        except KeyboardInterrupt:
            print(f"\n⚠️  用户中断下载")
            sys.exit(1)
        except Exception as e:
            print(f"\n❌ 下载过程出错: {e}")
            sys.exit(1)

def main():
    """主函数"""
    print("🌟 Paradise HTML游戏资源下载器")
    print("=" * 50)
    
    # 检查Python版本
    if sys.version_info < (3, 6):
        print("❌ 需要Python 3.6或更高版本")
        sys.exit(1)
    
    print(f"✅ Python版本: {sys.version}")
    print("✅ 使用内置库，无外部依赖")
    
    # 执行下载
    downloader = ResourceDownloader()
    downloader.run()

if __name__ == "__main__":
    main()
