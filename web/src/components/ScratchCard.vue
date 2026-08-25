<template>
  <canvas ref="canvasRef" class="scratch-canvas"
    @touchstart.prevent="startDraw"
    @touchmove.prevent="moveDraw"
    @touchend.prevent="endDraw"
    @mousedown="startDraw"
    @mousemove="moveDraw"
    @mouseup="endDraw"
    @mouseleave="endDraw"></canvas>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const emit = defineEmits(['reveal'])
const canvasRef = ref(null)
let ctx = null
let isDrawing = false
let lastX = 0, lastY = 0

onMounted(() => {
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  const dpr = window.devicePixelRatio || 1
  const w = canvas.offsetWidth, h = 200
  canvas.width = w * dpr
  canvas.height = h * dpr
  canvas.style.height = h + 'px'
  ctx.scale(dpr, dpr)

  const gradient = ctx.createLinearGradient(0, 0, w, h)
  gradient.addColorStop(0, '#BDBDBD')
  gradient.addColorStop(0.5, '#E0E0E0')
  gradient.addColorStop(1, '#9E9E9E')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, w, h)

  ctx.fillStyle = '#616161'
  ctx.font = 'bold 18px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('刮开看结果', w / 2, h / 2 + 6)
  ctx.textAlign = 'start'
})

function getPos(e) {
  const canvas = canvasRef.value
  const rect = canvas.getBoundingClientRect()
  const isTouch = e.touches
  const clientX = isTouch ? e.touches[0].clientX : e.clientX
  const clientY = isTouch ? e.touches[0].clientY : e.clientY
  return { x: clientX - rect.left, y: clientY - rect.top }
}

function startDraw(e) {
  isDrawing = true
  const pos = getPos(e)
  lastX = pos.x
  lastY = pos.y
  ctx.globalCompositeOperation = 'destination-out'
  ctx.beginPath()
  ctx.arc(pos.x, pos.y, 20, 0, Math.PI * 2)
  ctx.fill()
}

function moveDraw(e) {
  if (!isDrawing) return
  const pos = getPos(e)
  ctx.lineWidth = 40
  ctx.lineCap = 'round'
  ctx.beginPath()
  ctx.moveTo(lastX, lastY)
  ctx.lineTo(pos.x, pos.y)
  ctx.stroke()
  lastX = pos.x
  lastY = pos.y
}

function endDraw() {
  if (!isDrawing) return
  isDrawing = false
  checkReveal()
}

function checkReveal() {
  const canvas = canvasRef.value
  const w = canvas.width, h = canvas.height
  const data = ctx.getImageData(0, 0, w, h).data
  let cleared = 0
  for (let i = 3; i < data.length; i += 4) {
    if (data[i] === 0) cleared++
  }
  const total = data.length / 4
  if (cleared / total > 0.5) {
    ctx.globalCompositeOperation = 'destination-out'
    ctx.fillRect(0, 0, w, h)
    emit('reveal')
  }
}
</script>

<style scoped>
.scratch-canvas { width: 100%; display: block; border-radius: 12px; touch-action: none; }
</style>
