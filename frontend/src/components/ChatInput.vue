<script setup lang="ts">
import { ref, computed } from 'vue'

const emit = defineEmits<{
  send: [message: string]
  generateImage: [prompt: string]
}>()

const props = defineProps<{
  disabled?: boolean
}>()

type InputMode = 'text' | 'image'

const input = ref('')
const mode = ref<InputMode>('text')

const textActions = [
  { label: '选品推荐', icon: 'Search', prompt: '帮我推荐最近在亚马逊美国站热销的宠物用品' },
  { label: '投流建议', icon: 'TrendCharts', prompt: '我想推广一款智能猫砂盆，请给我投流建议' },
  { label: '创意文案', icon: 'EditPen', prompt: '帮我为智能宠物喂食器写一段广告文案和卖点描述' }
]

const imageExamples = [
  { label: '亚马逊主图', prompt: '一款智能猫砂盆在客厅中，现代简约风格，白色背景，产品突出展示，适合亚马逊主图' },
  { label: '场景图', prompt: '宠物用品场景图，温暖的家庭环境，自然光线，产品实拍风格' },
  { label: '白底产品图', prompt: '极简白底产品图，高清细节展示，电商产品摄影风格' }
]

const isImageMode = computed(() => mode.value === 'image')

const placeholderText = computed(() => {
  if (props.disabled) return '请等待当前任务完成...'
  return isImageMode.value
    ? '描述你想要生成的图片内容，例如：一只橘猫在窗边晒太阳'
    : '输入你的需求，例如：最近美国站有什么好卖的宠物用品？'
})

const hintText = computed(() => {
  const base = isImageMode.value ? 'Enter 生成图片 · Shift+Enter 换行' : 'Enter 发送 · Shift+Enter 换行'
  return base
})

function handleSend() {
  const text = input.value.trim()
  if (!text || props.disabled) return

  if (isImageMode.value) {
    emit('generateImage', text)
  } else {
    emit('send', text)
  }
  input.value = ''
}

function handleQuickAction(prompt: string) {
  if (props.disabled) return
  if (isImageMode.value) {
    emit('generateImage', prompt)
  } else {
    emit('send', prompt)
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function autoResize(event: Event) {
  const target = event.target as HTMLTextAreaElement
  if (target) {
    target.style.height = 'auto'
    target.style.height = Math.min(target.scrollHeight, 120) + 'px'
  }
}
</script>

<template>
  <div class="chat-input-area" :class="{ 'image-mode': isImageMode }">
    <!-- Mode toggle -->
    <div class="mode-toggle">
      <button
        class="mode-btn"
        :class="{ active: mode === 'text' }"
        :disabled="disabled"
        @click="mode = 'text'"
      >
        <el-icon :size="15"><ChatDotSquare /></el-icon>
        <span>文字对话</span>
      </button>
      <button
        class="mode-btn"
        :class="{ active: mode === 'image' }"
        :disabled="disabled"
        @click="mode = 'image'"
      >
        <el-icon :size="15"><PictureFilled /></el-icon>
        <span>文生图</span>
      </button>
    </div>

    <!-- Quick action chips: text mode -->
    <div v-if="!isImageMode" class="quick-actions">
      <button
        v-for="action in textActions"
        :key="action.label"
        class="action-chip"
        :disabled="disabled"
        @click="handleQuickAction(action.prompt)"
      >
        <el-icon :size="14">
          <component :is="action.icon" />
        </el-icon>
        <span>{{ action.label }}</span>
      </button>
    </div>

    <!-- Quick action chips: image mode -->
    <div v-else class="quick-actions">
      <button
        v-for="example in imageExamples"
        :key="example.label"
        class="action-chip action-image"
        :disabled="disabled"
        @click="handleQuickAction(example.prompt)"
      >
        <span class="chip-dot"></span>
        <span>{{ example.label }}</span>
      </button>
    </div>

    <!-- Input row -->
    <div class="input-row">
      <div class="input-wrapper" :class="{ 'wrapper-image': isImageMode }">
        <textarea
          v-model="input"
          :disabled="disabled"
          :placeholder="placeholderText"
          class="chat-textarea"
          rows="1"
          @keydown="handleKeydown"
          @input="autoResize"
        ></textarea>
      </div>
      <button
        class="send-button"
        :class="{
          active: input.trim() && !disabled && !isImageMode,
          'active-image': input.trim() && !disabled && isImageMode
        }"
        :disabled="disabled || !input.trim()"
        @click="handleSend"
      >
        <el-icon :size="18">
          <component :is="isImageMode ? 'PictureFilled' : 'Promotion'" />
        </el-icon>
      </button>
    </div>

    <!-- Footer hint -->
    <div class="input-footer">
      <span class="hint">{{ hintText }}</span>
      <span v-if="isImageMode" class="image-badge">
        <el-icon :size="12"><PictureFilled /></el-icon>
        文生图模式
      </span>
    </div>
  </div>
</template>

<style scoped>
.chat-input-area {
  padding: 6px 24px 14px;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  flex-shrink: 0;
  position: relative;
  transition: all var(--transition-normal);
}

.chat-input-area.image-mode {
  background: linear-gradient(to top, rgba(114, 46, 209, 0.03), var(--card-bg));
}

/* Mode toggle */
.mode-toggle {
  display: flex;
  gap: 0;
  margin-bottom: 10px;
  background: var(--bg-color);
  border-radius: 10px;
  padding: 3px;
  width: fit-content;
  border: 1px solid var(--border-color);
}

.mode-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 18px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
  white-space: nowrap;
}

