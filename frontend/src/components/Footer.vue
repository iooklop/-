<script setup lang="ts">
import { Boxes } from 'lucide-vue-next'
</script>

<template>
  <footer class="footer">
    <div class="container footer-inner">
      <div class="brand">
        <Boxes :size="20" />
        <span>资源共享平台</span>
      </div>
      <p>上传 · 审核 · 检索 · 下载 —— 一个面向开发者的资源分享社区</p>
      <p class="copyright">© 2026 Resource Share. All rights reserved.</p>
    </div>
  </footer>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.footer {
  background: $bg-dark;
  color: $text-light;
  padding: $spacing-xl 0;
  margin-top: $spacing-2xl;
}

.footer-inner {
  text-align: center;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  font-size: $font-lg;
  font-weight: 600;
  color: $text-white;
  margin-bottom: $spacing-sm;
}

p {
  font-size: $font-sm;
  margin-bottom: $spacing-xs;
}

.copyright {
  color: #6b7280;
  font-size: $font-xs;
}
</style>
