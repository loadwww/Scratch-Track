import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const marqueeTexts = ref(JSON.parse(localStorage.getItem('marqueeTexts') || '["见好就收"]'))
  const monthlyBudget = ref(Number(localStorage.getItem('monthlyBudget') || 0))
  const budgetWarnPercent = ref(Number(localStorage.getItem('budgetWarnPercent') || 60))
  const budgetAlertPercent = ref(Number(localStorage.getItem('budgetAlertPercent') || 80))
  const budgetOverText = ref(localStorage.getItem('budgetOverText') || '已超支 ¥')
  const budgetWarnText = ref(localStorage.getItem('budgetWarnText') || '预算使用较多，注意控制')
  const budgetAlertText = ref(localStorage.getItem('budgetAlertText') || '预算已达警戒线，请控制')
  const themeColor = ref(localStorage.getItem('themeColor') || '#B71C1C')
  const wallpaper = ref(localStorage.getItem('wallpaper') || '')
  const musicEnabled = ref(localStorage.getItem('musicEnabled') !== 'false')
  const musicName = ref(localStorage.getItem('musicName') || '好运来')
  const musicData = ref(localStorage.getItem('musicData') || '')
  const scratchDailyCoins = ref(Number(localStorage.getItem('scratchDailyCoins') || 100))
  const lastClaimDate = ref(localStorage.getItem('lastClaimDate') || '')

  watch(marqueeTexts, v => localStorage.setItem('marqueeTexts', JSON.stringify(v)), { deep: true })
  watch(monthlyBudget, v => localStorage.setItem('monthlyBudget', String(v)))
  watch(budgetWarnPercent, v => localStorage.setItem('budgetWarnPercent', String(v)))
  watch(budgetAlertPercent, v => localStorage.setItem('budgetAlertPercent', String(v)))
  watch(budgetOverText, v => localStorage.setItem('budgetOverText', v))
  watch(budgetWarnText, v => localStorage.setItem('budgetWarnText', v))
  watch(budgetAlertText, v => localStorage.setItem('budgetAlertText', v))
  watch(themeColor, v => localStorage.setItem('themeColor', v))
  watch(wallpaper, v => localStorage.setItem('wallpaper', v))
  watch(musicEnabled, v => localStorage.setItem('musicEnabled', String(v)))
  watch(musicName, v => localStorage.setItem('musicName', v))
  watch(musicData, v => localStorage.setItem('musicData', v))
  watch(scratchDailyCoins, v => localStorage.setItem('scratchDailyCoins', String(v)))
  watch(lastClaimDate, v => localStorage.setItem('lastClaimDate', v))

  return {
    marqueeTexts, monthlyBudget, budgetWarnPercent, budgetAlertPercent,
    budgetOverText, budgetWarnText, budgetAlertText,
    themeColor, wallpaper,
    musicEnabled, musicName, musicData, scratchDailyCoins, lastClaimDate
  }
})
