import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  define: {
    // some libraries (eg. sockjs-client) reference `global` — map it to `globalThis`
    global: 'globalThis',
  },
  plugins: [
    vue(),
    vueJsx(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: true,
    port: 5173,
    proxy: {
      // proxy API calls to the backend service inside Docker network
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
      // proxy SockJS/STOMP endpoint (handles both HTTP handshake and WebSocket upgrade)
      '/ws-chat': {
        target: 'http://backend:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
      '/actuator': {
        target: 'http://backend:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
