import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { db } from '../db/database'

export const useDiaryStore = defineStore('diary', () => {
  const diaries = ref([])

  async function loadDiaries() {
    diaries.value = await db.diaries.orderBy('date').reverse().toArray()
  }

  async function addDiary(data) {
    const id = await db.diaries.add({
      ...data,
      date: new Date().toISOString(),
      isHighlighted: false
    })
    await loadDiaries()
    return id
  }

  async function updateDiary(id, changes) {
    await db.diaries.update(id, changes)
    await loadDiaries()
  }

  async function deleteDiary(id) {
    await db.diaries.delete(id)
    await loadDiaries()
  }

  async function toggleHighlight(id) {
    const diary = await db.diaries.get(id)
    if (diary) {
      await db.diaries.update(id, { isHighlighted: !diary.isHighlighted })
      await loadDiaries()
    }
  }

  const highlights = computed(() => diaries.value.filter(d => d.isHighlighted))

  return { diaries, loadDiaries, addDiary, updateDiary, deleteDiary, toggleHighlight, highlights }
})
