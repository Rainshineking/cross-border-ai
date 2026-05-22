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
  <div class="message-row" :class="{
    'row-user': isUser,
    'row-assistant': !isUser && !isImage,
    'row-image': isImage
  }">
    <!-- Assistant avatar (top-left of each assistant message group) -->
    <div v-if="!isUser" class="row-avatar">
      <div class="av-circle" :class="{ 'av-image': isImage }">
        <el-icon :size="15">
          <component :is="isImage ? 'PictureFilled' : 'ChatDotSquare'" />
        </el-icon>
      </div>
    </div>

    <div class="row-body">
      <!-- User message -->
      <div v-if="isUser" class="msg user-msg">
        <div class="user-text">{{ message.content }}</div>
      </div>

      <!-- Image message -->
      <template v-else-if="isImage">
        <div class="msg image-msg">
          <div v-if="!hasImageUrl && isStreaming" class="img-loading">
            <div class="img-spinner"></div>
            <div class="img-loading-text">
              <span class="img-loading-label">正在生成图片</span>
              <span class="img-loading-dots">
                <span class="idot"></span>
                <span class="idot"></span>
                <span class="idot"></span>
              </span>
            </div>
            <span class="img-loading-hint">AI 正在创作中，请稍候...</span>
          </div>
          <div v-else-if="hasImageUrl" class="img-result">
            <div class="img-wrap">
              <img :src="message.imageUrl" :alt="message.prompt || '生成图片'"
                class="img-show" />
            </div>
            <div v-if="message.prompt" class="img-prompt">
              <el-icon :size="13"><InfoFilled /></el-icon>
              <span>{{ message.prompt }}</span>
            </div>
            <div class="img-actions">
              <button class="dl-btn" @click="handleDownload">
                <el-icon :size="14"><Download /></el-icon>
                查看原图
              </button>
            </div>
          </div>
          <div v-else class="img-error">
            <el-icon :size="18" color="#ff4d4f"><WarningFilled /></el-icon>
            <span>{{ message.content }}</span>
          </div>
        </div>
      </template>

      <!-- Assistant message -->
      <template v-else>
        <div class="msg assistant-msg">
          <template v-if="isStreaming && !message.content">
            <div class="thinking">
              <span class="tdot"></span>
              <span class="tdot"></span>
              <span class="tdot"></span>
            </div>
          </template>
          <template v-else-if="isStreaming">
            <div class="chat-message-content streaming" v-html="renderedContent"></div>
          </template>
          <template v-else>
            <div class="chat-message-content" v-html="renderedContent"></div>
          </template>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.message-row {
  display: flex;
  gap: 10px;
  padding: 4px 24px;
  max-width: 820px;
  margin: 0 auto;
  width: 100%;
  animation: fadeIn 0.25s ease;
}

.row-user {
  justify-content: flex-end;
}

/* Avatar */
.row-avatar {
  flex-shrink: 0;
  padding-top: 4px;
}

.av-circle {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: linear-gradient(135deg, #52c41a, #389e0d);
  box-shadow: 0 1px 4px rgba(82, 196, 26, 0.25);
}

.av-image {
  background: linear-gradient(135deg, #722ed1, #531dab);
  box-shadow: 0 1px 4px rgba(114, 46, 209, 0.25);
}

/* Body */
.row-body {
  min-width: 0;
  max-width: 78%;
}

.row-user .row-body {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* Messages */
.msg {
  line-height: 1.65;
  position: relative;
}

/* User message — ChatGPT style: light gray bubble */
.user-msg {
  background: #f0f0f0;
  color: var(--text-primary);
  border-radius: 18px;
  padding: 10px 16px;
  font-size: 15.5px;
  width: fit-content;
  max-width: 100%;
}

.user-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

/* Assistant message — clean white, flat */
.assistant-msg {
  background: transparent;
  padding: 4px 0;
  font-size: 15.5px;
}

/* Thinking dots */
.thinking {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 4px;
}

.tdot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-muted);
  animation: dotPulse 1.4s ease-in-out infinite;
}

.tdot:nth-child(2) { animation-delay: 0.16s; }
.tdot:nth-child(3) { animation-delay: 0.32s; }

/* Streaming cursor */
.streaming::after {
  content: '|';
  display: inline;
  animation: blink 0.7s step-end infinite;
  color: var(--text-muted);
  font-weight: 300;
  margin-left: 1px;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* Image message */
.image-msg {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  padding: 12px;
  box-shadow: var(--card-shadow);
  min-width: 240px;
}

.img-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 32px 20px;
  color: var(--text-muted);
  border: 1.5px dashed rgba(114, 46, 209, 0.15);
  border-radius: var(--radius-sm);
  background: rgba(114, 46, 209, 0.02);
}

.img-spinner {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 3px solid rgba(114, 46, 209, 0.1);
  border-top-color: #722ed1;
  animation: imgSpin 0.8s linear infinite;
}

@keyframes imgSpin {
  to { transform: rotate(360deg); }
}

.img-loading-text {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
}

.img-loading-dots {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-left: 2px;
}

.idot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #722ed1;
  animation: dotPulse 1.4s ease-in-out infinite;
}

.idot:nth-child(2) { animation-delay: 0.16s; }
.idot:nth-child(3) { animation-delay: 0.32s; }

.img-loading-hint {
  font-size: 12px;
  color: var(--text-placeholder);
}

.img-result {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.img-wrap {
  border-radius: var(--radius-sm);
  overflow: hidden;
  max-height: 360px;
  display: flex;
  justify-content: center;
  background: #f5f5f5;
}

.img-show {
  max-width: 100%;
  max-height: 360px;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.img-prompt {
  display: flex;
  align-items: flex-start;
  gap: 5px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
}

.img-prompt span { flex: 1; }

.img-actions {
  display: flex;
  gap: 6px;
}

.dl-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--card-bg);
  color: var(--text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
}

.dl-btn:hover {
  background: var(--bg-color);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.img-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px;
  color: var(--danger-color);
  font-size: 14px;
}
</style>