<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">彩票日记</span>
    </div>

    <div v-if="store.diaries.length === 0" class="empty-state">暂无彩票日记</div>

    <div v-for="d in store.diaries" :key="d.id" class="list-item diary-card" @click="$router.push(`/diary/edit/${d.id}`)">
      <div class="diary-top">
        <span class="diary-title">{{ d.title || '未命名' }}</span>
        <button class="star-btn" :class="{ active: d.isHighlighted }" @click.stop="store.toggleHighlight(d.id)">★</button>
      </div>
      <div class="diary-date">{{ formatDateTime(d.date) }}</div>
      <div v-if="d.description" class="diary-desc">{{ d.description }}</div>
      <img v-if="d.image" :src="d.image" class="diary-img" />
    </div>

    <button class="fab" @click="$router.push('/diary/edit')">+</button>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDiaryStore } from '../stores/diary'
import { formatDateTime } from '../utils/date'

const store = useDiaryStore()
onMounted(() => store.loadDiaries())
</script>

<style scoped>
.diary-card { cursor: pointer; }
.diary-top { display: flex; justify-content: space-between; align-items: center; }
.diary-title { font-size: 16px; font-weight: 600; }
.star-btn { background: none; border: none; font-size: 24px; color: #E0E0E0; cursor: pointer; }
.star-btn.active { color: #FFD700; }
.diary-date { font-size: 12px; color: var(--on-surface-variant); margin-top: 4px; }
.diary-desc { font-size: 14px; margin-top: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.diary-img { width: 100%; max-height: 100px; object-fit: cover; border-radius: 8px; margin-top: 8px; }
</style>
