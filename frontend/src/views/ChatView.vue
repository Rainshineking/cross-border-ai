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
        <div class="welcome-icon">🌐</div>
        <h2>跨境选品小助手</h2>
        <p class="welcome-desc">
          我是你的 AI 选品与投流专家，可以帮你：
        </p>
        <div class="capabilities">
          <div class="capability">
            <el-icon :size="28"><Search /></el-icon>
            <span>选品推荐</span>
            <small>联网搜索热销品类，智能评分推荐</small>
          </div>
          <div class="capability">
            <el-icon :size="28"><TrendCharts /></el-icon>
            <span>投流建议</span>
            <small>广告策略、渠道推荐、预算分配</small>
          </div>
          <div class="capability">
            <el-icon :size="28"><PictureFilled /></el-icon>
            <span>创意配图</span>
            <small>AI 文生图，自动生成广告素材</small>
          </div>
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
  padding: 16px 0;
}

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.welcome h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.welcome-desc {
  color: var(--text-secondary);
  font-size: 15px;
  margin-bottom: 32px;
}

.capabilities {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.capability {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px 24px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  width: 180px;
  color: var(--primary-color);
}

.capability span {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.capability small {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
