<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">随机数</span>
    </div>

    <div class="card">
      <div class="rand-input-row">
        <div class="rand-input-item">
          <label>最小值</label>
          <input v-model="minVal" type="number" class="input-field" />
        </div>
        <div class="rand-input-item">
          <label>最大值</label>
          <input v-model="maxVal" type="number" class="input-field" />
        </div>
      </div>
      <button class="btn-primary" style="width: 100%; margin-top: 12px" @click="generate">生成随机数</button>
    </div>

    <div v-if="result !== null" class="card result-card">
      <div class="result-label">随机结果</div>
      <div class="result-value">{{ result }}</div>
    </div>

    <div class="card">
      <div class="pool-title">幸运池</div>
      <div v-if="!poolCreated" class="pool-section">
        <div class="pool-inputs">
          <input v-model="poolInput" class="input-field" placeholder="输入号码后回车" @keyup.enter="addNumber" />
          <button class="pool-add-btn" @click="addNumber">添加</button>
        </div>
        <div v-if="poolNumbers.length > 0" class="pool-numbers">
          <span v-for="(n, i) in poolNumbers" :key="i" class="pool-tag" @click="poolNumbers.splice(i, 1)">
            {{ n }} x
          </span>
        </div>
        <button v-if="poolNumbers.length > 0" class="btn-primary" style="width: 100%; margin-top: 8px" @click="createPool">创建幸运池</button>
      </div>
      <div v-else class="pool-created">
        <div class="pool-numbers">
          <span v-for="(n, i) in poolNumbers" :key="i" class="pool-tag">{{ n }}</span>
        </div>
        <button class="btn-primary" style="width: 100%; margin-top: 8px" @click="drawFromPool">从池中抽取</button>
        <button class="reset-pool-btn" @click="resetPool">重置幸运池</button>
        <div v-if="poolResult !== null" class="pool-result">抽中: {{ poolResult }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const minVal = ref(1)
const maxVal = ref(100)
const result = ref(null)
const poolInput = ref('')
const poolNumbers = ref(JSON.parse(localStorage.getItem('poolNumbers') || '[]'))
const poolCreated = ref(JSON.parse(localStorage.getItem('poolCreated') || 'false'))
const poolResult = ref(null)

watch(poolNumbers, v => localStorage.setItem('poolNumbers', JSON.stringify(v)), { deep: true })
watch(poolCreated, v => localStorage.setItem('poolCreated', JSON.stringify(v)))

function generate() {
  const min = Number(minVal.value)
  const max = Number(maxVal.value)
  if (min >= max) { result.value = min; return }
  result.value = Math.floor(Math.random() * (max - min + 1)) + min
}

function addNumber() {
  if (poolInput.value.trim()) {
    poolNumbers.value.push(poolInput.value.trim())
    poolInput.value = ''
  }
}
function createPool() {
  if (poolNumbers.value.length === 0) return
  poolCreated.value = true
}
function drawFromPool() {
  poolResult.value = poolNumbers.value[Math.floor(Math.random() * poolNumbers.value.length)]
}
function resetPool() {
  poolNumbers.value = []
  poolCreated.value = false
  poolResult.value = null
}
</script>

<style scoped>
.rand-input-row { display: flex; gap: 12px; }
.rand-input-item { flex: 1; }
.rand-input-item label { display: block; font-size: 14px; color: var(--on-surface-variant); margin-bottom: 6px; }
.result-card { text-align: center; }
.result-label { font-size: 14px; color: var(--on-surface-variant); }
.result-value { font-size: 36px; font-weight: bold; color: var(--primary-color); margin-top: 8px; }
.pool-title { font-size: 16px; font-weight: 600; margin-bottom: 12px; }
.pool-inputs { display: flex; gap: 8px; }
.pool-add-btn { background: var(--primary-color); color: #fff; border: none; border-radius: 8px; padding: 0 16px; cursor: pointer; white-space: nowrap; }
.pool-numbers { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.pool-tag { background: var(--primary-container); color: var(--on-primary-container); padding: 4px 12px; border-radius: 16px; font-size: 14px; cursor: pointer; }
.reset-pool-btn { width: 100%; margin-top: 8px; background: none; border: none; color: var(--on-surface-variant); font-size: 13px; cursor: pointer; }
.pool-result { text-align: center; font-size: 24px; font-weight: bold; color: var(--primary-color); margin-top: 12px; }
</style>
