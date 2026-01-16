import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'path'
import { copyFileSync, mkdirSync, readdirSync, statSync, existsSync, rmdirSync } from 'fs'

// 确保 static 目录结构在编译输出中保持正确
const ensureStaticStructure = () => {
  return {
    name: 'ensure-static-structure',
    writeBundle() {
      const outputDir = path.resolve(__dirname, 'dist/dev/mp-weixin')
      const staticDir = path.join(outputDir, 'static')
      
      // 创建 static 目录
      if (!existsSync(staticDir)) {
        mkdirSync(staticDir, { recursive: true })
      }
      
      // 移动 icons 目录到 static/icons
      const iconsDir = path.join(outputDir, 'icons')
      if (existsSync(iconsDir)) {
        mkdirSync(path.join(staticDir, 'icons'), { recursive: true })
        const files = readdirSync(iconsDir)
        files.forEach(file => {
          const srcPath = path.join(iconsDir, file)
          const destPath = path.join(staticDir, 'icons', file)
          const stat = statSync(srcPath)
          if (stat.isFile()) {
            copyFileSync(srcPath, destPath)
          }
        })
        // 删除原 icons 目录
        try {
          rmdirSync(iconsDir, { recursive: true })
        } catch (e) {}
      }
      
      // 移动 imgs 目录到 static/imgs
      const imgsDir = path.join(outputDir, 'imgs')
      if (existsSync(imgsDir)) {
        mkdirSync(path.join(staticDir, 'imgs'), { recursive: true })
        const files = readdirSync(imgsDir)
        files.forEach(file => {
          const srcPath = path.join(imgsDir, file)
          const destPath = path.join(staticDir, 'imgs', file)
          const stat = statSync(srcPath)
          if (stat.isFile()) {
            copyFileSync(srcPath, destPath)
          }
        })
        try {
          rmdirSync(imgsDir, { recursive: true })
        } catch (e) {}
      }
      
      // 移动 tabbar 目录到 static/tabbar
      const tabbarDir = path.join(outputDir, 'tabbar')
      if (existsSync(tabbarDir)) {
        mkdirSync(path.join(staticDir, 'tabbar'), { recursive: true })
        const files = readdirSync(tabbarDir)
        files.forEach(file => {
          const srcPath = path.join(tabbarDir, file)
          const destPath = path.join(staticDir, 'tabbar', file)
          const stat = statSync(srcPath)
          if (stat.isFile()) {
            copyFileSync(srcPath, destPath)
          }
        })
        try {
          rmdirSync(tabbarDir, { recursive: true })
        } catch (e) {}
      }
      
      // 移动根目录的图片文件到 static
      const rootFiles = ['logo.jpg', 'appbg.jpg']
      rootFiles.forEach(file => {
        const srcPath = path.join(outputDir, file)
        if (existsSync(srcPath)) {
          copyFileSync(srcPath, path.join(staticDir, file))
        }
      })
    }
  }
}

export default defineConfig({
  plugins: [
    uni(),
    ensureStaticStructure()
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  publicDir: 'static',
  server: {
    port: 3000,
    host: '0.0.0.0'
  }
})
