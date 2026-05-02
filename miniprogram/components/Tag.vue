<template>
  <text class="sk-tag" :class="[type, size]">
    <slot />
  </text>
</template>

<script lang="ts">
export default {
  name: 'SkTag',
  props: {
    type: { type: String, default: 'default' },  // default | success | warning | error | info
    size: { type: String, default: 'medium' },
  },
}
</script>

<style scoped>
.sk-tag {
  display: inline-block;
  font-weight: 600;
  border: 2rpx solid #000;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  line-height: 1.4;
}
.sk-tag.default { background: #f5f5f5; color: #333; }
.sk-tag.success { background: #e8f5e9; color: #2e7d32; }
.sk-tag.warning { background: #fff3e0; color: #e65100; }
.sk-tag.error { background: #fbe9e7; color: #b71c1c; }
.sk-tag.info { background: #e3f2fd; color: #1565c0; }
.sk-tag.small { font-size: 20rpx; padding: 2rpx 8rpx; }
</style>
