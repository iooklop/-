<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft,
  Download,
  ThumbsUp,
  Heart,
  Gift,
  Eye,
  MessageSquare,
  Trash2,
  FileText,
  Send
} from 'lucide-vue-next'
import QRCode from 'qrcode'
import axios from '@/utils/axios'
import { useUserStore } from '@/store/user'
import { formatDate, formatFileSize } from '@/utils/format'
import CoverImage from '@/components/CoverImage.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

interface ResourceDetail {
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
  uploaderId: number
  uploaderName?: string
  isRecommended: boolean
  createTime: string
}

interface CommentItem {
  id: number
  userId: number
  username?: string
  content: string
  parentId: number
  createTime: string
}

const resource = ref<ResourceDetail | null>(null)
const categoryName = ref('')
const liked = ref(false)
const favorited = ref(false)
const loading = ref(true)

const comments = ref<CommentItem[]>([])
const commentText = ref('')
const commentPage = ref(0)
const commentTotalPages = ref(0)

// 打赏
const showReward = ref(false)
const rewardAmount = ref('5')
const rewardInfo = ref<any>(null)
const qrDataUrl = ref('')
const rewardError = ref('')
const rewardLoading = ref(false)

const resourceId = computed(() => Number(route.params.id))
const canDeleteComment = (c: CommentItem) =>
  userStore.isAdmin || c.userId === userStore.user?.id

const loadCategory = async () => {
  try {
    const resp = await axios.get('/categories')
    const cats = resp.data.data || []
    const found = cats.find((c: any) => c.id === resource.value?.categoryId)
    categoryName.value = found?.name || ''
  } catch (e) {
    /* ignore */
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    const resp = await axios.get(`/resources/${resourceId.value}`)
    resource.value = resp.data.data
    document.title = `${resource.value?.title || '资源详情'} - 资源共享平台`
    loadCategory()
    if (userStore.isLoggedIn) {
      loadLikeState()
      loadFavoriteState()
    }
  } catch (e: any) {
    alert(e.response?.data?.msg || '资源不存在或未通过审核')
    router.push('/')
  } finally {
    loading.value = false
  }
}

const loadLikeState = async () => {
  try {
    const resp = await axios.get(`/likes/check/${resourceId.value}`)
    liked.value = !!resp.data.data
  } catch (e) {
    /* ignore */
  }
}

const loadFavoriteState = async () => {
  try {
    const resp = await axios.get(`/favorites/check/${resourceId.value}`)
    favorited.value = !!resp.data.data
  } catch (e) {
    /* ignore */
  }
}

const toggleLike = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    const resp = await axios.post(`/likes/${resourceId.value}/toggle`)
    const data = resp.data.data
    liked.value = data.liked
    if (resource.value) resource.value.likeCount = data.likeCount
  } catch (e: any) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    const resp = await axios.post(`/favorites/${resourceId.value}/toggle`)
    const data = resp.data.data
    favorited.value = data.favorited
    if (resource.value) resource.value.favoriteCount = data.favoriteCount
  } catch (e: any) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

const download = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    const resp = await axios.get(`/resources/${resourceId.value}/download`, {
      responseType: 'blob'
    })
    const blob = resp.data as Blob
    if (blob.type && blob.type.includes('json')) {
      const text = await blob.text()
      const json = JSON.parse(text)
      alert(json.msg || '下载失败')
      return
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = resource.value?.fileName || 'download'
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    loadDetail()
  } catch (e: any) {
    alert(e.response?.data?.msg || '下载失败')
  }
}

// 打赏
const openReward = () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  rewardAmount.value = '5'
  rewardInfo.value = null
  qrDataUrl.value = ''
  rewardError.value = ''
  showReward.value = true
}

