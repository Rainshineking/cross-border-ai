<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import ChatMessage from '@/components/ChatMessage.vue'
import ChatInput from '@/components/ChatInput.vue'

const chatStore = useChatStore()
const messagesContainer = ref<HTMLElement | null>(null)
const isAtBottom = ref(true)

onMounted(() => {
  if (!chatStore.sessionId) {
    chatStore.initSession()
  }
  scrollToBottom()
})

async function handleSend(message: string) {
  await chatStore.sendMessage(message)
  await nextTick()
  scrollToBottom()
}

async function handleGenerateImage(prompt: string) {
  await chatStore.generateImage(prompt)
  await nextTick()
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function handleScroll() {
  if (!messagesContainer.value) return
  const el = messagesContainer.value
  isAtBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 50
}

watch(() => chatStore.streamingContent, () => {
  if (isAtBottom.value) {
    nextTick(scrollToBottom)
  }
})

watch(() => chatStore.imageProgress, () => {
  if (isAtBottom.value) {
    nextTick(scrollToBottom)
  }
})
</script>

<template>
  <div class="chat-view">
    <div
      ref="messagesContainer"
      class="messages-area"
      @scroll="handleScroll"
    >
      <!-- Welcome Screen -->
      <div v-if="chatStore.messages.length === 0" class="welcome">
        <div class="welcome-badge">
          <div class="welcome-icon-wrapper">
            <span class="welcome-icon">🌐</span>
          </div>
        </div>
        <h2 class="welcome-title">国创智联</h2>
        <p class="welcome-desc">
          我是你的 AI 选品与投流专家，可以帮你：
        </p>
        <div class="capabilities">
          <div class="capability" @click="chatStore.sendMessage('帮我推荐最近在亚马逊美国站热销的宠物用品')">
            <div class="capability-icon" style="background: var(--primary-bg); color: var(--primary-color);">
              <el-icon :size="24"><Search /></el-icon>
            </div>
            <div class="capability-content">
              <span class="capability-title">选品推荐</span>
              <span class="capability-desc">联网搜索热销品类，智能评分推荐</span>
            </div>
            <el-icon class="capability-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
          </div>
          <div class="capability" @click="chatStore.sendMessage('我想推广一款智能猫砂盆，请给我投流建议')">
            <div class="capability-icon" style="background: var(--success-bg); color: var(--success-color);">
              <el-icon :size="24"><TrendCharts /></el-icon>
            </div>
            <div class="capability-content">
              <span class="capability-title">投流建议</span>
              <span class="capability-desc">广告策略、渠道推荐、预算分配</span>
            </div>
            <el-icon class="capability-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
          </div>
          <div class="capability" @click="chatStore.sendMessage('帮我为智能宠物喂食器写一段广告文案和卖点描述')">
            <div class="capability-icon" style="background: var(--warning-bg); color: var(--warning-color);">
              <el-icon :size="24"><EditPen /></el-icon>
            </div>
            <div class="capability-content">
              <span class="capability-title">创意文案</span>
              <span class="capability-desc">广告文案撰写、卖点提炼</span>
            </div>
            <el-icon class="capability-arrow" color="var(--text-muted)"><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="welcome-footer">
          <span>点击上方快捷方式开始，或在下方输入你的需求</span>
        </div>
      </div>

      <!-- Messages -->
      <div v-for="(msg, index) in chatStore.messages" :key="index">
        <ChatMessage
          :message="msg"
          :is-streaming="(chatStore.isLoading || chatStore.isGeneratingImage) && index === chatStore.messages.length - 1"
        />
      </div>
    </div>

    <!-- Input -->
    <ChatInput
      :disabled="chatStore.isLoading || chatStore.isGeneratingImage"
      @send="handleSend"
      @generate-image="handleGenerateImage"
    />
  </div>
</template>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  scroll-behavior: smooth;
}

/* Welcome screen */
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 40px 24px 80px;
  text-align: center;
  animation: fadeIn 0.5s ease;
}

.welcome-badge {
  margin-bottom: 20px;
}

.welcome-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 24px;
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(24, 144, 255, 0.35);
  animation: slideUp 0.5s ease;
}

.welcome-icon {
  font-size: 40px;
  line-height: 1;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
  animation: slideUp 0.5s ease 0.1s both;
}

.welcome-desc {
  color: var(--text-secondary);
  font-size: 16px;
  margin-bottom: 36px;
  animation: slideUp 0.5s ease 0.15s both;
}

.capabilities {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-width: 480px;
  animation: slideUp 0.5s ease 0.2s both;
}

.capability {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--card-bg);
  border: var(--card-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  text-align: left;
  box-shadow: var(--card-shadow);
}

.capability:hover {
  border-color: var(--primary-color);
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

.capability:active {
  transform: translateY(0);
}

.capability-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.capability-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.capability-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.capability-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.4;
}

.capability-arrow {
  flex-shrink: 0;
  transition: transform var(--transition-normal);
}

.capability:hover .capability-arrow {
  transform: translateX(4px);
}

.welcome-footer {
  margin-top: 32px;
  font-size: 12px;
  color: var(--text-placeholder);
  animation: slideUp 0.5s ease 0.3s both;
}
</style>