.mode-btn:hover:not(:disabled) {
  color: var(--text-secondary);
}

.mode-btn.active {
  background: var(--card-bg);
  color: var(--text-primary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.mode-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.mode-btn:last-child.active {
  color: #722ed1;
}

.mode-btn:last-child.active .el-icon {
  color: #722ed1;
}

/* Quick action chips */
.quick-actions {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
  min-height: 30px;
}

.action-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--card-bg);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  font-family: inherit;
}

.action-chip:hover:not(:disabled) {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-bg);
}

.action-chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-image {
  background: rgba(114, 46, 209, 0.06);
  border-color: transparent;
  color: #722ed1;
}

.action-image:hover:not(:disabled) {
  background: rgba(114, 46, 209, 0.12);
  border-color: #722ed1;
}

.chip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #722ed1;
}

/* Input row */
.input-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.input-wrapper {
  flex: 1;
  background: var(--bg-color);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  padding: 8px 14px;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1);
}

.wrapper-image {
  border-color: rgba(114, 46, 209, 0.3);
}

.wrapper-image:focus-within {
  border-color: #722ed1;
  box-shadow: 0 0 0 3px rgba(114, 46, 209, 0.1);
}

.chat-textarea {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 15.5px;
  color: var(--text-primary);
  line-height: 1.5;
  resize: none;
  min-height: 24px;
  max-height: 120px;
}

.chat-textarea::placeholder {
  color: var(--text-placeholder);
}

.chat-textarea:disabled {
  cursor: not-allowed;
}

/* Send button */
.send-button {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--bg-color);
  color: var(--text-muted);
  transition: all var(--transition-fast);
  flex-shrink: 0;
  margin-bottom: 2px;
}

.send-button.active {
  background: var(--primary-gradient);
  color: white;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.send-button.active:hover {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
  transform: scale(1.05);
}

.send-button.active-image {
  background: linear-gradient(135deg, #722ed1, #531dab);
  color: white;
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.3);
}

.send-button.active-image:hover {
  box-shadow: 0 4px 12px rgba(114, 46, 209, 0.4);
  transform: scale(1.05);
}

.send-button:disabled {
  cursor: not-allowed;
}

/* Footer */
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.hint {
  font-size: 12px;
  color: var(--text-placeholder);
}

.image-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #722ed1;
  background: rgba(114, 46, 209, 0.08);
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
}
</style>