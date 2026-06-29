import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    server: {
      proxy: {
        '/api/v1': { target: env.VITE_PROXY_TARGET || 'http://localhost:18011', changeOrigin: true },
      },
    },
  }
})
