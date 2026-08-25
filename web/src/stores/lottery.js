import { defineStore } from 'pinia'
import { ref } from 'vue'
import { db } from '../db/database'

export const useLotteryStore = defineStore('lottery', () => {
  const lotteries = ref([])

  async function loadLotteries() {
    lotteries.value = await db.lotteries.orderBy('date').reverse().toArray()
  }

  async function addLottery(data) {
    const id = await db.lotteries.add({
      ...data,
      date: new Date().toISOString(),
      month: new Date().toISOString().slice(0, 7)
    })
    await loadLotteries()
    return id
  }

  async function deleteLottery(id) {
    await db.lotteries.delete(id)
    await loadLotteries()
  }

  return { lotteries, loadLotteries, addLottery, deleteLottery }
})
