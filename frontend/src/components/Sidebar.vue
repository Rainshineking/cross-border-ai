<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useChatStore } from '@/stores/chat'

const router = useRouter()
const route = useRoute()
const chatStore = useChatStore()

const navItems = [
  { path: '/chat', name: 'AI 对话', icon: 'ChatDotSquare' },
  { path: '/dashboard', name: '数据看板', icon: 'DataBoard' }
]

function navigate(path: string) {
  router.push(path)
}

function handleNewChat() {
  chatStore.clearMessages()
  router.push('/chat')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-icon-wrapper">
          <span class="logo-icon">🌐</span>
        </div>
        <div class="logo-text">
          <span class="logo-title">国创智联</span>
        </div>
      </div>
    </div>

    <nav class="nav-list">
      <div
        v-for="item in navItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: route.path === item.path }"
        @click="navigate(item.path)"
      >
        <div class="nav-indicator"></div>
        <el-icon :size="20">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.name }}</span>
      </div>
    </nav>

    <div class="sidebar-footer">
      <button class="new-chat-btn" @click="handleNewChat">
        <el-icon><Plus /></el-icon>
        <span>新建对话</span>
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  background: var(--card-bg);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.sidebar::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(to bottom, transparent, var(--border-color) 20%, var(--border-color) 80%, transparent);
}

.sidebar-header {
  padding: 24px 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.logo-icon {
  font-size: 24px;
  line-height: 1;
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.logo-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.2;
}

.nav-list {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 16px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  background: var(--primary-bg);
  color: var(--primary-color);
}

.nav-item.active {
  background: var(--primary-bg);
  color: var(--primary-color);
  font-weight: 600;
}

.nav-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 20px;
  border-radius: 0 3px 3px 0;
  background: var(--primary-gradient);
  transition: transform var(--transition-normal);
}

.nav-item.active .nav-indicator {
  transform: translateY(-50%) scaleY(1);
}

.sidebar-footer {
  padding: 16px 12px;
}

.new-chat-btn {
  width: 100%;
  padding: 11px 16px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--primary-gradient);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all var(--transition-normal);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.25);
}

.new-chat-btn:hover {
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.35);
  transform: translateY(-1px);
}

.new-chat-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(24, 144, 255, 0.2);
}
</style>