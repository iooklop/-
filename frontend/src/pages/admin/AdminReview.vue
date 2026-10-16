<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Check, X, ExternalLink } from 'lucide-vue-next'
import axios from '@/utils/axios'
import AdminNav from './AdminNav.vue'
import { formatDate, formatFileSize, statusText } from '@/utils/format'
import CoverImage from '@/components/CoverImage.vue'

interface Resource {
  id: number
  title: string
  description: string
  categoryId: number
  coverImage: string
  fileName: string
  fileSize: number
  fileType: string
  uploaderId: number
  uploaderName?: string
  status: string
  rejectReason: string
  createTime: string
}

const statusFilter = ref<'PENDING' | 'ALL' | 'APPROVED' | 'REJECTED'>('PENDING')
const list = ref<Resource[]>([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const rejectTarget = ref<Resource | null>(null)
const rejectReason = ref('')

const filters = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'ALL', label: '全部' }
]

const load = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/admin/resources', {
      params: { status: statusFilter.value, page: page.value, size: 10 }
    })
    const data = resp.data.data
    list.value = data.content || []
    totalPages.value = data.totalPages || 0
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
}

const approve = async (id: number) => {
  try {
    await axios.put(`/admin/resources/${id}/review`, { status: 'APPROVED' })
    load()
  } catch (e: any) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

const openReject = (r: Resource) => {
  rejectTarget.value = r
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('请填写驳回原因')
    return
  }
  try {
    await axios.put(`/admin/resources/${rejectTarget.value!.id}/review`, {
      status: 'REJECTED',
      reason: rejectReason.value
    })
    rejectTarget.value = null
    load()
  } catch (e: any) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

const switchFilter = (f: typeof statusFilter.value) => {
  statusFilter.value = f
  page.value = 0
  load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page container">
    <AdminNav />
    <div class="content">
      <h1 class="page-title">资源审核</h1>

      <div class="filter-row">
        <button
          v-for="f in filters"
          :key="f.value"
          class="chip"
          :class="{ active: statusFilter === f.value }"
          @click="switchFilter(f.value as typeof statusFilter)"
        >
          {{ f.label }}
        </button>
      </div>

      <div v-if="loading" class="empty">加载中...</div>

      <div v-else-if="list.length" class="review-list">
        <div v-for="r in list" :key="r.id" class="review-item card">
          <div class="thumb">
            <CoverImage :src="r.coverImage" :title="r.title" small />
          </div>
          <div class="info">
            <h3 class="title">{{ r.title }}</h3>
            <p class="desc">{{ r.description || '暂无描述' }}</p>
            <p class="meta">
              上传者：{{ r.uploaderName || `用户#${r.uploaderId}` }} ·
              {{ formatDate(r.createTime) }} ·
              {{ formatFileSize(r.fileSize) }} ·
              {{ r.fileType }}
            </p>
            <p v-if="r.status === 'REJECTED'" class="reject-reason">驳回原因：{{ r.rejectReason }}</p>
          </div>
          <div class="status-badge" :class="r.status.toLowerCase()">
            {{ statusText[r.status] || r.status }}
          </div>
          <div v-if="r.status === 'PENDING'" class="actions">
            <button class="btn btn-primary" @click="approve(r.id)">
              <Check :size="15" /> 通过
            </button>
            <button class="btn btn-outline reject" @click="openReject(r)">
              <X :size="15" /> 驳回
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty card">暂无相关资源</div>
    </div>

    <!-- 驳回弹窗 -->
    <div v-if="rejectTarget" class="modal-mask" @click.self="rejectTarget = null">
      <div class="modal card">
        <h3>驳回资源</h3>
        <p class="modal-sub">{{ rejectTarget.title }}</p>
        <textarea v-model="rejectReason" class="input" rows="3" placeholder="请填写驳回原因"></textarea>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="rejectTarget = null">取消</button>
          <button class="btn btn-primary" @click="confirmReject">确认驳回</button>
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

.filter-row {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;
}

.chip {
  padding: $spacing-xs $spacing-lg;
  border: 1px solid $border-color;
  border-radius: $radius-full;
  background: $bg-light;
  color: $text-secondary;
  font-size: $font-sm;
  transition: all $transition-fast;

  &.active {
    background: $primary-color;
    border-color: $primary-color;
    color: $text-white;
  }
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.review-item {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
  align-items: center;
}

.thumb {
  width: 100px;
  height: 72px;
  border-radius: $radius-md;
  overflow: hidden;
  background: $border-light;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: $font-base;
  margin-bottom: $spacing-xs;
}

.desc {
  font-size: $font-sm;
  color: $text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: $spacing-xs;
}

.meta {
  font-size: $font-xs;
  color: $text-light;
}

.reject-reason {
  font-size: $font-xs;
  color: $error-color;
  margin-top: $spacing-xs;
}

.status-badge {
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

.actions {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.btn-outline.reject {
  color: $error-color;
  border-color: $error-color;

  &:hover {
    background: rgba(239, 68, 68, 0.06);
  }
}

.empty {
  text-align: center;
  padding: $spacing-2xl;
  color: $text-light;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-modal;
}

.modal {
  width: 90%;
  max-width: 420px;
  padding: $spacing-lg;

  h3 {
    margin-bottom: $spacing-xs;
  }

  .modal-sub {
    font-size: $font-sm;
    color: $text-secondary;
    margin-bottom: $spacing-md;
  }

  textarea {
    width: 100%;
    resize: vertical;
    margin-bottom: $spacing-md;
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
