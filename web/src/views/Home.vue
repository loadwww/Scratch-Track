<template>
  <div class="page" :style="bgStyle">
    <div class="home-header">
      <span class="home-title">Scratch Track</span>
      <van-icon name="setting-o" size="22" color="#FFFFFF" @click="$router.push('/settings')" />
    </div>

    <div v-if="budgetAlert" class="budget-alert" :style="{ color: alertColor }">
      {{ budgetAlert }}
    </div>

    <div class="card">
      <div class="stat-row">
        <div class="stat-item">
          <div class="stat-label">本月投入</div>
          <div class="stat-value">¥{{ monthInvest.toFixed(2) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">本月中奖</div>
          <div class="stat-value">¥{{ monthWin.toFixed(2) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label" :class="profitClass">盈亏比</div>
          <div class="stat-value" :class="profitClass">{{ profitRatio }}%</div>
        </div>
      </div>
      <div class="budget-section">
        <div v-if="settings.monthlyBudget > 0" class="budget-info">
          <span>预算 ¥{{ monthInvest.toFixed(2) }} / ¥{{ settings.monthlyBudget.toFixed(2) }}</span>
          <span>{{ budgetPercent }}%</span>
        </div>
        <div v-else class="budget-info">
          <span style="color: var(--on-surface-variant)">未设置月度预算</span>
          <span style="color: var(--primary-color)" @click="$router.push('/settings')">去设置</span>
        </div>
        <div class="budget-bar-container">
          <div class="budget-bar-fill" :style="{ width: budgetBarWidth, background: settings.monthlyBudget > 0 ? budgetBarColor : '#E0E0E0' }"></div>
        </div>
      </div>
    </div>

    <div class="marquee-text" @click="rotateMarquee">{{ currentMarquee }}</div>

    <div class="func-grid">
      <div class="func-item" @click="$router.push('/lottery')">
        <van-icon name="balance-o" size="32" color="#B71C1C" />
        <div class="func-name">中奖记录</div>
      </div>
      <div class="func-item" @click="$router.push('/diary')">
        <van-icon name="notes-o" size="32" color="#B71C1C" />
        <div class="func-name">彩票日记</div>
      </div>
      <div class="func-item" @click="$router.push('/highlight')">
        <van-icon name="photo-o" size="32" color="#B71C1C" />
        <div class="func-name">高光时刻</div>
      </div>
      <div class="func-item" @click="$router.push('/chart')">
        <van-icon name="chart-trending-o" size="32" color="#B71C1C" />
        <div class="func-name">历史盈亏</div>
      </div>
      <div class="func-item" @click="$router.push('/random')">
        <van-icon name="aim" size="32" color="#B71C1C" />
        <div class="func-name">随机数</div>
      </div>
      <div class="func-item" @click="$router.push('/settings')">
        <van-icon name="setting-o" size="32" color="#B71C1C" />
        <div class="func-name">设置</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useLotteryStore } from '../stores/lottery'
import { useSettingsStore } from '../stores/settings'

const lotteryStore = useLotteryStore()
const settings = useSettingsStore()
const marqueeIndex = ref(0)

onMounted(() => {
  lotteryStore.loadLotteries()
})

const monthLotteries = computed(() =>
  lotteryStore.lotteries.filter(l => {
    const d = new Date(l.date)
    const now = new Date()
    return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
  })
)

const monthInvest = computed(() => monthLotteries.value.reduce((s, l) => s + (l.invest || 0), 0))
const monthWin = computed(() => monthLotteries.value.reduce((s, l) => s + (l.win || 0), 0))
const profit = computed(() => monthWin.value - monthInvest.value)
const profitClass = computed(() =>
  profit.value > 0 ? 'profit-positive' : profit.value < 0 ? 'profit-negative' : 'profit-zero'
)
const profitRatio = computed(() => {
  if (monthInvest.value === 0) return '0.00'
  return ((profit.value / monthInvest.value) * 100).toFixed(2)
})

const budgetPercent = computed(() =>
  settings.monthlyBudget > 0 ? ((monthInvest.value / settings.monthlyBudget) * 100).toFixed(2) : '0'
)
const budgetBarWidth = computed(() =>
  Math.min(100, settings.monthlyBudget > 0 ? (monthInvest.value / settings.monthlyBudget) * 100 : 0) + '%'
)
const budgetBarColor = computed(() => {
  const pct = settings.monthlyBudget > 0 ? monthInvest.value / settings.monthlyBudget : 0
  if (pct > 1) return '#B71C1C'
  if (pct > 0.8) return '#FFB300'
  if (pct > 0.5) return '#1E88E5'
  return '#43A047'
})

const budgetAlert = computed(() => {
  if (settings.monthlyBudget <= 0) return ''
  if (monthInvest.value > settings.monthlyBudget)
    return `已超支 ¥${(monthInvest.value - settings.monthlyBudget).toFixed(2)}`
  const pct = monthInvest.value / settings.monthlyBudget
  if (pct >= 0.8) return `接近预算上限，请控制`
  return ''
})
const alertColor = computed(() => {
  if (monthInvest.value > settings.monthlyBudget) return '#B71C1C'
  return '#FF9800'
})

const currentMarquee = computed(() => {
  const texts = settings.marqueeTexts.filter(t => t.trim())
  if (texts.length === 0) return '见好就收'
  return texts[marqueeIndex.value % texts.length] || texts[0]
})
function rotateMarquee() {
  const texts = settings.marqueeTexts.filter(t => t.trim())
  if (texts.length <= 1) return
  let next = Math.floor(Math.random() * texts.length)
  while (next === marqueeIndex.value && texts.length > 1) {
    next = Math.floor(Math.random() * texts.length)
  }
  marqueeIndex.value = next
}

const bgStyle = computed(() =>
  settings.wallpaper ? { backgroundImage: `url(${settings.wallpaper})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}
)
</script>

<style scoped>
.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #B71C1C;
}
.home-title {
  font-size: 22px;
  font-weight: bold;
  color: #FFFFFF;
}
.budget-section {
  margin-top: 14px;
}
.budget-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--on-surface-variant);
  margin-bottom: 6px;
}
.budget-bar-container {
  width: 100%;
  height: 14px;
  background: #E0E0E0;
  border-radius: 7px;
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
}
.budget-bar-fill {
  height: 100%;
  border-radius: 7px;
  transition: width 0.4s ease, background 0.3s;
}
.budget-alert {
  text-align: center;
  padding: 8px 16px;
  font-weight: 600;
  font-size: 14px;
}
</style>
