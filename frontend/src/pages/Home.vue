<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChevronLeft, ChevronRight, Sparkles } from 'lucide-vue-next'
import axios from '@/utils/axios'
import ResourceCard, { type Resource } from '@/components/ResourceCard.vue'

const router = useRouter()
const route = useRoute()

const resources = ref<Resource[]>([])
const categories = ref<{ id: number; name: string }[]>([])
const total = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const keyword = ref('')
const categoryId = ref<number | ''>('')
const sort = ref<'newest' | 'downloads' | 'views' | 'likes'>('newest')
const page = ref(0)
const size = 12

const sortOptions = [
  { value: 'newest', label: '最新发布' },
  { value: 'downloads', label: '最多下载' },
  { value: 'views', label: '最多浏览' },
  { value: 'likes', label: '最多点赞' }
]

const categoryMap = computed(() => {
  const map: Record<number, string> = {}
  categories.value.forEach((c) => (map[c.id] = c.name))
  return map
})

const loadCategories = async () => {
  try {
    const resp = await axios.get('/categories')
    categories.value = resp.data.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const loadResources = async () => {
  loading.value = true
  try {
    const resp = await axios.get('/resources', {
      params: {
        page: page.value,
        size,
        keyword: keyword.value || undefined,
        categoryId: categoryId.value || undefined,
        sort: sort.value
      }
    })
    const data = resp.data.data
    resources.value = data.content || []
    total.value = data.totalElements || 0
    totalPages.value = data.totalPages || 0
  } catch (e) {
    console.error('加载资源失败', e)
  } finally {
    loading.value = false
  }
}

const goPage = (p: number) => {
  if (p < 0 || p >= totalPages.value) return
  page.value = p
  loadResources()
}

const selectCategory = (id: number | '') => {
  categoryId.value = id
  page.value = 0
  loadResources()
}

const changeSort = () => {
  page.value = 0
  loadResources()
}

const goDetail = (id: number) => {
  router.push(`/resources/${id}`)
}

watch(
  () => route.query.keyword,
  (val) => {
    keyword.value = (val as string) || ''
    page.value = 0
    loadResources()
  }
)

onMounted(() => {
  keyword.value = (route.query.keyword as string) || ''
  loadCategories()
  loadResources()
})
</script>

<template>
  <div class="home container">
    <div class="hero">
      <h1><Sparkles :size="26" class="hero-icon" /> 发现优质资源</h1>
      <p>共享知识，传递价值 —— 上传、审核、检索、下载，一站搞定</p>
    </div>

    <div class="filters card">
      <div class="categories">
        <span class="filter-label">分类：</span>
        <button
          class="chip"
          :class="{ active: categoryId === '' }"
          @click="selectCategory('')"
        >
          全部
        </button>
        <button
          v-for="cat in categories"
          :key="cat.id"
          class="chip"
          :class="{ active: categoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          {{ cat.name }}
        </button>
      </div>
      <div class="sort-row">
        <span class="filter-label">排序：</span>
        <button
          v-for="opt in sortOptions"
          :key="opt.value"
          class="chip"
          :class="{ active: sort === opt.value }"
          @click="sort = opt.value as typeof sort; changeSort()"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="resources.length" class="grid">
      <ResourceCard
        v-for="r in resources"
        :key="r.id"
        :resource="r"
        :category-name="categoryMap[r.categoryId]"
        @click="goDetail"
      />
    </div>

    <div v-else class="empty card">
      <p>没有找到相关资源，换个关键词或分类试试吧～</p>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button class="page-btn" :disabled="page === 0" @click="goPage(page - 1)">
        <ChevronLeft :size="16" />
      </button>
      <span class="page-info">{{ page + 1 }} / {{ totalPages }}</span>
      <button class="page-btn" :disabled="page >= totalPages - 1" @click="goPage(page + 1)">
        <ChevronRight :size="16" />
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.home {
  padding-top: $spacing-xl;
  padding-bottom: $spacing-xl;
}

.hero {
  text-align: center;
  margin-bottom: $spacing-xl;

  h1 {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    font-size: $font-3xl;
    color: $text-primary;
    margin-bottom: $spacing-sm;
  }

  .hero-icon {
    color: $primary-color;
  }

  p {
    color: $text-secondary;
  }
}

.filters {
  padding: $spacing-md;
  margin-bottom: $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.categories,
.sort-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $spacing-xs;
}

.filter-label {
  font-size: $font-sm;
  color: $text-secondary;
  white-space: nowrap;
}

.chip {
  padding: 4px 14px;
  border: 1px solid $border-color;
  border-radius: $radius-full;
  background: $bg-light;
  font-size: $font-sm;
  color: $text-secondary;
  transition: all $transition-fast;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }

  &.active {
    background: $primary-color;
    border-color: $primary-color;
    color: $text-white;
  }
}

.loading,
.empty {
  text-align: center;
  padding: $spacing-2xl;
  color: $text-light;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: $spacing-lg;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;
  margin-top: $spacing-xl;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: $bg-light;
  color: $text-secondary;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    border-color: $primary-color;
    color: $primary-color;
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: $font-sm;
  color: $text-secondary;
}
</style>
