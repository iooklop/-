<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, UploadCloud, Download, Heart, Trash2, KeyRound, Camera } from 'lucide-vue-next'
import axios from '@/utils/axios'
import { useUserStore } from '@/store/user'
import ResourceCard, { type Resource } from '@/components/ResourceCard.vue'
import { formatDate, formatFileSize, statusText } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref<'profile' | 'uploads' | 'downloads' | 'favorites'>('profile')

const profile = ref({
  nickname: '',
  name: '',
  email: '',
  phone: '',
  age: null as number | null
})
const profileMsg = ref('')
const profileError = ref('')

const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordMsg = ref('')
const passwordError = ref('')

const uploads = ref<Resource[]>([])
const downloads = ref<Resource[]>([])
const favorites = ref<Resource[]>([])
const loading = ref(false)

const loadProfile = () => {
  profile.value.nickname = userStore.user?.nickname || ''
  profile.value.name = userStore.user?.name || ''
  profile.value.email = userStore.user?.email || ''
  profile.value.phone = userStore.user?.phone || ''
  profile.value.age = userStore.user?.age ?? null
}

const saveProfile = async () => {
  profileMsg.value = ''
  profileError.value = ''
  try {
    await userStore.updateProfile({
      nickname: profile.value.nickname,
      name: profile.value.name,
      email: profile.value.email,
      phone: profile.value.phone,
      age: profile.value.age
    })
    profileMsg.value = '资料已保存'
  } catch (e: any) {
    profileError.value = e.response?.data?.msg || '保存失败'
  }
}

const avatarFile = ref<File | null>(null)
const avatarPreview = ref('')

const onAvatarChange = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    avatarFile.value = input.files[0]
    avatarPreview.value = URL.createObjectURL(input.files[0])
    await uploadAvatar()
  }
}

const uploadAvatar = async () => {
  if (!avatarFile.value) return
  try {
    const fd = new FormData()
    fd.append('avatar', avatarFile.value)
    const resp = await axios.post('/users/me/avatar', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    userStore.setUser(resp.data.data)
    avatarFile.value = null
    avatarPreview.value = ''
  } catch (e: any) {
    alert(e.response?.data?.msg || '头像上传失败')
  }
}

const changePassword = async () => {
  passwordMsg.value = ''
  passwordError.value = ''
  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = '新密码至少 6 位'
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  try {
    await axios.put('/users/me/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    passwordMsg.value = '密码修改成功'
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) {
    passwordError.value = e.response?.data?.msg || '修改失败'
  }
}

const loadUploads = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/users/me/resources', { params: { page: 0, size: 50 } })
    uploads.value = resp.data.data.content || []
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
}

const loadDownloads = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/users/me/downloads')
    downloads.value = resp.data.data || []
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/favorites')
    favorites.value = resp.data.data || []
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
}

const deleteResource = async (id: number) => {
  if (!confirm('确定删除该资源吗？此操作不可恢复')) return
  try {
    await axios.delete(`/resources/${id}`)
    loadUploads()
  } catch (e: any) {
    alert(e.response?.data?.msg || '删除失败')
  }
}

