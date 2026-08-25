<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">设置</span>
    </div>

    <van-cell-group inset style="margin-top: 12px">
      <van-field label="首页轮换文字" readonly />
      <div style="padding: 0 16px 12px">
        <div v-for="(text, i) in settings.marqueeTexts" :key="i" class="text-row">
          <input v-model="settings.marqueeTexts[i]" class="input-field" placeholder="输入提醒文字" />
          <button class="del-btn" @click="settings.marqueeTexts.splice(i, 1)">x</button>
        </div>
        <button class="add-text-btn" @click="settings.marqueeTexts.push('')">+ 添加句子</button>
      </div>
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-field label="月度预算" v-model="budgetInput" type="number" placeholder="设置月度购彩预算" @blur="saveBudget" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="盈利庆祝音乐" center>
        <template #right-icon>
          <van-switch v-model="settings.musicEnabled" />
        </template>
      </van-cell>
      <van-cell title="更换音乐文件" is-link @click="pickMusic" :value="settings.musicName" />
      <input ref="musicInput" type="file" accept="audio/*" style="display:none" @change="onMusicPicked" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="自定义壁纸" is-link @click="pickWallpaper" :value="settings.wallpaper ? '已设置' : ''" />
      <input ref="wallpaperInput" type="file" accept="image/*" style="display:none" @change="onWallpaperPicked" />
      <van-cell title="清除壁纸" is-link @click="settings.wallpaper = ''" v-if="settings.wallpaper" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell title="导出数据" is-link @click="exportData" />
      <van-cell title="导入数据" is-link @click="importData" />
      <input ref="importInput" type="file" accept=".json" style="display:none" @change="onImportData" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useSettingsStore } from '../stores/settings'
import { db } from '../db/database'
import { showToast } from 'vant'

const settings = useSettingsStore()
const budgetInput = ref(String(settings.monthlyBudget || ''))
const musicInput = ref(null)
const wallpaperInput = ref(null)
const importInput = ref(null)

function saveBudget() {
  settings.monthlyBudget = Number(budgetInput.value) || 0
}

function pickMusic() { musicInput.value?.click() }
async function onMusicPicked(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    settings.musicData = reader.result
    settings.musicName = file.name.replace(/\.[^.]+$/, '')
    showToast('音乐已设置')
  }
  reader.readAsDataURL(file)
}

function pickWallpaper() { wallpaperInput.value?.click() }
async function onWallpaperPicked(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => {
    settings.wallpaper = reader.result
    showToast('壁纸已设置')
  }
  reader.readAsDataURL(file)
}

async function exportData() {
  const data = {
    diaries: await db.diaries.toArray(),
    lotteries: await db.lotteries.toArray(),
    settings: { ...localStorage }
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `caiji-backup-${new Date().toISOString().slice(0, 10)}.json`
  a.click()
  URL.revokeObjectURL(url)
  showToast('数据已导出')
}

function importData() { importInput.value?.click() }
async function onImportData(e) {
  const file = e.target.files[0]
  if (!file) return
  const text = await file.text()
  const data = JSON.parse(text)
  if (data.diaries) {
    await db.diaries.clear()
    await db.diaries.bulkAdd(data.diaries)
  }
  if (data.lotteries) {
    await db.lotteries.clear()
    await db.lotteries.bulkAdd(data.lotteries)
  }
  if (data.settings) {
    for (const [k, v] of Object.entries(data.settings)) {
      localStorage.setItem(k, v)
    }
  }
  showToast('数据已导入')
  setTimeout(() => location.reload(), 1000)
}
</script>

<style scoped>
.text-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.del-btn {
  width: 32px;
  border: none;
  background: var(--surface-variant);
  border-radius: 8px;
  color: var(--error);
  font-size: 16px;
  cursor: pointer;
}
.add-text-btn {
  width: 100%;
  padding: 8px;
  border: 1px dashed var(--on-surface-variant);
  border-radius: 8px;
  background: transparent;
  color: var(--on-surface-variant);
  cursor: pointer;
}
</style>
