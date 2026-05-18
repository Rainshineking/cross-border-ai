<script setup lang="ts">
import { ref, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
])

// Sample data for demonstration
const marketTrendOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['搜索热度', '销量指数'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'], axisLabel: { fontSize: 12 } },
  yAxis: { type: 'value' },
  series: [
    {
      name: '搜索热度',
      type: 'line',
      smooth: true,
      data: [120, 132, 101, 134, 190, 230],
      lineStyle: { color: '#1890ff', width: 3 },
      areaStyle: { color: 'rgba(24,144,255,0.1)' },
      itemStyle: { color: '#1890ff' }
    },
    {
      name: '销量指数',
      type: 'bar',
      barWidth: '35%',
      data: [85, 92, 78, 110, 145, 178],
      itemStyle: { color: '#52c41a', borderRadius: [4, 4, 0, 0] }
    }
  ]
})

const categoryOption = ref({
  tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: false,
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: [
        { value: 32, name: '宠物用品', itemStyle: { color: '#1890ff' } },
        { value: 25, name: '家居生活', itemStyle: { color: '#52c41a' } },
        { value: 18, name: '运动户外', itemStyle: { color: '#faad14' } },
        { value: 15, name: '美妆个护', itemStyle: { color: '#ff4d4f' } },
        { value: 10, name: '电子配件', itemStyle: { color: '#722ed1' } }
      ]
    }
  ]
})

const trendComparisonOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['宠物用品', '家居生活', '运动户外'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
  yAxis: { type: 'value', name: '搜索热度指数' },
  series: [
    { name: '宠物用品', type: 'line', smooth: true, data: [120, 132, 150, 180, 210, 260], lineStyle: { color: '#1890ff' }, itemStyle: { color: '#1890ff' } },
    { name: '家居生活', type: 'line', smooth: true, data: [100, 115, 130, 145, 160, 175], lineStyle: { color: '#52c41a' }, itemStyle: { color: '#52c41a' } },
    { name: '运动户外', type: 'line', smooth: true, data: [80, 90, 105, 120, 140, 155], lineStyle: { color: '#faad14' }, itemStyle: { color: '#faad14' } }
  ]
})

// Mock recommendations
const recommendations = ref([
  { name: '智能猫砂盆', platform: 'Amazon', price: '$89.99', score: 85, competition: '中', trend: 'up' },
  { name: '自动喂食器', platform: 'Amazon', price: '$59.99', score: 82, competition: '高', trend: 'up' },
  { name: '宠物摄像头', platform: 'Temu', price: '$35.99', score: 78, competition: '中', trend: 'up' },
  { name: '智能饮水机', platform: 'Amazon', price: '$45.99', score: 76, competition: '高', trend: 'stable' },
  { name: '宠物毛发梳', platform: 'Temu', price: '$12.99', score: 72, competition: '低', trend: 'up' }
])

function scoreColor(score: number): string {
  if (score >= 80) return 'var(--success-color)'
  if (score >= 70) return 'var(--warning-color)'
  return 'var(--danger-color)'
}
</script>

<template>
  <div class="dashboard-view">
    <div class="dashboard-grid">
      <!-- Market Trend -->
      <el-card class="dashboard-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>市场热度趋势</span>
            <el-tag size="small" type="info">近 6 个月</el-tag>
          </div>
        </template>
        <v-chart :option="marketTrendOption" style="height: 280px" autoresize />
      </el-card>

      <!-- Category Distribution -->
      <el-card class="dashboard-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>品类热度分布</span>
            <el-tag size="small" type="info">本月</el-tag>
          </div>
        </template>
        <v-chart :option="categoryOption" style="height: 280px" autoresize />
      </el-card>

      <!-- Trend Comparison -->
      <el-card class="dashboard-card card-wide" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>品类热度对比</span>
            <el-tag size="small" type="info">Top 3 品类</el-tag>
          </div>
        </template>
        <v-chart :option="trendComparisonOption" style="height: 280px" autoresize />
      </el-card>

      <!-- Recommendations -->
      <el-card class="dashboard-card card-wide" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>AI 选品推荐排行</span>
            <el-tag size="small" type="success">评分排行</el-tag>
          </div>
        </template>
        <div class="recommend-list">
          <div v-for="(item, index) in recommendations" :key="index" class="recommend-item">
            <div class="rank" :class="{ 'top': index < 3 }">{{ index + 1 }}</div>
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-tags">
                <el-tag size="small">{{ item.platform }}</el-tag>
                <el-tag size="small" type="warning">{{ item.price }}</el-tag>
                <el-tag size="small" :type="item.competition === '低' ? 'success' : 'warning'">
                  竞争: {{ item.competition }}
                </el-tag>
              </div>
            </div>
            <div class="item-score" :style="{ color: scoreColor(item.score) }">
              <div class="score-value">{{ item.score }}</div>
              <div class="score-label">推荐指数</div>
            </div>
            <div class="item-trend">
              <el-icon v-if="item.trend === 'up'" color="#52c41a" :size="20"><Top /></el-icon>
              <el-icon v-else color="#909399" :size="20"><Minus /></el-icon>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard-view {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.card-wide {
  grid-column: 1 / -1;
}

.dashboard-card {
  border-radius: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 600;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 8px;
  border-bottom: 1px solid var(--border-color);
  transition: background 0.2s;
}

.recommend-item:last-child {
  border-bottom: none;
}

.recommend-item:hover {
  background: var(--bg-color);
  border-radius: 8px;
}

.rank {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 600;
  background: var(--bg-color);
  color: var(--text-muted);
}

.rank.top {
  background: var(--primary-color);
  color: white;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.item-tags {
  display: flex;
  gap: 4px;
}

.item-score {
  text-align: center;
  min-width: 60px;
}

.score-value {
  font-size: 22px;
  font-weight: 700;
}

.score-label {
  font-size: 11px;
  color: var(--text-muted);
}

.item-trend {
  width: 30px;
  display: flex;
  justify-content: center;
}
</style>
