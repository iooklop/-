<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Upload, User, LogOut, ShieldCheck, Boxes } from 'lucide-vue-next'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const keyword = ref('')

watch(
  () => route.query.keyword,
  (val) => {
    keyword.value = (val as string) || ''
  },
  { immediate: true }
)

const handleSearch = () => {
  router.push({ path: '/', query: keyword.value ? { keyword: keyword.value } : {} })
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <header class="navbar">
    <div class="container navbar-inner">
      <router-link to="/" class="logo">
        <Boxes class="logo-icon" :size="22" />
        <span>资源共享</span>
      </router-link>

      <div class="search-box">
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          placeholder="搜索你想要的资源..."
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">
          <Search :size="16" />
        </button>
      </div>

      <nav class="nav-links">
        <router-link to="/" class="nav-link" :class="{ active: route.path === '/' }">首页</router-link>
        <router-link to="/upload" class="nav-link nav-upload">
          <Upload :size="15" />
          <span>上传资源</span>
        </router-link>

        <template v-if="userStore.isLoggedIn">
          <router-link v-if="userStore.isAdmin" to="/admin" class="nav-link">
            <ShieldCheck :size="15" />
            <span>管理后台</span>
          </router-link>
          <router-link to="/user-center" class="nav-link user-chip">
            <img :src="userStore.user?.avatar || '/default-avatar.svg'" class="avatar" alt="avatar" />
            <span>{{ userStore.user?.nickname || userStore.user?.username }}</span>
          </router-link>
          <button class="nav-link logout-btn" @click="handleLogout">
            <LogOut :size="15" />
          </button>
        </template>

        <template v-else>
          <router-link to="/login" class="btn btn-outline btn-sm">登录</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: $bg-light;
  border-bottom: 1px solid $border-color;
  z-index: $z-sticky;
}

.navbar-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.logo {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-lg;
  font-weight: 700;
  color: $primary-color;
  white-space: nowrap;

  .logo-icon {
    color: $primary-color;
  }
}

.search-box {
  flex: 1;
  max-width: 420px;
  display: flex;
  align-items: center;
  background: $bg-color;
  border: 1px solid $border-color;
  border-radius: $radius-full;
  padding: 0 $spacing-sm;
  transition: border-color $transition-fast;

  &:focus-within {
    border-color: $primary-color;
  }
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: $spacing-sm $spacing-md;
  font-size: $font-sm;
  outline: none;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: $radius-full;
  background: $primary-color;
  color: $text-white;
  transition: background $transition-fast;

  &:hover {
    background: $primary-dark;
  }
}

.nav-links {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-left: auto;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  font-size: $font-sm;
  color: $text-secondary;
  transition: all $transition-fast;
  white-space: nowrap;

  &:hover {
    color: $primary-color;
    background: rgba(30, 128, 255, 0.06);
  }

  &.active {
    color: $primary-color;
    font-weight: 600;
  }
}

.nav-upload {
  background: $secondary-color;
  color: $text-white !important;

  &:hover {
    background: $secondary-dark !important;
  }
}

.user-chip {
  .avatar {
    width: 24px;
    height: 24px;
    border-radius: $radius-full;
    object-fit: cover;
  }
}

.logout-btn {
  border: none;
  background: transparent;
  color: $text-light;

  &:hover {
    color: $error-color;
    background: rgba(239, 68, 68, 0.06);
  }
}

.btn-sm {
  padding: $spacing-xs $spacing-md;
  font-size: $font-sm;
}
</style>
