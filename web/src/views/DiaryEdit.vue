<template>
  <div class="page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">&lt;</button>
      <span class="page-title">{{ isEdit ? '查看日记' : '添加日记' }}</span>
      <button v-if="isEdit" class="del-btn-header" @click="confirmDelete">删除</button>
    </div>

    <div class="input-group">
      <label>标题</label>
      <input v-model="form.title" class="input-field" placeholder="输入标题" />
    </div>
    <div class="input-group">
      <label>文字描述</label>
      <textarea v-model="form.description" class="input-field" placeholder="输入描述"></textarea>
    </div>
    <div class="input-group">
      <label>附图</label>
      <div v-if="form.image" class="img-preview">
        <img :src="form.image" />
        <button class="del-img-btn" @click="form.image = ''">移除</button>
      </div>
      <button v-else class="pick-img-btn" @click="pickImage">+ 选择图片</button>
      <input ref="imageInput" type="file" accept="image/*" style="display:none" @change="onImagePicked" />
    </div>

    <div style="padding: 12px">
      <button class="btn-primary" style="width: 100%" @click="save">保存</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDiaryStore } from '../stores/diary'
import { showConfirmDialog, showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const store = useDiaryStore()
const imageInput = ref(null)
const form = ref({ title: '', description: '', image: '' })

const isEdit = computed(() => !!route.params.id)

onMounted(async () => {
  if (isEdit.value) {
    await store.loadDiaries()
    const d = store.diaries.find(d => d.id === Number(route.params.id))
    if (d) form.value = { title: d.title || '', description: d.description || '', image: d.image || '' }
  }
})

function pickImage() { imageInput.value?.click() }
async function onImagePicked(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => { form.value.image = reader.result }
  reader.readAsDataURL(file)
}

async function save() {
  if (!form.value.title && !form.value.description && !form.value.image) {
    showToast('请至少输入一项内容')
    return
  }
  if (isEdit.value) {
    await store.updateDiary(Number(route.params.id), { ...form.value })
  } else {
    await store.addDiary({ ...form.value })
  }
  showToast('已保存')
  router.back()
}

function confirmDelete() {
  showConfirmDialog({ title: '删除日记', message: '确定删除这篇日记吗？' })
    .then(() => { store.deleteDiary(Number(route.params.id)); showToast('已删除'); router.back() })
    .catch(() => {})
}
</script>

<style scoped>
.del-btn-header { background: none; border: none; color: var(--on-primary); font-size: 14px; cursor: pointer; }
.img-preview { position: relative; }
.img-preview img { width: 100%; max-height: 200px; object-fit: cover; border-radius: 8px; }
.del-img-btn { position: absolute; top: 8px; right: 8px; background: rgba(0,0,0,0.6); color: #fff; border: none; border-radius: 4px; padding: 4px 8px; font-size: 12px; cursor: pointer; }
.pick-img-btn { width: 100%; padding: 20px; border: 1px dashed var(--on-surface-variant); border-radius: 8px; background: transparent; color: var(--on-surface-variant); cursor: pointer; }
</style>