const submitReward = async () => {
  const amount = parseFloat(rewardAmount.value)
  if (!amount || amount <= 0) {
    rewardError.value = '请输入有效金额'
    return
  }
  rewardError.value = ''
  rewardLoading.value = true
  try {
    const resp = await axios.post('/rewards', { resourceId: resourceId.value, amount })
    rewardInfo.value = resp.data.data
    if (rewardInfo.value && !rewardInfo.value.mock) {
      qrDataUrl.value = await QRCode.toDataURL(rewardInfo.value.codeUrl, { width: 220, margin: 1 })
    }
  } catch (e: any) {
    rewardError.value = e.response?.data?.msg || '创建订单失败'
  } finally {
    rewardLoading.value = false
  }
}

const mockPay = async () => {
  try {
    await axios.post(`/rewards/${rewardInfo.value.orderNo}/mock-pay`)
    alert('打赏成功，感谢支持！')
    showReward.value = false
  } catch (e: any) {
    alert(e.response?.data?.msg || '支付失败')
  }
}

// 评论
const loadComments = async () => {
  try {
    const resp = await axios.get(`/resources/${resourceId.value}/comments`, {
      params: { page: commentPage.value, size: 20 }
    })
    const data = resp.data.data
    comments.value = data.content || []
    commentTotalPages.value = data.totalPages || 0
  } catch (e) {
    /* ignore */
  }
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (!commentText.value.trim()) {
    alert('评论内容不能为空')
    return
  }
  try {
    await axios.post(`/resources/${resourceId.value}/comments`, {
      content: commentText.value,
      parentId: 0
    })
    commentText.value = ''
    loadComments()
    if (resource.value) resource.value.commentCount++
  } catch (e: any) {
    alert(e.response?.data?.msg || '评论失败')
  }
}

const deleteComment = async (id: number) => {
  try {
    await axios.delete(`/comments/${id}`)
    loadComments()
    if (resource.value) resource.value.commentCount = Math.max(0, resource.value.commentCount - 1)
  } catch (e: any) {
    alert(e.response?.data?.msg || '删除失败')
  }
}

onMounted(() => {
  loadDetail()
  loadComments()
})
</script>

