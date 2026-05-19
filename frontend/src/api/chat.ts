import type { SSEEvent } from '@/types'

const API_BASE = '/api'

let sessionId: string | null = null

export async function getSessionId(): Promise<string> {
  if (sessionId) return sessionId
  try {
    const res = await fetch(`${API_BASE}/session`)
    const data = await res.json()
    sessionId = data.sessionId
    return sessionId!
  } catch {
    // Generate a local UUID if API call fails
    sessionId = crypto.randomUUID?.() || Math.random().toString(36).substring(2)
    return sessionId
  }
}

export async function* streamChat(
  message: string,
  sid: string
): AsyncGenerator<SSEEvent> {
  const response = await fetch(`${API_BASE}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, sessionId: sid })
  })

  if (!response.ok) {
    yield { type: 'error', content: `请求失败: ${response.status}` }
    return
  }

  const reader = response.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // Process complete SSE lines
    const lines = buffer.split('\n')
    buffer = lines.pop() || '' // Keep incomplete line in buffer

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed || trimmed.startsWith(':')) continue // Skip comments and empty lines

      // SSE lines can be "field:value" or "field: value" — both valid
      const colonIdx = trimmed.indexOf(':')
      if (colonIdx === -1) continue

      const field = trimmed.substring(0, colonIdx)
      let value = trimmed.substring(colonIdx + 1)
      if (value.startsWith(' ')) value = value.substring(1) // strip optional space

      if (field !== 'data') continue

      // Handle data fields
      if (value === '[DONE]') {
        yield { type: 'done', content: null }
        continue
      }

      try {
        const parsed = JSON.parse(value)
        yield parsed as SSEEvent
      } catch {
        console.warn('Failed to parse SSE data:', value)
      }
    }
  }
}

export async function* streamImage(prompt: string): AsyncGenerator<SSEEvent> {
  const response = await fetch(`${API_BASE}/generate-image`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ prompt })
  })

  if (!response.ok) {
    yield { type: 'error', content: `图片生成请求失败: ${response.status}` }
    return
  }

  const reader = response.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed || trimmed.startsWith(':')) continue

      const colonIdx = trimmed.indexOf(':')
      if (colonIdx === -1) continue

      const field = trimmed.substring(0, colonIdx)
      let val = trimmed.substring(colonIdx + 1)
      if (val.startsWith(' ')) val = val.substring(1)

      if (field !== 'data') continue

      if (val === '[DONE]') {
        yield { type: 'done', content: null }
        continue
      }

      try {
        const parsed = JSON.parse(val)
        yield parsed as SSEEvent
      } catch {
        console.warn('Failed to parse SSE data:', val)
      }
    }
  }
}
