import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue')
  },
  {
    path: '/resources/:id',
    name: 'ResourceDetail',
    component: () => import('@/pages/ResourceDetail.vue')
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/pages/Upload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/Register.vue')
  },
  {
    path: '/user-center',
    name: 'UserCenter',
    component: () => import('@/pages/UserCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/pages/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/review',
    name: 'AdminReview',
    component: () => import('@/pages/admin/AdminReview.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/pages/admin/AdminUsers.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/categories',
    name: 'AdminCategories',
    component: () => import('@/pages/admin/AdminCategories.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
    return
  }

  next()
})

export default router
