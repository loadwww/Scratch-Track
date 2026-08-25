import { defineStore } from 'pinia'
import { ref } from 'vue'
import { db } from '../db/database'

export const useScratchStore = defineStore('scratch', () => {
  const coins = ref(Number(localStorage.getItem('scratchCoins') || 0))

  async function addCoins(amount) {
    coins.value += amount
    localStorage.setItem('scratchCoins', String(coins.value))
    await db.scratchRecords.add({
      amount,
      type: amount > 0 ? 'claim' : 'scratch',
      date: new Date().toISOString()
    })
  }

  async function spendCoins(amount) {
    if (coins.value < amount) return false
    coins.value -= amount
    localStorage.setItem('scratchCoins', String(coins.value))
    await db.scratchRecords.add({
      amount: -amount,
      type: 'scratch',
      date: new Date().toISOString()
    })
    return true
  }

  function canClaimDaily(settingsStore) {
    const today = new Date().toDateString()
    return settingsStore.lastClaimDate !== today
  }

  async function claimDaily(settingsStore) {
    const today = new Date().toDateString()
    if (settingsStore.lastClaimDate === today) return false
    await addCoins(settingsStore.scratchDailyCoins)
    settingsStore.lastClaimDate = today
    return true
  }

  return { coins, addCoins, spendCoins, canClaimDaily, claimDaily }
})
