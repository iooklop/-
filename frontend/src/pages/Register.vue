<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserPlus, Camera } from 'lucide-vue-next'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  name: '',
  age: null as number | null,
  phone: '',
  email: ''
})
const avatarFile = ref<File | null>(null)
const avatarPreview = ref('')
const error = ref('')
const loading = ref(false)

const onAvatarChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    avatarFile.value = input.files[0]
    avatarPreview.value = URL.createObjectURL(input.files[0])
  }
}

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) {
    error.value = '请填写用户名和密码'
    return
  }
  if (form.value.password.length < 6) {
    error.value = '密码长度至少 6 位'
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  if (form.value.age !== null && (form.value.age < 1 || form.value.age > 150)) {
    error.value = '请输入有效的年龄'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const fd = new FormData()
    fd.append('username', form.value.username)
    fd.append('password', form.value.password)
    if (form.value.nickname) fd.append('nickname', form.value.nickname)
    if (form.value.name) fd.append('name', form.value.name)
    if (form.value.age !== null) fd.append('age', String(form.value.age))
    if (form.value.phone) fd.append('phone', form.value.phone)
    if (form.value.email) fd.append('email', form.value.email)
    if (avatarFile.value) fd.append('avatar', avatarFile.value)

    await userStore.register(fd)
    router.push('/login')
  } catch (e: any) {
    error.value = e.response?.data?.msg || '注册失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2>注册</h2>
      <p class="subtitle">加入我们，分享你的优质资源</p>

      <label class="avatar-upload" title="点击上传头像">
        <img :src="avatarPreview || '/default-avatar.svg'" class="avatar" alt="avatar" />
        <span class="avatar-cam"><Camera :size="14" /></span>
        <input type="file" accept="image/*" hidden @change="onAvatarChange" />
      </label>
      <p class="avatar-tip">点击上传头像（可选）</p>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名 *</label>
          <input v-model="form.username" class="input" type="text" placeholder="请输入用户名" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>密码 *</label>
            <input v-model="form.password" class="input" type="password" placeholder="至少 6 位" />
          </div>
          <div class="form-group">
            <label>确认密码 *</label>
            <input v-model="form.confirmPassword" class="input" type="password" placeholder="再次输入密码" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>昵称</label>
            <input v-model="form.nickname" class="input" type="text" placeholder="你的昵称（默认同用户名）" />
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input v-model="form.name" class="input" type="text" placeholder="真实姓名" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>年龄</label>
            <input v-model.number="form.age" class="input" type="number" min="1" max="150" placeholder="选填" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="form.email" class="input" type="email" placeholder="you@example.com" />
          </div>
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" class="input" type="text" placeholder="选填" />
        </div>

        <p v-if="error" class="error">{{ error }}</p>

        <button class="btn btn-primary btn-block" type="submit" :disabled="loading">
          <UserPlus :size="16" />
          <span>{{ loading ? '注册中...' : '注册' }}</span>
        </button>
      </form>

      <p class="switch">
        已有账号？<router-link to="/login" class="link">去登录</router-link>
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
  max-width: 460px;
  padding: $spacing-xl;
  text-align: center;

  h2 {
    font-size: $font-2xl;
    margin-bottom: $spacing-xs;
  }

  .subtitle {
    color: $text-secondary;
    margin-bottom: $spacing-md;
    font-size: $font-sm;
  }
}

.avatar-upload {
  position: relative;
  display: inline-block;
  cursor: pointer;

  .avatar {
    width: 76px;
    height: 76px;
    border-radius: $radius-full;
    object-fit: cover;
    display: block;
    border: 2px solid $border-light;
  }

  .avatar-cam {
    position: absolute;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    background: $primary-color;
    color: $text-white;
    border-radius: $radius-full;
    border: 2px solid $bg-light;
  }
}

.avatar-tip {
  font-size: $font-xs;
  color: $text-light;
  margin: $spacing-xs 0 $spacing-md;
}

.form-row {
  display: flex;
  gap: $spacing-md;

  .form-group {
    flex: 1;
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
