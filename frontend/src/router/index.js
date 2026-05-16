import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard/index.vue')
  },
  {
    path: '/order',
    name: 'Order',
    component: () => import('@/views/Order/index.vue')
  },
  {
    path: '/vehicle',
    name: 'Vehicle',
    component: () => import('@/views/Vehicle/index.vue')
  },
  {
    path: '/driver',
    name: 'Driver',
    component: () => import('@/views/Driver/index.vue')
  },
  {
    path: '/dispatch',
    name: 'Dispatch',
    component: () => import('@/views/Dispatch/index.vue')
  },
  {
    path: '/transport',
    name: 'Transport',
    component: () => import('@/views/Transport/index.vue')
  },
  {
    path: '/warning',
    name: 'Warning',
    component: () => import('@/views/Warning/index.vue')
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/Statistics/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
