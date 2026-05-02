<template>
  <div class="iot-page">
    <div class="page-header">
      <h2>物联网设备监控</h2>
      <span class="subtitle">实时监控所有在线无人机状态与遥测数据</span>
    </div>

    <div class="stat-row">
      <div class="stat-card"><div class="stat-icon">■</div><div class="stat-info"><span class="stat-value">0</span><span class="stat-label">在线设备</span></div></div>
      <div class="stat-card"><div class="stat-icon">D</div><div class="stat-info"><span class="stat-value">0</span><span class="stat-label">注册设备</span></div></div>
      <div class="stat-card"><div class="stat-icon">@</div><div class="stat-info"><span class="stat-value">0</span><span class="stat-label">MQTT 消息/秒</span></div></div>
    </div>

    <div class="card">
      <h3 class="section-title">设备列表</h3>
      <table>
        <thead><tr><th>设备 ID</th><th>状态</th><th>GPS 坐标</th><th>电池</th><th>最后心跳</th></tr></thead>
        <tbody>
          <tr><td colspan="5" class="empty-state">暂无设备连接，请等待设备上线或检查 MQTT Broker 配置</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.iot-page { max-width: 1100px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0 0 4px; font-size: 20px; font-weight: 700; }
.subtitle { color: #666; font-size: 13px; }
.stat-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 24px; }
.stat-card { background: #fff; border: 2px solid #000; padding: 20px; display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 44px; height: 44px; border: 2px solid #000; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 18px; }
.stat-value { font-size: 28px; font-weight: 700; color: #000; }
.stat-label { font-size: 13px; color: #666; }
.card { background: #fff; border: 1px solid #e0e0e0; padding: 20px; margin-bottom: 20px; }
.section-title { font-size: 16px; font-weight: 700; margin: 0 0 16px; padding-bottom: 10px; border-bottom: 2px solid #000; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #e0e0e0; }
th { background: #f5f5f5; font-weight: 600; }
.empty-state { text-align: center; padding: 24px; color: #999; }
@media (max-width: 768px) { .stat-row { grid-template-columns: 1fr; } }
</style>
