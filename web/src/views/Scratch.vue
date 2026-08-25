<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">刮刮乐</span>
    </div>

    <div class="card" style="text-align: center">
      <div class="coins-display">
        <span class="coins-label">金币余额</span>
        <span class="coins-value">{{ scratch.coins }}</span>
      </div>
      <button v-if="canClaim" class="btn-primary claim-btn" @click="claimDaily">
        领取每日 {{ settings.scratchDailyCoins }} 金币
      </button>
      <div v-else class="claimed-tip">今日已领取</div>
    </div>

    <div v-if="prize === null && scratch.coins >= 20" class="card">
      <div class="facevalue-title">选择面值</div>
      <div class="facevalue-row">
        <button v-for="fv in faceValues" :key="fv"
          :class="['fv-btn', { active: selectedFV === fv }]"
          @click="selectedFV = fv">{{ fv }} 金币</button>
      </div>
      <div class="prize-info">
        <div>中奖率 50% / 返奖率 65%</div>
        <div class="prize-levels">1x 2x 5x 10x 20x 100x</div>
      </div>
      <button class="btn-primary" style="width: 100%; margin-top: 12px" @click="startScratch">开始刮奖</button>
    </div>

    <div v-if="prize !== null" class="card">
      <div class="scratch-area">
        <div class="prize-result" :class="{ win: prize > 0 }">
          <div v-if="prize > 0" class="prize-amount">中奖 {{ prize }} 金币</div>
          <div v-else class="prize-no-win">未中奖</div>
        </div>
        <ScratchCard v-if="!revealed" @reveal="onReveal" />
      </div>
      <button class="btn-primary" style="width: 100%; margin-top: 12px" @click="reset">再来一局</button>
    </div>

    <div v-if="scratch.coins < 20 && !canClaim && prize === null" class="card empty-state">
      金币不足，请明天再来领取
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useScratchStore } from '../stores/scratch'
import { useSettingsStore } from '../stores/settings'
import { playScratchSound } from '../utils/audio'
import ScratchCard from '../components/ScratchCard.vue'
import { showToast } from 'vant'

const scratch = useScratchStore()
const settings = useSettingsStore()
const selectedFV = ref(20)
const prize = ref(null)
const revealed = ref(false)

const faceValues = [20, 30, 50]

const canClaim = computed(() => {
  const today = new Date().toDateString()
  return settings.lastClaimDate !== today
})

onMounted(() => {
  if (canClaim.value) scratch.claimDaily(settings)
})

async function claimDaily() {
  const ok = await scratch.claimDaily(settings)
  if (ok) showToast(`已领取 ${settings.scratchDailyCoins} 金币`)
}

const prizeWeights = [
  { multiplier: 100, weight: 5 },
  { multiplier: 20, weight: 18 },
  { multiplier: 10, weight: 32 },
  { multiplier: 5, weight: 145 },
  { multiplier: 2, weight: 1350 },
  { multiplier: 1, weight: 8450 }
]

async function startScratch() {
  if (scratch.coins < selectedFV.value) { showToast('金币不足'); return }
  await scratch.spendCoins(selectedFV.value)
  const isWin = Math.random() < 0.5
  if (!isWin) { prize.value = 0; return }
  const totalWeight = prizeWeights.reduce((s, p) => s + p.weight, 0)
  let rand = Math.random() * totalWeight
  let multiplier = 1
  for (const p of prizeWeights) {
    rand -= p.weight
    if (rand <= 0) { multiplier = p.multiplier; break }
  }
  prize.value = selectedFV.value * multiplier
}

function onReveal() {
  revealed.value = true
  playScratchSound()
  if (prize.value > 0) {
    scratch.addCoins(prize.value)
    showToast(`中奖 ${prize.value} 金币`)
  }
}

function reset() {
  prize.value = null
  revealed.value = false
}
</script>

<style scoped>
.coins-display { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.coins-label { font-size: 14px; color: var(--on-surface-variant); }
.coins-value { font-size: 32px; font-weight: bold; color: var(--primary-color); }
.claim-btn { margin-top: 12px; }
.claimed-tip { margin-top: 12px; color: var(--on-surface-variant); font-size: 14px; }
.facevalue-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.facevalue-row { display: flex; gap: 8px; }
.fv-btn { flex: 1; padding: 12px; border: 2px solid #E0E0E0; border-radius: 10px; background: var(--surface); font-size: 15px; cursor: pointer; }
.fv-btn.active { border-color: var(--primary-color); background: var(--primary-container); color: var(--primary-color); font-weight: 600; }
.prize-info { margin-top: 12px; text-align: center; font-size: 13px; color: var(--on-surface-variant); }
.prize-levels { margin-top: 4px; letter-spacing: 2px; }
.scratch-area { position: relative; }
.prize-result { height: 200px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: var(--surface-variant); }
.prize-result.win { background: var(--primary-container); }
.prize-amount { font-size: 24px; font-weight: bold; color: var(--primary-color); }
.prize-no-win { font-size: 20px; color: var(--on-surface-variant); }
</style>
