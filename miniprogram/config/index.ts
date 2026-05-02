const ENV: 'development' | 'production' =
  (typeof uni !== 'undefined' && uni.getAccountInfoSync?.().miniProgram?.envVersion === 'release')
    ? 'production'
    : 'development'

const API_HOST = ENV === 'production'
  ? 'https://api.skytrust.com'
  : 'http://localhost:9090'

export const API_BASE_URL = `${API_HOST}/api`
export const WS_BASE_URL = `${API_HOST.replace('http', 'ws')}/ws`
export { ENV }
