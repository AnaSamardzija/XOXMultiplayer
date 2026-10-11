import {defineConfig} from 'vite'
import {svelte} from '@sveltejs/vite-plugin-svelte'

export default defineConfig({
  plugins: [svelte()],

  build: {
    outDir: 'dist',
    emptyOutDir: true
  },

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // Bez ws: true proxy prosleđuje samo obične HTTP zahteve, a WebSocket vezu ne
      '/ws': {
        target: 'http://localhost:8080',
        ws: true
      }
    }
  }
})
