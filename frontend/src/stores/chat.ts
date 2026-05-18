import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types'
import { getSessionId, streamChat } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const sessionId = ref('')
  const isLoading = ref(false)
  const streamingContent = ref('')

  async function initSession() {
    sessionId.value = await getSessionId()
  }

  async function sendMessage(content: string) {
    if (!content.trim() || isLoading.value) return

    // Add user message
    messages.value.push({
      role: 'user',
      content: content.trim(),
      timestamp: Date.now()
    })

    isLoading.value = true
    streamingContent.value = ''

    // Add placeholder for assistant response
    const assistantMsg: ChatMessage = {
      role: 'assistant',
      content: '',
      timestamp: Date.now()
    }
    messages.value.push(assistantMsg)

    try {
      // Stream the response
      for await (const event of streamChat(content, sessionId.value)) {
        switch (event.type) {
          case 'text':
            streamingContent.value += event.content
            assistantMsg.content = streamingContent.value
            break
          case 'error':
            assistantMsg.content = event.content || '抱歉，出错了，请稍后重试。'
            break
          case 'done':
            // Streaming complete, ensure final content is saved
            assistantMsg.content = streamingContent.value
            break
        }
      }
    } catch (e: any) {
      assistantMsg.content = '抱歉，网络请求失败，请检查网络后重试。'
    } finally {
      isLoading.value = false
      streamingContent.value = ''
    }
  }

  function clearMessages() {
    messages.value = []
    streamingContent.value = ''
    sessionId.value = ''
    initSession()
  }

  return {
    messages,
    sessionId,
    isLoading,
    streamingContent,
    initSession,
    sendMessage,
    clearMessages
  }
})
