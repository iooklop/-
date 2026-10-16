<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Shield, User as UserIcon } from 'lucide-vue-next'
import axios from '@/utils/axios'
import AdminNav from './AdminNav.vue'
import { formatDate } from '@/utils/format'

interface UserItem {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  role: string
  status: string
  createTime: string
}

const list = ref<UserItem[]>([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/admin/users', { params: { page: page.value, size: 10 } })
    const data = resp.data.data
    list.value = data.content || []
    totalPages.value = data.totalPages || 0
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (u: UserItem) => {
  const newStatus = u.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const action = newStatus === 'DISABLED' ? '禁用' : '启用'
  if (!confirm(`确定${action}用户「${u.username}」吗？`)) return
  try {
    await axios.put(`/admin/users/${u.id}/status`, { status: newStatus })
    load()
  } catch (e: any) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page container">
    <AdminNav />
    <div class="content">
      <h1 class="page-title">用户管理</h1>

      <div class="table card">
        <div class="table-head">
          <span>用户</span>
          <span>邮箱/手机</span>
          <span>角色</span>
          <span>状态</span>
          <span>注册时间</span>
          <span>操作</span>
        </div>
        <div v-if="loading" class="empty">加载中...</div>
        <template v-else>
          <div v-for="u in list" :key="u.id" class="table-row">
            <div class="user-cell">
              <img :src="u.avatar || '/default-avatar.svg'" class="avatar" alt="" />
              <div>
                <p class="name">{{ u.nickname || u.username }}</p>
                <p class="sub">@{{ u.username }}</p>
              </div>
            </div>
            <span class="cell">{{ u.email || u.phone || '-' }}</span>
            <span class="cell">
              <span class="role-badge" :class="u.role.toLowerCase()">
                {{ u.role === 'ADMIN' ? '管理员' : '用户' }}
              </span>
            </span>
            <span class="cell">
              <span class="status-badge" :class="u.status.toLowerCase()">
                {{ u.status === 'ACTIVE' ? '正常' : '已禁用' }}
              </span>
            </span>
            <span class="cell">{{ formatDate(u.createTime) }}</span>
            <span class="cell">
              <button
                v-if="u.role !== 'ADMIN'"
                class="btn btn-outline btn-sm"
                @click="toggleStatus(u)"
              >
                {{ u.status === 'ACTIVE' ? '禁用' : '启用' }}
              </button>
            </span>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.admin-page {
  display: flex;
  gap: $spacing-lg;
  padding-top: $spacing-xl;
  padding-bottom: $spacing-xl;
}

.content {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: $font-xl;
  margin-bottom: $spacing-lg;
}

.table {
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.6fr 1.2fr 0.7fr 0.8fr 1.2fr 0.7fr;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
}

.table-head {
  background: $bg-color;
  font-size: $font-sm;
  font-weight: 600;
  color: $text-secondary;
  border-bottom: 1px solid $border-color;
}

.table-row {
  border-bottom: 1px solid $border-light;
  font-size: $font-sm;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #fafbfc;
  }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .avatar {
    width: 36px;
    height: 36px;
    border-radius: $radius-full;
    object-fit: cover;
  }

  .name {
    font-weight: 500;
  }

  .sub {
    font-size: $font-xs;
    color: $text-light;
  }
}

.cell {
  color: $text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-badge,
.status-badge {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: $font-xs;
}

.role-badge.admin {
  background: #f0f5ff;
  color: $primary-color;
}

.role-badge.user {
  background: $border-light;
  color: $text-secondary;
}

.status-badge.active {
  background: #e6fffb;
  color: $success-color;
}

.status-badge.disabled {
  background: #fff1f0;
  color: $error-color;
}

.btn-sm {
  padding: $spacing-xs $spacing-sm;
  font-size: $font-xs;
}

.empty {
  text-align: center;
  padding: $spacing-2xl;
  color: $text-light;
}
</style>
