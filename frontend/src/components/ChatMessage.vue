<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import type { ChatMessage as ChatMessageType } from '@/types'

const props = defineProps<{
  message: ChatMessageType
  isStreaming?: boolean
}>()

const isUser = computed(() => props.message.role === 'user')
const isImage = computed(() => props.message.role === 'image')
const hasImageUrl = computed(() => isImage.value && !!props.message.imageUrl)

const renderedContent = computed(() => {
  const content = props.message.content || ''
  if (isUser.value) return content
  return marked(content, { breaks: true })
})

function handleDownload() {
  if (props.message.imageUrl) {
    window.open(props.message.imageUrl, '_blank')
  }
}
</script>

<template>
  <div class="message-wrapper" :class="{
    'is-user': isUser,
    'is-assistant': !isUser && !isImage,
    'is-image': isImage
  }">
    <div class="message-avatar">
      <el-avatar :size="36"
        :icon="isUser ? 'UserFilled' : (isImage ? 'PictureFilled' : 'ChatDotSquare')"
        :style="{ background: isUser ? '#1890ff' : (isImage ? '#722ed1' : '#52c41a') }" />
    </div>
    <div class="message-body">
      <div class="message-role">
        {{ isUser ? '你' : (isImage ? '创意配图' : '跨境选品小助手') }}
      </div>
      <div class="message-content" :class="{ 'is-streaming': isStreaming }">
        <!-- User message: plain text -->
        <template v-if="isUser">
          <div class="user-text">{{ message.content }}</div>
        </template>

        <!-- Image message -->
        <template v-else-if="isImage">
          <div v-if="!hasImageUrl && isStreaming" class="image-loading">
            <el-icon class="loading-icon" :size="32"><PictureFilled /></el-icon>
            <span>{{ message.content || '正在生成图片...' }}</span>
          </div>
          <div v-else-if="hasImageUrl" class="image-result">
            <div class="image-wrapper">
              <img :src="message.imageUrl" :alt="message.prompt || '生成图片'"
                class="generated-image" @load="$emit('loaded')" />
            </div>
            <div v-if="message.prompt" class="image-prompt">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ message.prompt }}</span>
            </div>
            <div class="image-actions">
              <el-button size="small" type="primary" @click="handleDownload" :icon="'Download'">
                查看原图
              </el-button>
            </div>
          </div>
          <div v-else class="image-error">
            <el-icon :size="24" color="#ff4d4f"><WarningFilled /></el-icon>
            <span>{{ message.content }}</span>
          </div>
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

/* Image message styles */
.image-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  color: var(--text-muted);
}

.loading-icon {
  animation: spin 2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.image-result {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-wrapper {
  border-radius: 8px;
  overflow: hidden;
  max-height: 400px;
  display: flex;
  justify-content: center;
  background: #f5f5f5;
}

.generated-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  cursor: pointer;
}

.image-prompt {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
  padding: 4px 0;
}

.image-actions {
  display: flex;
  gap: 8px;
  padding-top: 4px;
}

.image-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  color: var(--danger-color);
}
</style>
