<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Users, Boxes, Clock, CheckCircle, XCircle, Download, Eye } from 'lucide-vue-next'
import axios from '@/utils/axios'
import AdminNav from './AdminNav.vue'

interface Stats {
  userCount: number
  resourceCount: number
  pendingCount: number
  approvedCount: number
  rejectedCount: number
  totalDownloads: number
  totalViews: number
}

const stats = ref<Stats | null>(null)

const load = async () => {
  try {
    const resp = await axios.get('/admin/statistics')
    stats.value = resp.data.data
  } catch (e) {
    /* ignore */
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page container">
    <AdminNav />
    <div class="content">
      <h1 class="page-title">数据概览</h1>

      <div v-if="stats" class="stat-grid">
        <div class="stat-card card">
          <div class="icon-box blue"><Users :size="22" /></div>
          <div>
            <p class="value">{{ stats.userCount }}</p>
            <p class="label">注册用户</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="icon-box green"><Boxes :size="22" /></div>
          <div>
            <p class="value">{{ stats.resourceCount }}</p>
            <p class="label">资源总数</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="icon-box amber"><Clock :size="22" /></div>
          <div>
            <p class="value">{{ stats.pendingCount }}</p>
            <p class="label">待审核</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="icon-box cyan"><CheckCircle :size="22" /></div>
          <div>
            <p class="value">{{ stats.approvedCount }}</p>
            <p class="label">已通过</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="icon-box red"><XCircle :size="22" /></div>
          <div>
            <p class="value">{{ stats.rejectedCount }}</p>
            <p class="label">已驳回</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="icon-box indigo"><Download :size="22" /></div>
          <div>
            <p class="value">{{ stats.totalDownloads }}</p>
            <p class="label">总下载量</p>
          </div>
        </div>
        <div class="stat-card card">
          <div class="icon-box purple"><Eye :size="22" /></div>
          <div>
            <p class="value">{{ stats.totalViews }}</p>
            <p class="label">总浏览量</p>
          </div>
        </div>
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

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: $spacing-md;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-white;

  &.blue {
    background: $primary-color;
  }
  &.green {
    background: $success-color;
  }
  &.amber {
    background: $warning-color;
  }
  &.cyan {
    background: #06b6d4;
  }
  &.red {
    background: $error-color;
  }
  &.indigo {
    background: #6366f1;
  }
  &.purple {
    background: #a855f7;
  }
}

.value {
  font-size: $font-2xl;
  font-weight: 700;
}

.label {
  font-size: $font-sm;
  color: $text-secondary;
}
</style>
