<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">高光时刻</span>
    </div>

    <div v-if="highlights.length === 0" class="empty-state">
      暂无高光时刻<br />在彩票日记中收藏即可显示
    </div>

    <div v-for="d in highlights" :key="d.id" class="list-item highlight-card" @click="$router.push(`/diary/edit/${d.id}`)">
      <div class="hl-top">
        <span class="hl-title">{{ d.title || '未命名日记' }}</span>
        <button class="star-btn" @click.stop="store.toggleHighlight(d.id)">★</button>
      </div>
      <div class="hl-date">{{ formatDateTime(d.date) }}</div>
      <div v-if="d.description" class="hl-desc">{{ d.description }}</div>
      <img v-if="d.image" :src="d.image" class="hl-img" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useDiaryStore } from '../stores/diary'
import { formatDateTime } from '../utils/date'

const store = useDiaryStore()
onMounted(() => store.loadDiaries())
const highlights = computed(() => store.diaries.filter(d => d.isHighlighted))
</script>

<style scoped>
.highlight-card { cursor: pointer; }
.hl-top { display: flex; justify-content: space-between; align-items: center; }
.hl-title { font-size: 16px; font-weight: 600; }
.star-btn { background: none; border: none; font-size: 24px; color: #FFD700; cursor: pointer; }
.hl-date { font-size: 12px; color: var(--on-surface-variant); margin-top: 4px; }
.hl-desc { font-size: 14px; margin-top: 8px; color: var(--on-surface); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hl-img { width: 100%; max-height: 120px; object-fit: cover; border-radius: 8px; margin-top: 8px; }
</style>
