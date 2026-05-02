<template>
  <button
    class="sk-btn"
    :class="[type, size, { loading, disabled }]"
    :disabled="disabled || loading"
    @tap="$emit('click')"
  >
    <text v-if="loading" class="sk-btn-loading">...</text>
    <text v-else><slot /></text>
  </button>
</template>

<script lang="ts">
export default {
  name: 'SkButton',
  props: {
    type: { type: String, default: 'primary' },  // primary | default | danger
    size: { type: String, default: 'medium' },    // small | medium | large
    disabled: { type: Boolean, default: false },
    loading: { type: Boolean, default: false },
  },
  emits: ['click'],
}
</script>

<style scoped>
.sk-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  border: none;
  border-radius: 0;
  width: 100%;
  padding: 20rpx 32rpx;
  font-size: 28rpx;
}
.sk-btn.primary { background: #000; color: #fff; }
.sk-btn.default { background: #fff; color: #000; border: 2rpx solid #000; }
.sk-btn.danger { background: #fff; color: #b71c1c; border: 2rpx solid #b71c1c; }
.sk-btn.small { padding: 12rpx 20rpx; font-size: 24rpx; }
.sk-btn.large { padding: 28rpx 40rpx; font-size: 32rpx; }
.sk-btn.disabled, .sk-btn.loading { opacity: 0.5; }
</style>
