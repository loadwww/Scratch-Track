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

  // 动态选择合适的刻度间隔
  const rawStep = maxVal / 4
  let actualUnit
  if (rawStep >= 100) actualUnit = 100
  else if (rawStep >= 50) actualUnit = 50
  else if (rawStep >= 10) actualUnit = 10
  else actualUnit = 1

  ctx.strokeStyle = '#E0E0E0'
  ctx.fillStyle = '#666670'
  ctx.font = '10px sans-serif'
  const steps = 4
  for (let i = 0; i <= steps; i++) {
    const y = padding.top + chartH - (chartH / steps) * i
    const val = Math.round((maxVal / steps) * i / actualUnit) * actualUnit
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(width - padding.right, y)
    ctx.stroke()
    ctx.fillText('¥' + val, 2, y + 4)
  }

  props.data.forEach((d, i) => {
    const x = padding.left + groupWidth * i + groupWidth / 2 - barWidth

    const investH = (d.invest / maxVal) * chartH
    ctx.fillStyle = '#E53935'
    ctx.fillRect(x, padding.top + chartH - investH, barWidth, investH)

    const winH = (d.win / maxVal) * chartH
    ctx.fillStyle = '#43A047'
    ctx.fillRect(x + barWidth, padding.top + chartH - winH, barWidth, winH)

    // 柱子上方显示数值
    ctx.fillStyle = '#666670'
    ctx.font = '9px sans-serif'
    ctx.textAlign = 'center'
    if (d.invest > 0) ctx.fillText('¥' + d.invest, x + barWidth / 2, padding.top + chartH - investH - 4)
    if (d.win > 0) ctx.fillText('¥' + d.win, x + barWidth * 1.5, padding.top + chartH - winH - 4)

    ctx.fillStyle = '#666670'
    ctx.font = '10px sans-serif'
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
