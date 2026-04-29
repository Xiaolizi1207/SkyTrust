<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('SkyTrust 小程序启动')
})

onShow(() => {
  console.log('SkyTrust 小程序显示')
})

onHide(() => {
  console.log('SkyTrust 小程序隐藏')
})
</script>
