<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UploadCloud, ImagePlus, FileUp, CheckCircle } from 'lucide-vue-next'
import axios from '@/utils/axios'

const router = useRouter()

const categories = ref<{ id: number; name: string }[]>([])

const title = ref('')
const categoryId = ref<number | ''>('')
const description = ref('')
const points = ref(0)

const coverFile = ref<File | null>(null)
const coverPreview = ref('')
const resourceFile = ref<File | null>(null)

const error = ref('')
const loading = ref(false)
const success = ref(false)

const loadCategories = async () => {
  try {
    const resp = await axios.get('/categories')
    categories.value = resp.data.data || []
  } catch (e) {
    /* ignore */
  }
}

const onCoverChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    coverFile.value = input.files[0]
    coverPreview.value = URL.createObjectURL(input.files[0])
  }
}

const onFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (input.files && input.files[0]) {
    resourceFile.value = input.files[0]
  }
}

const submit = async () => {
  if (!title.value.trim()) {
    error.value = '请填写资源标题'
    return
  }
  error.value = ''
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('title', title.value)
    formData.append('description', description.value)
    if (categoryId.value !== '') formData.append('categoryId', String(categoryId.value))
    formData.append('points', String(points.value || 0))
    if (coverFile.value) formData.append('cover', coverFile.value)
    if (resourceFile.value) formData.append('file', resourceFile.value)

    await axios.post('/resources', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    success.value = true
    setTimeout(() => router.push('/user-center'), 1200)
  } catch (e: any) {
    error.value = e.response?.data?.msg || '上传失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadCategories)
</script>

<template>
  <div class="upload-page container">
    <div class="header">
      <h1><UploadCloud :size="24" class="icon" /> 上传资源</h1>
      <p>上传后需经管理员审核，通过后才会展示在首页</p>
    </div>

    <div v-if="success" class="success card">
      <CheckCircle :size="48" class="success-icon" />
      <h2>上传成功！</h2>
      <p>资源已提交审核，请耐心等待管理员处理</p>
    </div>

    <form v-else class="form card" @submit.prevent="submit">
      <div class="form-group">
        <label>资源标题 *</label>
        <input v-model="title" class="input" type="text" placeholder="请输入资源标题" />
      </div>

      <div class="form-group">
        <label>资源分类</label>
        <select v-model="categoryId" class="input">
          <option value="">请选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>资源描述</label>
        <textarea
          v-model="description"
          class="input"
          rows="5"
          placeholder="介绍一下这个资源的内容、用途等"
        ></textarea>
      </div>

      <div class="form-group">
        <label>所需积分（0 表示免费）</label>
        <input v-model.number="points" class="input" type="number" min="0" />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>封面图片</label>
          <label class="upload-box" :class="{ has: coverPreview }">
            <img v-if="coverPreview" :src="coverPreview" class="preview" alt="cover" />
            <template v-else>
              <ImagePlus :size="28" />
              <span>点击选择封面</span>
            </template>
            <input type="file" accept="image/*" hidden @change="onCoverChange" />
          </label>
        </div>

        <div class="form-group">
          <label>资源文件</label>
          <label class="upload-box">
            <template v-if="resourceFile">
              <FileUp :size="28" />
              <span class="file-name">{{ resourceFile.name }}</span>
            </template>
            <template v-else>
              <FileUp :size="28" />
              <span>点击选择文件（≤100MB）</span>
            </template>
            <input type="file" hidden @change="onFileChange" />
          </label>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <button class="btn btn-primary" type="submit" :disabled="loading">
        <UploadCloud :size="16" />
        <span>{{ loading ? '上传中...' : '提交审核' }}</span>
      </button>
    </form>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.upload-page {
  padding-top: $spacing-xl;
  padding-bottom: $spacing-xl;
  max-width: 760px;
}

.header {
  text-align: center;
  margin-bottom: $spacing-xl;

  h1 {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    font-size: $font-2xl;
    margin-bottom: $spacing-sm;

    .icon {
      color: $primary-color;
    }
  }

  p {
    color: $text-secondary;
    font-size: $font-sm;
  }
}

.form {
  padding: $spacing-xl;
}

.form-group {
  margin-bottom: $spacing-lg;

  label {
    display: block;
    font-size: $font-sm;
    font-weight: 500;
    margin-bottom: $spacing-xs;
    color: $text-secondary;
  }

  .input {
    width: 100%;
  }

  textarea {
    resize: vertical;
  }
}

.form-row {
  display: flex;
  gap: $spacing-lg;

  .form-group {
    flex: 1;
  }
}

.upload-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  height: 140px;
  border: 2px dashed $border-color;
  border-radius: $radius-lg;
  color: $text-light;
  font-size: $font-sm;
  cursor: pointer;
  transition: all $transition-fast;
  overflow: hidden;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }

  &.has {
    border-style: solid;
  }

  .preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .file-name {
    color: $primary-color;
    word-break: break-all;
    padding: 0 $spacing-sm;
  }
}

.error {
  color: $error-color;
  font-size: $font-sm;
  margin-bottom: $spacing-md;
}

.success {
  text-align: center;
  padding: $spacing-2xl;

  .success-icon {
    color: $success-color;
    margin-bottom: $spacing-md;
  }

  h2 {
    margin-bottom: $spacing-sm;
  }

  p {
    color: $text-secondary;
  }
}
</style>
