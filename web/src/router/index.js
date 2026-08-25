import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/diary', name: 'diary', component: () => import('../views/Diary.vue') },
  { path: '/diary/edit/:id?', name: 'diary-edit', component: () => import('../views/DiaryEdit.vue') },
  { path: '/highlight', name: 'highlight', component: () => import('../views/Highlight.vue') },
  { path: '/lottery', name: 'lottery', component: () => import('../views/Lottery.vue') },
  { path: '/lottery/add', name: 'lottery-add', component: () => import('../views/LotteryAdd.vue') },
  { path: '/chart', name: 'chart', component: () => import('../views/Chart.vue') },
  { path: '/random', name: 'random', component: () => import('../views/Random.vue') },
  { path: '/scratch', name: 'scratch', component: () => import('../views/Scratch.vue') },
  { path: '/settings', name: 'settings', component: () => import('../views/Settings.vue') }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
