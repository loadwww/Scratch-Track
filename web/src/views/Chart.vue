<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">历史盈亏</span>
    </div>

    <div class="card">
      <div class="chart-title">最近 6 个月投入/中奖对比 <span class="chart-unit">单位：元</span></div>
      <BarChart :data="monthChartData" unit="100" />
      <div class="legend">
        <span class="legend-item"><span class="legend-dot" style="background:#E53935"></span>投入</span>
        <span class="legend-item"><span class="legend-dot" style="background:#43A047"></span>中奖</span>
      </div>
    </div>

    <div class="card">
      <div class="chart-title">最近 6 次记录投入/中奖对比 <span class="chart-unit">单位：元</span></div>
      <BarChart :data="recentChartData" unit="50" />
      <div class="legend">
        <span class="legend-item"><span class="legend-dot" style="background:#E53935"></span>投入</span>
        <span class="legend-item"><span class="legend-dot" style="background:#43A047"></span>中奖</span>
      </div>
    </div>

    <div v-if="olderData.length > 0" class="card">
      <div class="chart-title">6 个月以前数据 <span class="chart-unit">单位：元</span></div>
      <div v-for="m in olderData" :key="m.key" class="older-row">
        <span class="older-month">{{ m.key }}</span>
        <div class="older-bar-container">
          <div class="older-bar" :style="{ width: m.barWidth, background: m.profit >= 0 ? 'var(--success)' : 'var(--error)' }"></div>
        </div>
        <span :class="m.profit >= 0 ? 'profit-positive' : 'profit-negative'">{{ m.profit >= 0 ? '+' : '' }}{{ m.profit.toFixed(0) }} 元</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useLotteryStore } from '../stores/lottery'
import { getRecentMonths } from '../utils/date'
import BarChart from '../components/BarChart.vue'

const store = useLotteryStore()
onMounted(() => store.loadLotteries())

const recentMonths = getRecentMonths(6)

const monthChartData = computed(() => {
  return recentMonths.map(m => {
    const items = store.lotteries.filter(l => new Date(l.date).toISOString().slice(0, 7) === m.key)
    const invest = items.reduce((s, l) => s + (l.invest || 0), 0)
    const win = items.reduce((s, l) => s + (l.win || 0), 0)
    return { label: m.label, invest, win }
  })
})

const recentChartData = computed(() => {
  const recent = store.lotteries.slice(0, 6).reverse()
  return recent.map((l, i) => ({
    label: `第${i + 1}次`,
    invest: l.invest || 0,
    win: l.win || 0
  }))
})

const olderData = computed(() => {
  const sixMonthsAgo = new Date()
  sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)
  const older = store.lotteries.filter(l => new Date(l.date) < sixMonthsAgo)
  const grouped = {}
  older.forEach(l => {
    const key = new Date(l.date).toISOString().slice(0, 7)
    if (!grouped[key]) grouped[key] = { invest: 0, win: 0 }
    grouped[key].invest += l.invest || 0
    grouped[key].win += l.win || 0
  })
  const maxVal = Math.max(...Object.values(grouped).map(g => Math.abs(g.win - g.invest)), 1)
  return Object.entries(grouped).sort((a, b) => b[0].localeCompare(a[0])).map(([key, v]) => {
    const profit = v.win - v.invest
    return { key, profit, barWidth: (Math.abs(profit) / maxVal * 100) + '%' }
  })
})
</script>

<style scoped>
.chart-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
.chart-unit { font-size: 12px; font-weight: 400; color: var(--on-surface-variant); }
.legend { display: flex; gap: 16px; justify-content: center; margin-top: 8px; font-size: 13px; color: var(--on-surface-variant); }
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 2px; }
.older-row { display: flex; align-items: center; gap: 8px; margin: 6px 0; }
.older-month { font-size: 12px; width: 70px; color: var(--on-surface-variant); }
.older-bar-container { flex: 1; height: 16px; background: var(--surface-variant); border-radius: 4px; overflow: hidden; }
.older-bar { height: 100%; border-radius: 4px; }
</style>
