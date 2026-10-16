<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  src?: string | null
  title: string
  small?: boolean
}>()

// 与品牌主色一致的封面色板，按标题哈希取色，保证同一资源颜色稳定
const gradients = [
  'linear-gradient(135deg, #1e80ff 0%, #0f5cd6 100%)',
  'linear-gradient(135deg, #fc5531 0%, #e03e1d 100%)',
  'linear-gradient(135deg, #10b981 0%, #0d9468 100%)',
  'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
  'linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%)',
  'linear-gradient(135deg, #06b6d4 0%, #0e7490 100%)',
  'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
  'linear-gradient(135deg, #64748b 0%, #475569 100%)'
]

function hashString(str: string): number {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = (hash * 31 + str.charCodeAt(i)) >>> 0
  }
  return hash
}

const background = computed(() => gradients[hashString(props.title) % gradients.length])
</script>

<template>
  <img v-if="src" :src="src" :alt="title" loading="lazy" class="cover-img" />
  <div v-else class="cover-fallback" :class="{ 'is-small': small }" :style="{ background }">
    <span class="cover-title">{{ title }}</span>
  </div>
</template>

<style scoped lang="scss">
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;

  &.is-small {
    padding: 6px;

    .cover-title {
      font-size: 0.7rem;
      line-height: 1.2;
      -webkit-line-clamp: 2;
    }
  }
}

.cover-title {
  color: #fff;
  font-weight: 600;
  font-size: 0.95rem;
  line-height: 1.4;
  text-align: center;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}
</style>
