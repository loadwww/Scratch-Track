let currentAudio = null

export function playWinMusic(musicData) {
  stopWinMusic()
  if (!musicData) {
    playDefaultMelody()
    return
  }
  currentAudio = new Audio(musicData)
  currentAudio.currentTime = 6
  currentAudio.play().catch(() => {})
  setTimeout(() => stopWinMusic(), 10000)
}

function playDefaultMelody() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const notes = [523.25, 587.33, 659.25, 698.46, 783.99, 880, 783.99, 659.25]
    const now = ctx.currentTime
    notes.forEach((freq, i) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.frequency.value = freq
      osc.type = 'sine'
      gain.gain.setValueAtTime(0, now + i * 0.3)
      gain.gain.linearRampToValueAtTime(0.15, now + i * 0.3 + 0.05)
      gain.gain.exponentialRampToValueAtTime(0.001, now + i * 0.3 + 0.25)
      osc.start(now + i * 0.3)
      osc.stop(now + i * 0.3 + 0.3)
    })
    currentAudio = { ctx }
    setTimeout(() => stopWinMusic(), 3000)
  } catch (e) {}
}

export function stopWinMusic() {
  if (currentAudio) {
    if (currentAudio.pause) currentAudio.pause()
    if (currentAudio.ctx) currentAudio.ctx.close()
    currentAudio = null
  }
}

export function playScratchSound() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    const filter = ctx.createBiquadFilter()
    osc.connect(filter)
    filter.connect(gain)
    gain.connect(ctx.destination)
    filter.type = 'lowpass'
    filter.frequency.value = 800
    osc.type = 'sawtooth'
    osc.frequency.value = 150 + Math.random() * 100
    gain.gain.setValueAtTime(0.08, ctx.currentTime)
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.2)
    osc.start()
    osc.stop(ctx.currentTime + 0.2)
    setTimeout(() => ctx.close(), 300)
  } catch (e) {}
}
