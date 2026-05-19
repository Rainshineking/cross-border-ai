import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types'
import { getSessionId, streamChat, streamImage } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const sessionId = ref('')
  const isLoading = ref(false)
  const streamingContent = ref('')
  const isGeneratingImage = ref(false)
  const imageProgress = ref('')

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

  async function generateImage(prompt: string) {
    if (!prompt.trim() || isGeneratingImage.value) return

    // Add user message
    messages.value.push({
      role: 'user',
      content: `🎨 生成图片：${prompt.trim()}`,
      timestamp: Date.now()
    })

    isGeneratingImage.value = true
    imageProgress.value = ''

    // Add placeholder message for the image result
    const imageMsg: ChatMessage = {
      role: 'image',
      content: '正在生成图片...',
      timestamp: Date.now()
    }
    messages.value.push(imageMsg)

    try {
      for await (const event of streamImage(prompt.trim())) {
        switch (event.type) {
          case 'progress':
            imageProgress.value = event.content || ''
            imageMsg.content = imageProgress.value
            break
          case 'image':
            // Image generated successfully
            imageMsg.role = 'image'
            imageMsg.content = '图片生成完成'
            imageMsg.imageUrl = event.url
            imageMsg.prompt = event.prompt || prompt.trim()
            break
          case 'error':
            imageMsg.content = event.content || '图片生成失败，请稍后重试。'
            break
          case 'done':
            break
        }
      }
    } catch (e: any) {
      imageMsg.content = '图片生成请求失败，请检查网络后重试。'
    } finally {
      isGeneratingImage.value = false
      imageProgress.value = ''
    }
  }

  function clearMessages() {
    messages.value = []
    streamingContent.value = ''
    sessionId.value = ''
    imageProgress.value = ''
    initSession()
  }

  return {
    messages,
    sessionId,
    isLoading,
    streamingContent,
    isGeneratingImage,
    imageProgress,
    initSession,
    sendMessage,
    generateImage,
    clearMessages
  }
})
