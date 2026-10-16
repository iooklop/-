<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
import Footer from '@/components/Footer.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
userStore.loadFromStorage()
</script>

<template>
  <div class="app-container">
    <video class="bg-video" autoplay muted loop playsinline>
      <source src="/bg-video.mp4" type="video/mp4" />
    </video>
    <NavBar />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  position: relative;
}

.bg-video {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: -1;
  pointer-events: none;
}

.main-content {
  padding-top: 64px;
  min-height: calc(100vh - 64px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
