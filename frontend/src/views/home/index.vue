<template>
  <div class="landing">
    <!-- ====== 1. 导航栏 ====== -->
    <nav class="navbar">
      <div class="container navbar-inner">
        <div class="navbar-logo">
          <div class="logo-mark"><span>S</span></div>
          <span class="logo-text">SkyTrust</span>
        </div>
        <div class="navbar-links">
          <a href="#features">产品功能</a>
          <a href="#blockchain">区块链模块</a>
          <a href="#ai">AI 智能模块</a>
          <a href="#about">关于我们</a>
        </div>
        <div class="navbar-actions">
          <router-link to="/login" class="btn btn-sm btn-secondary">登录</router-link>
          <router-link to="/register" class="btn btn-sm btn-primary">注册</router-link>
        </div>
      </div>
    </nav>

    <!-- ====== 2. Hero 区域 ====== -->
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-content">
          <h1 class="text-h1">无人机共享<br>租赁区块链平台</h1>
          <p>基于 AI 调度与链上存证，为无人机租赁提供安全、透明、高效的全栈解决方案。</p>
          <div class="hero-buttons">
            <router-link to="/register" class="btn btn-lg btn-primary">立即注册</router-link>
            <a href="#features" class="btn btn-lg btn-secondary">了解更多</a>
          </div>
        </div>
        <div class="hero-visual">
          <!-- 极简线框插图 -->
          <div class="wireframe">
            <div class="wireframe-inner"></div>
            <div class="wireframe-dot tl"></div>
            <div class="wireframe-dot tr"></div>
            <div class="wireframe-dot bl"></div>
            <div class="wireframe-dot br"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 3. 指标栏 ====== -->
    <section class="metrics">
      <div class="container metrics-inner">
        <div class="metric">
          <div class="metric-value">10,000+</div>
          <div class="metric-label">注册无人机</div>
        </div>
        <div class="metric">
          <div class="metric-value">500+</div>
          <div class="metric-label">活跃机库</div>
        </div>
        <div class="metric">
          <div class="metric-value">99.9%</div>
          <div class="metric-label">数据上链率</div>
        </div>
        <div class="metric">
          <div class="metric-value">50ms</div>
          <div class="metric-label">围栏检测延迟</div>
        </div>
      </div>
    </section>

    <!-- ====== 4. 区块链模块入口 ====== -->
    <section class="section" id="blockchain">
      <div class="container">
        <h2 class="text-h2" style="margin:0 0 12px 0;">区块链模块</h2>
        <p class="text-body" style="max-width:640px;margin:0 0 48px 0;">
          基于 EVM 兼容链的智能合约体系，确保飞手身份、飞行日志、设备护照的不可篡改与链上可验证。
        </p>
        <div class="grid-3">
          <!-- 数字护照 -->
          <div class="card card-strong">
            <div style="width:40px;height:40px;border:2px solid var(--border-strong);display:flex;align-items:center;justify-content:center;margin-bottom:20px;">
              <span style="font-size:18px;font-weight:700;">P</span>
            </div>
            <h3 class="text-h3" style="margin:0 0 8px 0;">数字护照</h3>
            <p class="text-sm">ERC-721 NFT 记录每台无人机的出厂信息、维修历史、电池循环与固件版本。维修记录需 2-of-N 多签确认，支持全生命周期追溯。</p>
          </div>
          <!-- 执照验证 -->
          <div class="card card-strong">
            <div style="width:40px;height:40px;border:2px solid var(--border-strong);display:flex;align-items:center;justify-content:center;margin-bottom:20px;">
              <span style="font-size:18px;font-weight:700;">L</span>
            </div>
            <h3 class="text-h3" style="margin:0 0 8px 0;">执照验证</h3>
            <p class="text-sm">链上哈希存证飞手执照，仅存 keccak256 哈希不存明文。租赁订单绑定 licenseHash + 用户签名，事故时可快速回溯资质。</p>
          </div>
          <!-- 飞行日志 -->
          <div class="card card-strong">
            <div style="width:40px;height:40px;border:2px solid var(--border-strong);display:flex;align-items:center;justify-content:center;margin-bottom:20px;">
              <span style="font-size:18px;font-weight:700;">F</span>
            </div>
            <h3 class="text-h3" style="margin:0 0 8px 0;">飞行日志</h3>
            <p class="text-sm">航点数据哈希实时上链。原始数据云端存储，链上仅存不可篡改哈希。配套 CLI 工具支持离线日志完整性校验。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 5. AI 智能模块入口 ====== -->
    <section class="section" id="ai" style="background:var(--bg-page);border-top:1px solid var(--border-light);border-bottom:1px solid var(--border-light);">
      <div class="container">
        <h2 class="text-h2" style="margin:0 0 12px 0;">AI 智能模块</h2>
        <p class="text-body" style="max-width:640px;margin:0 0 48px 0;">
          机载端实时地理围栏 + 云端动态定价调度 + 大语言模型交互，构建智能无人机运营闭环。
        </p>
        <div class="grid-3">
          <!-- 地理围栏 -->
          <div class="card card-strong">
            <div style="width:40px;height:40px;border:2px solid var(--border-strong);display:flex;align-items:center;justify-content:center;margin-bottom:20px;">
              <span style="font-size:18px;font-weight:700;">G</span>
            </div>
            <h3 class="text-h3" style="margin:0 0 8px 0;">地理围栏</h3>
            <p class="text-sm">禁飞区数据完全本地化存储，无网络依赖。30 秒轨迹预测 + 点入多边形检测，闯入告警触发强制悬停（权限高于遥控）。</p>
          </div>
          <!-- 动态定价 -->
          <div class="card card-strong">
            <div style="width:40px;height:40px;border:2px solid var(--border-strong);display:flex;align-items:center;justify-content:center;margin-bottom:20px;">
              <span style="font-size:18px;font-weight:700;">¥</span>
            </div>
            <h3 class="text-h3" style="margin:0 0 8px 0;">动态定价</h3>
            <p class="text-sm">LSTM/XGBoost 时间序列模型预测 48 小时各机库需求。动态调整租赁价格（成本 120% ~ 基础 300%），自动生成换电与调配任务。</p>
          </div>
          <!-- AI 助手 -->
          <div class="card card-strong">
            <div style="width:40px;height:40px;border:2px solid var(--border-strong);display:flex;align-items:center;justify-content:center;margin-bottom:20px;">
              <span style="font-size:18px;font-weight:700;">AI</span>
            </div>
            <h3 class="text-h3" style="margin:0 0 8px 0;">AI 助手</h3>
            <p class="text-sm">自然语言交互生成飞行前检查清单与 DJI MSDK 兼容拍摄参数。危险指令（超视距/超风速）需二次确认方可执行，交互全程记录审计日志。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 6. 产品展示（双列） ====== -->
    <section class="section" id="features">
      <div class="container grid-2">
        <div>
          <h3 class="text-h3" style="margin:0 0 12px 0;">机载端 + 云端协同</h3>
          <p class="text-body" style="margin:0 0 24px 0;">机载 ARM 设备运行轻量地理围栏预测器（C++ 伪代码已就绪），云端通过 Docker 部署 Spring Boot 微服务与 Python AI 引擎。</p>
          <!-- 指标行 -->
          <div style="display:flex;gap:48px;">
            <div><div style="font-size:1.75rem;font-weight:700;color:var(--text-primary);">&lt;5ms</div><div class="text-sm">围栏检测</div></div>
            <div><div style="font-size:1.75rem;font-weight:700;color:var(--text-primary);">&lt;50ms</div><div class="text-sm">视觉推理</div></div>
            <div><div style="font-size:1.75rem;font-weight:700;color:var(--text-primary);">0 网络</div><div class="text-sm">离线可用</div></div>
          </div>
        </div>
        <!-- 线框占位图 -->
        <div style="border:2px solid var(--border-strong);aspect-ratio:16/10;display:flex;align-items:center;justify-content:center;">
          <span style="font-size:1.125rem;font-weight:700;color:var(--text-primary);">设备示意图</span>
        </div>
      </div>
    </section>

    <!-- ====== 7. 用户语录 ====== -->
    <section style="background:var(--bg-page);padding:80px 0;">
      <div class="container">
        <div class="quote">
          <p class="quote-text">"SkyTrust 的链上存证与自动定价让我们的机库运营效率提升了 40%。AI 助手的飞行前检查清单大幅降低了新人炸机率。"</p>
          <span class="quote-author">李明</span>
          <span class="quote-role"> — 某景区无人机租赁运营商</span>
        </div>
      </div>
    </section>

    <!-- ====== 8. CTA ====== -->
    <section class="cta">
      <div class="container">
        <h2 class="text-h2">准备好启航了吗？</h2>
        <p>加入 SkyTrust 无人机共享网络，享受区块链信任与 AI 智能的双重赋能。</p>
        <div class="cta-buttons">
          <router-link to="/register" class="btn btn-lg btn-primary">免费注册</router-link>
          <router-link to="/login" class="btn btn-lg btn-secondary">已有账号？登录</router-link>
        </div>
      </div>
    </section>

    <!-- ====== 9. 页脚 ====== -->
    <footer class="footer">
      <div class="container">
        <div class="footer-inner">
          <span class="footer-brand">SkyTrust</span>
          <div class="footer-links">
            <a href="#blockchain">区块链模块</a>
            <a href="#ai">AI 模块</a>
            <a href="#features">产品功能</a>
            <router-link to="/login">登录</router-link>
          </div>
          <div class="footer-copy">&copy; 2026 SkyTrust Inc. All rights reserved.</div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
// 首页纯展示，无需额外逻辑。
// 路由跳转通过 template 中的 @click 和 <router-link> 处理。
</script>

<style scoped>
/* 所有样式继承自 global.css 的设计令牌。
   本文件仅添加 landing 页面特有的局部覆盖。 */

/* 卡片悬停过渡（全局类已处理，此处仅确保网格一致性） */
.card-hover { transition: background 0.1s ease; }

/* 锚点平滑滚动 */
html { scroll-behavior: smooth; }

/* 区块间无多余 margin，global.css 的 .section 已处理 */
</style>
