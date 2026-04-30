import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    server: {
      proxy: {
        '/auth': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/users': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/pets': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/publicity': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/volunteers': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/lost': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/medical': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/items': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/tasks': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
        '/notices': { target: env.VITE_PROXY_TARGET || 'http://localhost:8080', changeOrigin: true },
      },
    },
  }
})
