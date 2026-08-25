<template>
  <canvas ref="canvasRef" class="bar-chart"></canvas>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  data: { type: Array, required: true },
  unit: { type: Number, default: 100 }
})

const canvasRef = ref(null)

function draw() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const dpr = window.devicePixelRatio || 1
  const width = canvas.offsetWidth
  const height = 200
  canvas.width = width * dpr
  canvas.height = height * dpr
  canvas.style.height = height + 'px'
  ctx.scale(dpr, dpr)
  ctx.clearRect(0, 0, width, height)

  const padding = { top: 20, right: 10, bottom: 30, left: 40 }
  const chartW = width - padding.left - padding.right
  const chartH = height - padding.top - padding.bottom

  const maxVal = Math.max(...props.data.map(d => Math.max(d.invest, d.win)), 1)
  const groupWidth = chartW / Math.max(props.data.length, 1)
  const barWidth = groupWidth * 0.35

  ctx.strokeStyle = '#E0E0E0'
  ctx.fillStyle = '#757575'
  ctx.font = '10px sans-serif'
  const steps = 4
  for (let i = 0; i <= steps; i++) {
    const y = padding.top + chartH - (chartH / steps) * i
    const val = Math.round((maxVal / steps) * i / props.unit) * props.unit
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(width - padding.right, y)
    ctx.stroke()
    ctx.fillText(String(val), 2, y + 4)
  }

  props.data.forEach((d, i) => {
    const x = padding.left + groupWidth * i + groupWidth / 2 - barWidth

    const investH = (d.invest / maxVal) * chartH
    ctx.fillStyle = '#D32F2F'
    ctx.fillRect(x, padding.top + chartH - investH, barWidth, investH)

    const winH = (d.win / maxVal) * chartH
    ctx.fillStyle = '#4CAF50'
    ctx.fillRect(x + barWidth, padding.top + chartH - winH, barWidth, winH)

    ctx.fillStyle = '#757575'
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText(d.label, x + barWidth, padding.top + chartH + 16)
    ctx.textAlign = 'start'
  })
}

onMounted(draw)
watch(() => props.data, draw, { deep: true })
</script>

<style scoped>
.bar-chart { width: 100%; display: block; }
</style>
