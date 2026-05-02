<template>
  <view class="sk-card">
    <view v-if="title" class="sk-card-header">
      <text class="sk-card-title">{{ title }}</text>
    </view>
    <view class="sk-card-body" :style="{ padding: padding }">
      <slot />
    </view>
  </view>
</template>

<script lang="ts">
export default {
  name: 'SkCard',
  props: {
    title: { type: String, default: '' },
    padding: { type: String, default: '24rpx' },
  },
}
</script>

<style scoped>
.sk-card { background: #fff; border: 2rpx solid #000; margin-bottom: 24rpx; }
.sk-card-header { padding: 24rpx 24rpx 0; }
.sk-card-title { font-size: 30rpx; font-weight: 700; }
.sk-card-body { padding: 24rpx; }
</style>