<template>
  <div class="detail-page container">
    <button class="back-btn" @click="router.back()">
      <ArrowLeft :size="16" /> 返回
    </button>

    <div v-if="loading" class="loading">加载中...</div>

    <template v-else-if="resource">
      <div class="main card">
        <div class="cover">
          <CoverImage :src="resource.coverImage" :title="resource.title" />
        </div>

        <div class="info">
          <h1 class="title">{{ resource.title }}</h1>

          <div class="meta">
            <span v-if="resource.uploaderName" class="meta-item">作者：{{ resource.uploaderName }}</span>
            <span v-if="categoryName" class="meta-item">分类：{{ categoryName }}</span>
            <span class="meta-item">发布于 {{ formatDate(resource.createTime) }}</span>
          </div>

          <div class="stats">
            <span class="stat"><Eye :size="15" /> {{ resource.viewCount }} 浏览</span>
            <span class="stat"><Download :size="15" /> {{ resource.downloadCount }} 下载</span>
            <span class="stat"><ThumbsUp :size="15" /> {{ resource.likeCount }} 点赞</span>
            <span class="stat"><Heart :size="15" /> {{ resource.favoriteCount }} 收藏</span>
            <span class="stat"><MessageSquare :size="15" /> {{ resource.commentCount }} 评论</span>
          </div>

          <div class="file-info">
            <FileText :size="16" class="file-icon" />
            <span class="file-name">{{ resource.fileName || '（示例数据，未上传文件）' }}</span>
            <span v-if="resource.fileSize" class="file-size">{{ formatFileSize(resource.fileSize) }}</span>
          </div>

          <div class="actions">
            <button class="btn btn-secondary" @click="download">
              <Download :size="16" /> 立即下载
            </button>
            <button class="btn btn-outline" :class="{ active: liked }" @click="toggleLike">
              <ThumbsUp :size="16" /> {{ liked ? '已点赞' : '点赞' }}
            </button>
            <button class="btn btn-outline" :class="{ active: favorited }" @click="toggleFavorite">
              <Heart :size="16" /> {{ favorited ? '已收藏' : '收藏' }}
            </button>
            <button class="btn btn-outline reward-btn" @click="openReward">
              <Gift :size="16" /> 打赏
            </button>
          </div>
        </div>
      </div>

      <div class="description card">
        <h2>资源描述</h2>
        <p>{{ resource.description || '暂无描述' }}</p>
      </div>

      <div class="comments card">
        <h2>评论（{{ resource.commentCount }}）</h2>

        <div class="comment-input">
          <textarea
            v-model="commentText"
            class="input"
            rows="3"
            placeholder="说点什么..."
          ></textarea>
          <button class="btn btn-primary" @click="submitComment">
            <Send :size="15" /> 发表评论
          </button>
        </div>

        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-head">
              <span class="comment-user">{{ c.username || `用户#${c.userId}` }}</span>
              <span class="comment-time">{{ formatDate(c.createTime) }}</span>
            </div>
            <p class="comment-content">{{ c.content }}</p>
            <button
              v-if="canDeleteComment(c)"
              class="comment-del"
              @click="deleteComment(c.id)"
            >
              <Trash2 :size="13" /> 删除
            </button>
          </div>
        </div>
        <p v-else class="no-comment">还没有评论，来抢沙发～</p>
      </div>
    </template>

    <!-- 打赏弹窗 -->
    <div v-if="showReward" class="modal-mask" @click.self="showReward = false">
      <div class="modal card">
        <h3>打赏作者</h3>
        <p class="modal-sub">支持作者，让优质资源持续更新</p>

        <template v-if="!rewardInfo">
          <div class="amount-row">
            <span class="amount-label">金额（元）</span>
            <input v-model="rewardAmount" class="input" type="number" min="0.01" step="0.01" />
          </div>
          <div class="quick-amounts">
            <button
              v-for="a in ['2', '5', '10', '20']"
              :key="a"
              class="chip"
              :class="{ active: rewardAmount === a }"
              @click="rewardAmount = a"
            >
              {{ a }} 元
            </button>
          </div>
          <p v-if="rewardError" class="error">{{ rewardError }}</p>
          <button class="btn btn-primary btn-block" :disabled="rewardLoading" @click="submitReward">
            {{ rewardLoading ? '创建订单中...' : '生成支付二维码' }}
          </button>
        </template>

        <template v-else>
          <p class="pay-amount">支付金额：¥{{ rewardInfo.amount }}</p>
          <div v-if="rewardInfo.mock" class="mock-pay">
            <p class="mock-tip">当前为模拟支付模式（未接入真实微信支付）</p>
            <button class="btn btn-secondary btn-block" @click="mockPay">模拟支付成功</button>
          </div>
          <template v-else>
            <img v-if="qrDataUrl" :src="qrDataUrl" class="qr" alt="支付二维码" />
            <p class="pay-tip">请使用微信扫码支付</p>
          </template>
        </template>

        <button class="modal-close" @click="showReward = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/style/variables.scss' as *;

.detail-page {
  padding-top: $spacing-lg;
  padding-bottom: $spacing-xl;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  background: transparent;
  border: none;
  color: $text-secondary;
  font-size: $font-sm;
  margin-bottom: $spacing-md;

  &:hover {
    color: $primary-color;
  }
}

.loading {
  text-align: center;
  padding: $spacing-2xl;
  color: $text-light;
}

