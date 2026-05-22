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

    messages.value.push({
      role: 'user',
      content: content.trim(),
      timestamp: Date.now()
    })

    isLoading.value = true
    streamingContent.value = ''

    const msgIndex = messages.value.length
    messages.value.push({
      role: 'assistant',
      content: '',
      timestamp: Date.now()
    })

    try {
      for await (const event of streamChat(content, sessionId.value)) {
        switch (event.type) {
          case 'text':
            streamingContent.value += event.content
            // Update through reactive array reference
            messages.value[msgIndex] = {
              ...messages.value[msgIndex],
              content: streamingContent.value
            }
            break
          case 'error':
            messages.value[msgIndex] = {
              ...messages.value[msgIndex],
              content: event.content || '抱歉，出错了，请稍后重试。'
            }
            break
          case 'done':
            messages.value[msgIndex] = {
              ...messages.value[msgIndex],
              content: streamingContent.value
            }
            break
        }
      }
    } catch (e: any) {
      messages.value[msgIndex] = {
        ...messages.value[msgIndex],
        content: '抱歉，网络请求失败，请检查网络后重试。'
      }
    } finally {
      isLoading.value = false
      streamingContent.value = ''
    }
  }

  async function generateImage(prompt: string) {
    if (!prompt.trim() || isGeneratingImage.value) return

    messages.value.push({
      role: 'user',
      content: `🎨 生成图片：${prompt.trim()}`,
      timestamp: Date.now()
    })

    isGeneratingImage.value = true
    imageProgress.value = ''

    // Track index so we can update through the reactive array
    const imgIndex = messages.value.length
    messages.value.push({
      role: 'image',
      content: '正在生成图片...',
      timestamp: Date.now()
    })

    try {
      for await (const event of streamImage(prompt.trim())) {
        switch (event.type) {
          case 'progress':
            imageProgress.value = event.content || ''
            messages.value[imgIndex] = {
              ...messages.value[imgIndex],
              content: imageProgress.value
            }
            break
          case 'image':
            console.log('[ImageGen] received:', event)
            messages.value[imgIndex] = {
              role: 'image',
              content: '图片生成完成',
              imageUrl: event.url,
              prompt: event.prompt || prompt.trim(),
              timestamp: Date.now()
            }
            console.log('[ImageGen] updated message:', { ...messages.value[imgIndex] })
            break
          case 'error':
            messages.value[imgIndex] = {
              ...messages.value[imgIndex],
              content: event.content || '图片生成失败，请稍后重试。'
            }
            break
          case 'done':
            break
        }
      }
    } catch (e: any) {
      messages.value[imgIndex] = {
        ...messages.value[imgIndex],
        content: '图片生成请求失败，请检查网络后重试。'
      }
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
