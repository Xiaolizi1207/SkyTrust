import { defineConfig } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';

global.uniPlugin = global.uniPlugin || { options: {} };

export default defineConfig({
  plugins: [uni()],
});
