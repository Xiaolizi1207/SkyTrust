<template>
  <text class="sk-status" :class="[statusClass]">{{ statusLabel }}</text>
</template>

<script lang="ts">
export default {
  name: 'SkStatusBadge',
  props: {
    status: { type: Number, default: 0 },
    type: { type: String, default: 'device' },  // device | order
  },
  computed: {
    statusClass(): string {
      if (this.type === 'order') {
        return ['pending', 'active', 'done', 'cancelled'][this.status] || 'default'
      }
      return ['offline', 'online', 'flying', 'maintenance', 'scrapped'][this.status] || 'default'
    },
    statusLabel(): string {
      if (this.type === 'order') {
        return ['待支付', '进行中', '已完成', '已取消'][this.status] || '未知'
      }
      return ['离线', '在线', '飞行中', '维护中', '已报废'][this.status] || '未知'
    },
  },
}
</script>

<style scoped>
.sk-status { display: inline-block; font-weight: 600; padding: 4rpx 12rpx; font-size: 22rpx; border: 2rpx solid #000; }
.sk-status.online, .sk-status.active { background: #e8f5e9; color: #2e7d32; }
.sk-status.offline { background: #f5f5f5; color: #999; }
.sk-status.flying { background: #e3f2fd; color: #1565c0; }
.sk-status.maintenance { background: #fff3e0; color: #e65100; }
.sk-status.scrapped { background: #fbe9e7; color: #b71c1c; }
.sk-status.pending { background: #fff3e0; color: #e65100; }
.sk-status.done { background: #e8f5e9; color: #2e7d32; }
.sk-status.cancelled { background: #f5f5f5; color: #999; }
</style>
