<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import type { ChatMessage as ChatMessageType } from '@/types'

const props = defineProps<{
  message: ChatMessageType
  isStreaming?: boolean
}>()

const isUser = computed(() => props.message.role === 'user')

const renderedContent = computed(() => {
  const content = props.message.content || ''
  if (isUser.value) return content
  return marked(content, { breaks: true })
})
</script>

<template>
  <div class="message-wrapper" :class="{ 'is-user': isUser, 'is-assistant': !isUser }">
    <div class="message-avatar">
      <el-avatar :size="36" :icon="isUser ? 'UserFilled' : 'ChatDotSquare'"
        :style="{ background: isUser ? '#1890ff' : '#52c41a' }" />
    </div>
    <div class="message-body">
      <div class="message-role">{{ isUser ? '你' : '跨境选品小助手' }}</div>
      <div class="message-content" :class="{ 'is-streaming': isStreaming }">
        <!-- User message: plain text -->
        <template v-if="isUser">
          <div class="user-text">{{ message.content }}</div>
        </template>
        <!-- Assistant message: rendered markdown -->
        <template v-else>
          <div v-if="!message.content && isStreaming" class="thinking">
            <span class="dot-pulse"></span>
          </div>
          <div v-else class="chat-message-content" v-html="renderedContent"></div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-wrapper {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  max-width: 860px;
  margin: 0 auto;
  width: 100%;
}

.message-wrapper.is-user {
  flex-direction: row-reverse;
}

.message-body {
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 48px);
}

.is-user .message-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-role {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 6px;
  font-weight: 500;
}

.message-content {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 12px 16px;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.is-user .message-content {
  background: var(--primary-color);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.is-assistant .message-content {
  border-bottom-left-radius: 4px;
}

.is-streaming {
  border-left: 3px solid var(--primary-color);
}

.user-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.thinking {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.dot-pulse {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}
</style>
