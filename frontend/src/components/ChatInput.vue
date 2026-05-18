<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  send: [message: string]
}>()

const props = defineProps<{
  disabled?: boolean
}>()

const input = ref('')

const quickActions = [
  { label: '选品推荐', icon: 'Search', prompt: '帮我推荐最近在亚马逊美国站热销的宠物用品' },
  { label: '投流建议', icon: 'TrendCharts', prompt: '我想推广一款智能猫砂盆，请给我投流建议' },
  { label: '创意文案', icon: 'EditPen', prompt: '帮我为智能宠物喂食器写一段广告文案和卖点描述' }
]

function handleSend() {
  const text = input.value.trim()
  if (!text || props.disabled) return
  emit('send', text)
  input.value = ''
}

function handleQuickAction(prompt: string) {
  if (props.disabled) return
  emit('send', prompt)
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="chat-input-area">
    <div class="quick-actions">
      <el-button
        v-for="action in quickActions"
        :key="action.label"
        size="small"
        :icon="action.icon"
        @click="handleQuickAction(action.prompt)"
        :disabled="disabled"
        round
      >
        {{ action.label }}
      </el-button>
    </div>
    <div class="input-wrapper">
      <el-input
        v-model="input"
        type="textarea"
        :rows="3"
        :disabled="disabled"
        placeholder="输入你的需求，例如：最近美国站有什么好卖的宠物用品？"
        @keydown="handleKeydown"
        resize="none"
      />
      <el-button
        type="primary"
        :icon="'Promotion'"
        :disabled="disabled || !input.trim()"
        @click="handleSend"
        class="send-btn"
        circle
      />
    </div>
    <div class="input-footer">
      <span class="hint">按 Enter 发送，Shift+Enter 换行</span>
    </div>
  </div>
</template>

<style scoped>
.chat-input-area {
  padding: 12px 24px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--card-bg);
  flex-shrink: 0;
}

.quick-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.input-wrapper {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.send-btn {
  flex-shrink: 0;
  margin-bottom: 2px;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}

.hint {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
