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
  return (route.meta?.title as string) || '国创智联'
}
</script>

<template>
  <div class="app-container">
    <Sidebar />
    <main class="main-content">
      <div class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
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
  background: #ffffff;
  position: relative;
}

.content-area {
  flex: 1;
  overflow: hidden;
  position: relative;
}
</style>