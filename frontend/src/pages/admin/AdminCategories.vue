<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Pencil, Trash2 } from 'lucide-vue-next'
import axios from '@/utils/axios'
import AdminNav from './AdminNav.vue'

interface Category {
  id: number
  name: string
  parentId: number
  icon: string
  sortOrder: number
  status: string
}

const list = ref<Category[]>([])
const loading = ref(false)

const showModal = ref(false)
const editing = ref<Category | null>(null)
const form = ref({ name: '', sortOrder: 0 })

const load = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/admin/categories')
    list.value = resp.data.data || []
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editing.value = null
  form.value = { name: '', sortOrder: 0 }
  showModal.value = true
}

const openEdit = (c: Category) => {
  editing.value = c
  form.value = { name: c.name, sortOrder: c.sortOrder }
  showModal.value = true
}

const save = async () => {
  if (!form.value.name.trim()) {
    alert('请填写分类名称')
    return
  }
  try {
    if (editing.value) {
      await axios.put(`/admin/categories/${editing.value.id}`, form.value)
    } else {
      await axios.post('/admin/categories', form.value)
    }
    showModal.value = false
    load()
  } catch (e: any) {
    alert(e.response?.data?.msg || '保存失败')
  }
}

const remove = async (c: Category) => {
  if (!confirm(`确定删除分类「${c.name}」吗？`)) return
  try {
    await axios.delete(`/admin/categories/${c.id}`)
    load()
  } catch (e: any) {
    alert(e.response?.data?.msg || '删除失败（可能已有资源关联该分类）')
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page container">
    <AdminNav />
    <div class="content">
      <div class="head">
        <h1 class="page-title">分类管理</h1>
        <button class="btn btn-primary" @click="openCreate">
          <Plus :size="16" /> 新增分类
        </button>
      </div>

      <div class="table card">
        <div class="table-head">
          <span>ID</span>
          <span>分类名称</span>
          <span>排序</span>
          <span>状态</span>
          <span>操作</span>
        </div>
        <div v-if="loading" class="empty">加载中...</div>
        <template v-else>
          <div v-for="c in list" :key="c.id" class="table-row">
            <span class="cell">{{ c.id }}</span>
            <span class="cell name">{{ c.name }}</span>
            <span class="cell">{{ c.sortOrder }}</span>
            <span class="cell">
              <span class="status-badge" :class="c.status.toLowerCase()">
                {{ c.status === 'ACTIVE' ? '启用' : '停用' }}
              </span>
            </span>
            <span class="cell actions">
              <button class="icon-btn" @click="openEdit(c)">
                <Pencil :size="15" />
              </button>
              <button class="icon-btn danger" @click="remove(c)">
                <Trash2 :size="15" />
              </button>
            </span>
          </div>
        </template>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="showModal = false">
      <div class="modal card">
        <h3>{{ editing ? '编辑分类' : '新增分类' }}</h3>
        <div class="form-group">
          <label>分类名称</label>
          <input v-model="form.name" class="input" type="text" placeholder="请输入分类名称" />
        </div>
        <div class="form-group">
          <label>排序（数值越小越靠前）</label>
          <input v-model.number="form.sortOrder" class="input" type="number" />
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="save">保存</button>
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

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
}

.page-title {
  font-size: $font-xl;
}

.table {
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 0.6fr 2fr 0.8fr 0.8fr 1fr;
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
}

.cell {
  color: $text-secondary;

  &.name {
    color: $text-primary;
    font-weight: 500;
  }
}

.status-badge {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: $font-xs;

  &.active {
    background: #e6fffb;
    color: $success-color;
  }

  &.disabled {
    background: $border-light;
    color: $text-light;
  }
}

.actions {
  display: flex;
  gap: $spacing-xs;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: $bg-light;
  color: $text-secondary;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }

  &.danger:hover {
    border-color: $error-color;
    color: $error-color;
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
  max-width: 400px;
  padding: $spacing-lg;

  h3 {
    margin-bottom: $spacing-lg;
  }
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

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
}
</style>
