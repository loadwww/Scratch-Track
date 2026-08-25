<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">中奖记录</span>
    </div>

    <div class="filter-bar">
      <select v-model="selectedMonth" class="select-field">
        <option value="">全部月份</option>
        <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
      </select>
      <select v-model="sortBy" class="select-field">
        <option value="date">按时间</option>
        <option value="invest">按投入</option>
        <option value="profit">按盈利</option>
        <option value="ratio">按盈利比</option>
      </select>
    </div>

    <div v-if="sortedLotteries.length === 0" class="empty-state">暂无中奖记录</div>

    <div v-for="l in sortedLotteries" :key="l.id" class="list-item">
      <div class="lottery-top">
        <span class="lottery-date">{{ formatDateTime(l.date) }}</span>
        <button class="del-btn-sm" @click="confirmDelete(l)">删除</button>
      </div>
      <div class="lottery-stats">
        <span>投入 ¥{{ (l.invest || 0).toFixed(2) }}</span>
        <span>中奖 ¥{{ (l.win || 0).toFixed(2) }}</span>
        <span :class="profitClass(l)">盈利 ¥{{ (l.win - l.invest).toFixed(2) }}</span>
        <span :class="profitClass(l)">{{ l.invest > 0 ? ((l.win - l.invest) / l.invest * 100).toFixed(2) : '0.00' }}%</span>
      </div>
    </div>

    <button class="fab" @click="$router.push('/lottery/add')">+</button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useLotteryStore } from '../stores/lottery'
import { formatDateTime } from '../utils/date'
import { showConfirmDialog, showToast } from 'vant'

const store = useLotteryStore()
const selectedMonth = ref('')
const sortBy = ref('date')

onMounted(() => store.loadLotteries())

const months = computed(() => {
  const set = new Set(store.lotteries.map(l => new Date(l.date).toISOString().slice(0, 7)))
  return [...set].sort().reverse()
})

const filteredLotteries = computed(() =>
  selectedMonth.value
    ? store.lotteries.filter(l => new Date(l.date).toISOString().slice(0, 7) === selectedMonth.value)
    : store.lotteries
)

const sortedLotteries = computed(() => {
  const arr = [...filteredLotteries.value]
  switch (sortBy.value) {
    case 'invest': return arr.sort((a, b) => b.invest - a.invest)
    case 'profit': return arr.sort((a, b) => (b.win - b.invest) - (a.win - a.invest))
    case 'ratio': return arr.sort((a, b) => ((b.win - b.invest) / b.invest) - ((a.win - a.invest) / a.invest))
    default: return arr.sort((a, b) => new Date(b.date) - new Date(a.date))
  }
})

function profitClass(l) {
  const p = l.win - l.invest
  return p > 0 ? 'profit-positive' : p < 0 ? 'profit-negative' : 'profit-zero'
}

function confirmDelete(l) {
  showConfirmDialog({ title: '删除记录', message: '确定删除这条记录吗？' })
    .then(() => { store.deleteLottery(l.id); showToast('已删除') })
    .catch(() => {})
}
</script>

<style scoped>
.filter-bar { display: flex; gap: 8px; padding: 12px; }
.select-field { flex: 1; padding: 8px 12px; border: 1px solid #E0E0E0; border-radius: 8px; font-size: 14px; background: var(--surface); }
.lottery-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.lottery-date { font-size: 12px; color: var(--on-surface-variant); }
.del-btn-sm { border: none; background: transparent; color: var(--error); font-size: 13px; cursor: pointer; }
.lottery-stats { display: flex; justify-content: space-between; font-size: 14px; }
</style>