.main {
  display: flex;
  gap: $spacing-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.cover {
  width: 320px;
  height: 220px;
  border-radius: $radius-lg;
  overflow: hidden;
  background: $border-light;
  flex-shrink: 0;
}

.info {
  flex: 1;
}

.title {
  font-size: $font-2xl;
  margin-bottom: $spacing-sm;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.meta-item {
  font-size: $font-sm;
  color: $text-secondary;
}

.stats {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-lg;
  padding: $spacing-md 0;
  border-top: 1px solid $border-light;
  border-bottom: 1px solid $border-light;
  margin-bottom: $spacing-md;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: $font-sm;
  color: $text-secondary;
}

.file-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  background: $bg-color;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  margin-bottom: $spacing-lg;
  font-size: $font-sm;

  .file-icon {
    color: $primary-color;
  }

  .file-name {
    flex: 1;
    color: $text-primary;
  }

  .file-size {
    color: $text-light;
  }
}

.actions {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.btn-outline.active {
  background: rgba(30, 128, 255, 0.08);
  color: $primary-color;
  border-color: $primary-color;
}

.reward-btn {
  color: $accent-gold;
  border-color: $accent-gold;

  &:hover {
    background: rgba(245, 158, 11, 0.06);
  }
}

.description {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;

  h2 {
    font-size: $font-lg;
    margin-bottom: $spacing-md;
  }

  p {
    color: $text-secondary;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.comments {
  padding: $spacing-lg;

  h2 {
    font-size: $font-lg;
    margin-bottom: $spacing-md;
  }
}

.comment-input {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin-bottom: $spacing-lg;

  textarea {
    width: 100%;
    resize: vertical;
  }

  .btn {
    align-self: flex-end;
  }
}

.comment-item {
  padding: $spacing-md 0;
  border-bottom: 1px solid $border-light;
  position: relative;

  &:last-child {
    border-bottom: none;
  }
}

.comment-head {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-xs;
}

.comment-user {
  font-weight: 600;
  font-size: $font-sm;
  color: $primary-color;
}

.comment-time {
  font-size: $font-xs;
  color: $text-light;
}

.comment-content {
  font-size: $font-sm;
  color: $text-primary;
  line-height: 1.6;
}

.comment-del {
  position: absolute;
  top: $spacing-md;
  right: 0;
  display: flex;
  align-items: center;
  gap: 3px;
  background: transparent;
  border: none;
  color: $text-light;
  font-size: $font-xs;

  &:hover {
    color: $error-color;
  }
}

.no-comment {
  color: $text-light;
  font-size: $font-sm;
  text-align: center;
  padding: $spacing-lg;
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
  max-width: 360px;
  padding: $spacing-lg;
  text-align: center;

  h3 {
    font-size: $font-xl;
    margin-bottom: $spacing-xs;
  }

  .modal-sub {
    font-size: $font-sm;
    color: $text-secondary;
    margin-bottom: $spacing-lg;
  }
}

.amount-row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;

  .amount-label {
    font-size: $font-sm;
    color: $text-secondary;
    white-space: nowrap;
  }

  .input {
    flex: 1;
  }
}

.quick-amounts {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.chip {
  flex: 1;
  padding: $spacing-xs;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: $bg-light;
  font-size: $font-sm;
  color: $text-secondary;
  transition: all $transition-fast;

  &.active {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba(30, 128, 255, 0.06);
  }
}

.error {
  color: $error-color;
  font-size: $font-sm;
  margin-bottom: $spacing-sm;
}

.btn-block {
  width: 100%;
}

.pay-amount {
  font-size: $font-lg;
  font-weight: 600;
  color: $secondary-color;
  margin-bottom: $spacing-md;
}

.qr {
  width: 220px;
  height: 220px;
  margin: 0 auto $spacing-sm;
  display: block;
}

.pay-tip {
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: $spacing-md;
}

.mock-pay {
  .mock-tip {
    font-size: $font-sm;
    color: $warning-color;
    margin-bottom: $spacing-md;
  }
}

.modal-close {
  margin-top: $spacing-md;
  background: transparent;
  border: none;
  color: $text-light;
  font-size: $font-sm;

  &:hover {
    color: $text-primary;
  }
}
</style>
