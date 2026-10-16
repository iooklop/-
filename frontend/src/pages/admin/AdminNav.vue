<script setup lang="ts">
import { useRoute } from 'vue-router'
import { LayoutDashboard, ClipboardCheck, Users, FolderTree, Home } from 'lucide-vue-next'

const route = useRoute()
</script>

<template>
  <div class="admin-nav card">
    <h2 class="admin-title">管理后台</h2>
    <nav class="links">
      <router-link to="/admin" class="link" :class="{ active: route.path === '/admin' }">
        <LayoutDashboard :size="16" /> 数据概览
      </router-link>
      <router-link to="/admin/review" class="link" :class="{ active: route.path === '/admin/review' }">
        <ClipboardCheck :size="16" /> 资源审核
      </router-link>
      <router-link to="/admin/users" class="link" :class="{ active: route.path === '/admin/users' }">
        <Users :size="16" /> 用户管理
      </router-link>
      <router-link to="/admin/categories" class="link" :class="{ active: route.path === '/admin/categories' }">
        <FolderTree :size="16" /> 分类管理
      </router-link>
      <router-link to="/" class="link back">
        <Home :size="16" /> 返回前台
      </router-link>
    </nav>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.admin-nav {
  width: 220px;
  padding: $spacing-lg;
  align-self: flex-start;
  flex-shrink: 0;
}

.admin-title {
  font-size: $font-lg;
  margin-bottom: $spacing-md;
  color: $text-primary;
}

.links {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.link {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  color: $text-secondary;
  font-size: $font-sm;
  transition: all $transition-fast;

  &:hover {
    background: $bg-color;
    color: $primary-color;
  }

  &.active {
    background: rgba(30, 128, 255, 0.08);
    color: $primary-color;
    font-weight: 600;
  }

  &.back {
    margin-top: $spacing-md;
    border-top: 1px solid $border-light;
    padding-top: $spacing-md;
  }
}
</style>