const removeFavorite = async (id: number) => {
  try {
    await axios.post(`/favorites/${id}/toggle`)
    loadFavorites()
  } catch (e: any) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

const switchTab = (tab: typeof activeTab.value) => {
  activeTab.value = tab
  if (tab === 'uploads') loadUploads()
  if (tab === 'downloads') loadDownloads()
  if (tab === 'favorites') loadFavorites()
}

const goDetail = (id: number) => router.push(`/resources/${id}`)

onMounted(loadProfile)
</script>

<template>
  <div class="user-center container">
    <div class="side card">
      <div class="profile-head">
        <label class="avatar-wrap" title="点击更换头像">
          <img :src="avatarPreview || userStore.user?.avatar || '/default-avatar.svg'" class="avatar" alt="avatar" />
          <span class="avatar-cam"><Camera :size="13" /></span>
          <input type="file" accept="image/*" hidden @change="onAvatarChange" />
        </label>
        <div>
          <p class="name">{{ userStore.user?.nickname || userStore.user?.username }}</p>
          <p class="role">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</p>
        </div>
      </div>
      <nav class="tabs">
        <button class="tab" :class="{ active: activeTab === 'profile' }" @click="switchTab('profile')">
          <User :size="16" /> 我的资料
        </button>
        <button class="tab" :class="{ active: activeTab === 'uploads' }" @click="switchTab('uploads')">
          <UploadCloud :size="16" /> 我的上传
        </button>
        <button class="tab" :class="{ active: activeTab === 'downloads' }" @click="switchTab('downloads')">
          <Download :size="16" /> 我的下载
        </button>
        <button class="tab" :class="{ active: activeTab === 'favorites' }" @click="switchTab('favorites')">
          <Heart :size="16" /> 我的收藏
        </button>
      </nav>
    </div>

    <div class="content">
      <!-- 我的资料 -->
      <div v-if="activeTab === 'profile'" class="panel card">
        <h2>基本资料</h2>
        <div class="form-group">
          <label>用户名</label>
          <input class="input" :value="userStore.user?.username" disabled />
        </div>
        <div class="form-group">
          <label>昵称</label>
          <input v-model="profile.nickname" class="input" type="text" />
        </div>
        <div class="form-group">
          <label>姓名</label>
          <input v-model="profile.name" class="input" type="text" />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="profile.email" class="input" type="email" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="profile.phone" class="input" type="text" />
        </div>
        <div class="form-group">
          <label>年龄</label>
          <input v-model.number="profile.age" class="input" type="number" min="1" max="150" />
        </div>
        <p v-if="profileMsg" class="msg success">{{ profileMsg }}</p>
        <p v-if="profileError" class="msg error">{{ profileError }}</p>
        <button class="btn btn-primary" @click="saveProfile">保存资料</button>

        <h2 class="section-title">修改密码</h2>
        <div class="form-group">
          <label>旧密码</label>
          <input v-model="passwordForm.oldPassword" class="input" type="password" />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input v-model="passwordForm.newPassword" class="input" type="password" />
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input v-model="passwordForm.confirmPassword" class="input" type="password" />
        </div>
        <p v-if="passwordMsg" class="msg success">{{ passwordMsg }}</p>
        <p v-if="passwordError" class="msg error">{{ passwordError }}</p>
        <button class="btn btn-outline" @click="changePassword">
          <KeyRound :size="15" /> 修改密码
        </button>
      </div>

      <!-- 我的上传 -->
      <div v-if="activeTab === 'uploads'" class="panel">
        <h2 class="panel-title">我的上传</h2>
        <div v-if="uploads.length" class="upload-list">
          <div v-for="r in uploads" :key="r.id" class="upload-item card">
            <div class="item-info" @click="goDetail(r.id)">
              <span class="item-title">{{ r.title }}</span>
              <span class="item-meta">{{ formatDate(r.createTime) }} · {{ formatFileSize(r.fileSize) }}</span>
            </div>
            <span class="status" :class="r.status.toLowerCase()">{{ statusText[r.status] || r.status }}</span>
            <span v-if="r.status === 'REJECTED'" class="reject-reason">驳回原因：{{ r.rejectReason }}</span>
            <button class="del-btn" @click="deleteResource(r.id)">
              <Trash2 :size="15" />
            </button>
          </div>
        </div>
        <p v-else class="empty">还没有上传过资源</p>
      </div>

      <!-- 我的下载 / 收藏 -->
      <div v-if="activeTab === 'downloads' || activeTab === 'favorites'" class="panel">
        <h2 class="panel-title">{{ activeTab === 'downloads' ? '我的下载' : '我的收藏' }}</h2>
        <div v-if="(activeTab === 'downloads' ? downloads : favorites).length" class="grid">
          <div v-for="r in activeTab === 'downloads' ? downloads : favorites" :key="r.id" class="grid-item">
            <ResourceCard :resource="r" @click="goDetail" />
            <button v-if="activeTab === 'favorites'" class="unfav-btn" @click="removeFavorite(r.id)">
              取消收藏
            </button>
          </div>
        </div>
        <p v-else class="empty">暂无内容</p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.user-center {
  display: flex;
  gap: $spacing-lg;
  padding-top: $spacing-xl;
  padding-bottom: $spacing-xl;
}

.side {
  width: 240px;
  padding: $spacing-lg;
  align-self: flex-start;
  flex-shrink: 0;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding-bottom: $spacing-lg;
  border-bottom: 1px solid $border-light;
  margin-bottom: $spacing-md;

  .avatar-wrap {
    position: relative;
    display: block;
    cursor: pointer;
    flex-shrink: 0;
  }

  .avatar {
    width: 48px;
    height: 48px;
    border-radius: $radius-full;
    object-fit: cover;
    display: block;
  }

  .avatar-cam {
    position: absolute;
    right: -2px;
    bottom: -2px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    background: $primary-color;
    color: $text-white;
    border-radius: $radius-full;
    border: 2px solid $bg-light;
  }

  .name {
    font-weight: 600;
  }

  .role {
    font-size: $font-xs;
    color: $text-light;
  }
}

.tabs {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.tab {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border: none;
  border-radius: $radius-md;
  background: transparent;
  color: $text-secondary;
  font-size: $font-sm;
  text-align: left;
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
}

.content {
  flex: 1;
  min-width: 0;
}

.panel {
  padding: $spacing-lg;

  h2 {
    font-size: $font-lg;
    margin-bottom: $spacing-lg;
  }
}

.section-title {
  margin-top: $spacing-xl;
  padding-top: $spacing-lg;
  border-top: 1px solid $border-light;
}

.form-group {
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

.msg {
  font-size: $font-sm;
  margin-bottom: $spacing-sm;

  &.success {
    color: $success-color;
  }

  &.error {
    color: $error-color;
  }
}

.panel-title {
  font-size: $font-lg;
  margin-bottom: $spacing-lg;
}

.upload-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.upload-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
}

.item-info {
  flex: 1;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-title {
  font-size: $font-sm;
  font-weight: 500;

  &:hover {
    color: $primary-color;
  }
}

.item-meta {
  font-size: $font-xs;
  color: $text-light;
}

.status {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: $font-xs;
  white-space: nowrap;

  &.pending {
    background: #fff7e6;
    color: $warning-color;
  }

  &.approved {
    background: #e6fffb;
    color: $success-color;
  }

  &.rejected {
    background: #fff1f0;
    color: $error-color;
  }
}

.reject-reason {
  font-size: $font-xs;
  color: $error-color;
  max-width: 200px;
}

.del-btn {
  display: flex;
  align-items: center;
  background: transparent;
  border: none;
  color: $text-light;
  padding: $spacing-xs;

  &:hover {
    color: $error-color;
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: $spacing-lg;
}

.grid-item {
  position: relative;
}

.unfav-btn {
  margin-top: $spacing-xs;
  width: 100%;
  padding: $spacing-xs;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: $bg-light;
  color: $text-secondary;
  font-size: $font-sm;

  &:hover {
    border-color: $error-color;
    color: $error-color;
  }
}

.empty {
  color: $text-light;
  text-align: center;
  padding: $spacing-2xl;
}
</style>
