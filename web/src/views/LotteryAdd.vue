<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">添加记录</span>
    </div>

    <div class="input-group">
      <label>投入金额</label>
      <input v-model="invest" type="number" class="input-field" placeholder="输入投入金额" />
    </div>
    <div class="input-group">
      <label>中奖金额</label>
      <input v-model="win" type="number" class="input-field" placeholder="输入中奖金额" />
    </div>

    <div style="padding: 12px">
      <button class="btn-primary" style="width: 100%" @click="save">保存</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useLotteryStore } from '../stores/lottery'
import { useSettingsStore } from '../stores/settings'
import { playWinMusic } from '../utils/audio'
import { showToast } from 'vant'

const router = useRouter()
const store = useLotteryStore()
const settings = useSettingsStore()
const invest = ref('')
const win = ref('')

async function save() {
  const inv = Number(invest.value) || 0
  const w = Number(win.value) || 0
  if (inv === 0 && w === 0) { showToast('请输入金额'); return }
  await store.addLottery({ invest: inv, win: w })
  if (w > inv && settings.musicEnabled) {
    playWinMusic(settings.musicData)
  }
  showToast('已保存')
  router.back()
}
</script>
