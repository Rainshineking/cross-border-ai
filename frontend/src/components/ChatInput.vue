<script setup lang="ts">
import { ref } from 'vue'

const emit = defineEmits<{
  send: [message: string]
  generateImage: [prompt: string]
}>()

const props = defineProps<{
  disabled?: boolean
}>()

const input = ref('')
const imageDialogVisible = ref(false)
const imagePrompt = ref('')

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

function openImageDialog() {
  imagePrompt.value = ''
  imageDialogVisible.value = true
}

function handleImageGenerate() {
  const prompt = imagePrompt.value.trim()
  if (!prompt || props.disabled) return
  imageDialogVisible.value = false
  emit('generateImage', prompt)
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
      <el-button
        size="small"
        icon="PictureFilled"
        @click="openImageDialog"
        :disabled="disabled"
        round
        type="success"
      >
        创意配图
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

    <!-- Image Generation Dialog -->
    <el-dialog
      v-model="imageDialogVisible"
      title="创意配图 - 文生图"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="image-dialog-body">
        <p class="image-dialog-hint">描述你想要生成的图片内容，AI 将根据描述自动生成广告图</p>
        <el-input
          v-model="imagePrompt"
          type="textarea"
          :rows="5"
          placeholder="例如：一款智能猫砂盆在客厅中，现代简约风格，柔和的自然光线，白色背景，产品突出展示，适合亚马逊主图风格"
          resize="none"
        />
      </div>
      <template #footer>
        <el-button @click="imageDialogVisible = false">取消</el-button>
        <el-button
          type="success"
          :disabled="!imagePrompt.trim()"
          @click="handleImageGenerate"
          :icon="'PictureFilled'"
        >
          开始生成
        </el-button>
      </template>
    </el-dialog>
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

.image-dialog-body {
  padding: 8px 0;
}

.image-dialog-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  line-height: 1.5;
}
</style>
