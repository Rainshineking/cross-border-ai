<script setup lang="ts">
import { onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useRoute } from 'vue-router'
import Sidebar from '@/components/Sidebar.vue'

const chatStore = useChatStore()
const route = useRoute()

onMounted(() => {
  chatStore.initSession()
})

function getPageTitle(): string {
  return (route.meta?.title as string) || '跨境选品小助手'
}
</script>

<template>
  <div class="app-container">
    <Sidebar />
    <main class="main-content">
      <header class="app-header">
        <h1>{{ getPageTitle() }}</h1>
      </header>
      <div class="content-area">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.app-header h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.content-area {
  flex: 1;
  overflow: hidden;
  position: relative;
}
</style>
