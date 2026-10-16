<script setup lang="ts">
import { Download, Eye, ThumbsUp, Heart } from 'lucide-vue-next'
import { formatDate } from '@/utils/format'
import CoverImage from '@/components/CoverImage.vue'

export interface Resource {
  id: number
  title: string
  description: string
  categoryId: number
  coverImage: string
  fileName: string
  fileSize: number
  fileType: string
  downloadCount: number
  viewCount: number
  favoriteCount: number
  likeCount: number
  commentCount: number
  points: number
  status: string
  rejectReason: string
  uploaderId: number
  uploaderName?: string
  isRecommended: boolean
  createTime: string
  updateTime: string
}

const props = defineProps<{
  resource: Resource
  categoryName?: string
}>()

const emit = defineEmits<{
  (e: 'click', id: number): void
}>()
</script>

<template>
  <div class="resource-card card" @click="emit('click', resource.id)">
    <div class="cover">
      <CoverImage :src="resource.coverImage" :title="resource.title" />
      <span v-if="resource.isRecommended" class="badge badge-rec">推荐</span>
      <span v-if="categoryName" class="badge badge-cat">{{ categoryName }}</span>
    </div>
    <div class="body">
      <h3 class="title">{{ resource.title }}</h3>
      <p class="desc">{{ resource.description || '暂无描述' }}</p>
      <div class="stats">
        <span class="stat"><Download :size="14" /> {{ resource.downloadCount }}</span>
        <span class="stat"><Eye :size="14" /> {{ resource.viewCount }}</span>
        <span class="stat"><ThumbsUp :size="14" /> {{ resource.likeCount }}</span>
        <span class="stat"><Heart :size="14" /> {{ resource.favoriteCount }}</span>
      </div>
      <div class="footer">
        <span class="date">{{ formatDate(resource.createTime) }}</span>
        <span class="size">{{ resource.fileType }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.resource-card {
  overflow: hidden;
  cursor: pointer;
  transition: all $transition-base;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-lg;

    .title {
      color: $primary-color;
    }
  }
}

.cover {
  position: relative;
  height: 160px;
  background: $border-light;
  overflow: hidden;
}

.badge {
  position: absolute;
  top: $spacing-sm;
  padding: 2px 8px;
  border-radius: $radius-sm;
  font-size: $font-xs;
  color: $text-white;
}

.badge-rec {
  left: $spacing-sm;
  background: $secondary-color;
}

.badge-cat {
  right: $spacing-sm;
  background: rgba(0, 0, 0, 0.55);
}

.body {
  padding: $spacing-md;
}

.title {
  font-size: $font-base;
  font-weight: 600;
  margin-bottom: $spacing-xs;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color $transition-fast;
}

.desc {
  font-size: $font-sm;
  color: $text-secondary;
  height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: $spacing-sm;
}

.stats {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;
}

.stat {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: $font-xs;
  color: $text-light;
}

.footer {
  display: flex;
  justify-content: space-between;
  font-size: $font-xs;
  color: $text-light;
  border-top: 1px solid $border-light;
  padding-top: $spacing-sm;
}
</style>
