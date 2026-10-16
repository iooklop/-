<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { LogIn } from 'lucide-vue-next'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await userStore.login(username.value, password.value)
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.msg || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2>登录</h2>
      <p class="subtitle">欢迎回来，继续共享知识</p>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" class="input" type="text" placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" class="input" type="password" placeholder="请输入密码" />
        </div>

        <p v-if="error" class="error">{{ error }}</p>

        <button class="btn btn-primary btn-block" type="submit" :disabled="loading">
          <LogIn :size="16" />
          <span>{{ loading ? '登录中...' : '登录' }}</span>
        </button>
      </form>

      <p class="switch">
        还没有账号？<router-link to="/register" class="link">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 160px);
  padding: $spacing-xl 0;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: $spacing-xl;
  text-align: center;

  h2 {
    font-size: $font-2xl;
    margin-bottom: $spacing-xs;
  }

  .subtitle {
    color: $text-secondary;
    margin-bottom: $spacing-lg;
    font-size: $font-sm;
  }
}

.form-group {
  text-align: left;
  margin-bottom: $spacing-md;

  label {
    display: block;
    font-size: $font-sm;
    margin-bottom: $spacing-xs;
    color: $text-secondary;
  }

  .input {
    width: 100%;
  }
}

.error {
  color: $error-color;
  font-size: $font-sm;
  margin-bottom: $spacing-md;
  text-align: left;
}

.btn-block {
  width: 100%;
  padding: $spacing-sm;
  font-size: $font-base;
}

.switch {
  margin-top: $spacing-lg;
  font-size: $font-sm;
  color: $text-secondary;

  .link {
    color: $primary-color;
    font-weight: 500;
  }
}
</style>
