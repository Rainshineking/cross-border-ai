<script setup lang="ts">
import { ref } from 'vue'
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

// 1. Market Trend Chart
const marketTrendOption = ref({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#e8e8ef',
    borderWidth: 1,
    textStyle: { color: '#1a1a2e', fontSize: 12 },
    formatter: function(params: any) {
      let html = `<strong style="font-size:13px;">${params[0].axisValue}</strong><br/>`
      params.forEach((p: any) => {
        html += `<span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${p.color};margin-right:6px;"></span>`
        html += `${p.seriesName}: <strong>${p.value}</strong><br/>`
      })
      return html
    }
  },
  legend: {
    data: ['搜索热度', '销量指数'],
    bottom: 0,
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    textStyle: { color: '#4a4a6a', fontSize: 12 }
  },
  grid: { left: '3%', right: '4%', bottom: '14%', top: '6%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月'],
    axisLine: { lineStyle: { color: '#e8e8ef' } },
    axisLabel: { color: '#8e8ea0', fontSize: 11 },
    axisTick: { show: false }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#e8e8ef', type: 'dashed' } },
    axisLabel: { color: '#8e8ea0', fontSize: 11 }
  },
  series: [
    {
      name: '搜索热度',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: [120, 132, 101, 134, 190, 230],
      lineStyle: { color: '#1890ff', width: 2.5 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24,144,255,0.25)' },
            { offset: 1, color: 'rgba(24,144,255,0.02)' }
          ]
        }
      },
      itemStyle: { color: '#1890ff' }
    },
    {
      name: '销量指数',
      type: 'bar',
      barWidth: '32%',
      data: [85, 92, 78, 110, 145, 178],
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(82,196,26,0.8)' },
            { offset: 1, color: 'rgba(82,196,26,0.4)' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      }
    }
  ]
})

// 2. Category Distribution Pie
const categoryOption = ref({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#e8e8ef',
    borderWidth: 1,
    textStyle: { color: '#1a1a2e', fontSize: 12 },
    formatter: '{b}: <strong>{d}%</strong>'
  },
  series: [
    {
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      padAngle: 2,
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 11,
        color: '#4a4a6a',
        lineHeight: 16
      },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.15)' }
      },
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

// 3. Trend Comparison
const trendComparisonOption = ref({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#e8e8ef',
    borderWidth: 1,
    textStyle: { color: '#1a1a2e', fontSize: 12 }
  },
  legend: {
    data: ['宠物用品', '家居生活', '运动户外'],
    bottom: 0,
    icon: 'circle',
    itemWidth: 8,
    itemHeight: 8,
    textStyle: { color: '#4a4a6a', fontSize: 12 }
  },
  grid: { left: '3%', right: '4%', bottom: '14%', top: '6%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月'],
    axisLine: { lineStyle: { color: '#e8e8ef' } },
    axisLabel: { color: '#8e8ea0', fontSize: 11 },
    axisTick: { show: false }
  },
  yAxis: {
    type: 'value',
    name: '搜索热度指数',
    nameTextStyle: { color: '#8e8ea0', fontSize: 11 },
    splitLine: { lineStyle: { color: '#e8e8ef', type: 'dashed' } },
    axisLabel: { color: '#8e8ea0', fontSize: 11 }
  },
  series: [
    {
      name: '宠物用品',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: [120, 132, 150, 180, 210, 260],
      lineStyle: { color: '#1890ff', width: 2.5 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(24,144,255,0.15)' }, { offset: 1, color: 'rgba(24,144,255,0.02)' }] } },
      itemStyle: { color: '#1890ff' }
    },
    {
      name: '家居生活',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: [100, 115, 130, 145, 160, 175],
      lineStyle: { color: '#52c41a', width: 2.5 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(82,196,26,0.15)' }, { offset: 1, color: 'rgba(82,196,26,0.02)' }] } },
      itemStyle: { color: '#52c41a' }
    },
    {
      name: '运动户外',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: [80, 90, 105, 120, 140, 155],
      lineStyle: { color: '#faad14', width: 2.5 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(250,173,20,0.15)' }, { offset: 1, color: 'rgba(250,173,20,0.02)' }] } },
      itemStyle: { color: '#faad14' }
    }
  ]
})

// 4. Recommendations
const recommendations = ref([
  { name: '智能猫砂盆', platform: 'Amazon', price: '$89.99', score: 85, competition: '中', trend: 'up' },
  { name: '自动喂食器', platform: 'Amazon', price: '$59.99', score: 82, competition: '高', trend: 'up' },
  { name: '宠物摄像头', platform: 'Temu', price: '$35.99', score: 78, competition: '中', trend: 'up' },
  { name: '智能饮水机', platform: 'Amazon', price: '$45.99', score: 76, competition: '高', trend: 'stable' },
  { name: '宠物毛发梳', platform: 'Temu', price: '$12.99', score: 72, competition: '低', trend: 'up' }
])

function scoreColor(score: number): string {
  if (score >= 80) return '#52c41a'
  if (score >= 70) return '#faad14'
  return '#ff4d4f'
}

function scoreBg(score: number): string {
  if (score >= 80) return 'rgba(82,196,26,0.08)'
  if (score >= 70) return 'rgba(250,173,20,0.08)'
  return 'rgba(255,77,79,0.08)'
}
</script>

<template>
  <div class="dashboard-view">
    <div class="dashboard-header">
      <h2 class="dashboard-title">数据看板</h2>
      <p class="dashboard-subtitle">市场数据分析与选品推荐概览</p>
    </div>

    <div class="dashboard-grid">
      <!-- Market Trend -->
      <div class="chart-card">
        <div class="chart-card-header">
          <div class="chart-card-title">
            <el-icon :size="18" color="#1890ff"><TrendCharts /></el-icon>
            <span>市场热度趋势</span>
          </div>
          <span class="chart-card-tag">近 6 个月</span>
        </div>
        <div class="chart-card-body">
          <v-chart :option="marketTrendOption" style="height: 280px" autoresize />
        </div>
      </div>

      <!-- Category Distribution -->
      <div class="chart-card">
        <div class="chart-card-header">
          <div class="chart-card-title">
            <el-icon :size="18" color="#faad14"><DataBoard /></el-icon>
            <span>品类热度分布</span>
          </div>
          <span class="chart-card-tag">本月</span>
        </div>
        <div class="chart-card-body">
          <v-chart :option="categoryOption" style="height: 280px" autoresize />
        </div>
      </div>

      <!-- Trend Comparison -->
      <div class="chart-card card-wide">
        <div class="chart-card-header">
          <div class="chart-card-title">
            <el-icon :size="18" color="#52c41a"><DataAnalysis /></el-icon>
            <span>品类热度对比</span>
          </div>
          <span class="chart-card-tag">Top 3 品类</span>
        </div>
        <div class="chart-card-body">
          <v-chart :option="trendComparisonOption" style="height: 280px" autoresize />
        </div>
      </div>

      <!-- Recommendations -->
      <div class="chart-card card-wide">
        <div class="chart-card-header">
          <div class="chart-card-title">
            <el-icon :size="18" color="#ff4d4f"><List /></el-icon>
            <span>AI 选品推荐排行</span>
          </div>
          <span class="chart-card-tag score-tag">评分排行</span>
        </div>
        <div class="chart-card-body">
          <div class="recommend-list">
            <div v-for="(item, index) in recommendations" :key="index" class="recommend-item">
              <div class="rank-badge" :class="{ 'rank-gold': index === 0, 'rank-silver': index === 1, 'rank-bronze': index === 2 }">
                {{ index + 1 }}
              </div>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-tags">
                  <span class="item-tag platform-tag">{{ item.platform }}</span>
                  <span class="item-tag price-tag">{{ item.price }}</span>
                  <span class="item-tag" :class="item.competition === '低' ? 'comp-low' : item.competition === '中' ? 'comp-mid' : 'comp-high'">
                    竞争: {{ item.competition }}
                  </span>
                </div>
              </div>
              <div class="item-score-wrapper" :style="{ background: scoreBg(item.score) }">
                <div class="item-score-value" :style="{ color: scoreColor(item.score) }">
                  {{ item.score }}
                </div>
                <div class="item-score-label">推荐指数</div>
              </div>
              <div class="item-trend">
                <el-icon v-if="item.trend === 'up'" color="#52c41a" :size="20"><Top /></el-icon>
                <el-icon v-else color="#8e8ea0" :size="20"><Minus /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-view {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  animation: fadeIn 0.4s ease;
  background: #ffffff;
}

.dashboard-header {
  margin-bottom: 20px;
  animation: slideUp 0.4s ease;
}

.dashboard-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.dashboard-subtitle {
  font-size: 13px;
  color: var(--text-muted);
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

/* Chart Cards */
.chart-card {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  overflow: hidden;
  transition: all var(--transition-normal);
  animation: slideUp 0.4s ease both;
}

.chart-card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

.chart-card:nth-child(1) { animation-delay: 0.05s; }
.chart-card:nth-child(2) { animation-delay: 0.1s; }
.chart-card:nth-child(3) { animation-delay: 0.15s; }
.chart-card:nth-child(4) { animation-delay: 0.2s; }

.chart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 0;
}

.chart-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-card-tag {
  font-size: 11px;
  color: var(--text-muted);
  background: var(--bg-color);
  padding: 2px 10px;
  border-radius: 12px;
}

.score-tag {
  background: var(--success-bg);
  color: var(--success-color);
}

.chart-card-body {
  padding: 8px 12px 12px;
}

/* Recommendation List */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  cursor: default;
}

.recommend-item:hover {
  background: var(--bg-color);
}

.rank-badge {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
  background: var(--bg-color);
  color: var(--text-muted);
  flex-shrink: 0;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ffab00);
  color: white;
  box-shadow: 0 2px 6px rgba(255, 215, 0, 0.4);
}

.rank-silver {
  background: linear-gradient(135deg, #e8e8ef, #c0c4cc);
  color: white;
  box-shadow: 0 2px 6px rgba(192, 196, 204, 0.4);
}

.rank-bronze {
  background: linear-gradient(135deg, #ffa07a, #d4875e);
  color: white;
  box-shadow: 0 2px 6px rgba(212, 135, 94, 0.4);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.item-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.item-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.platform-tag {
  background: var(--primary-bg);
  color: var(--primary-color);
}

.price-tag {
  background: var(--danger-bg);
  color: var(--danger-color);
}

.comp-low {
  background: var(--success-bg);
  color: var(--success-color);
}

.comp-mid {
  background: var(--warning-bg);
  color: var(--warning-color);
}

.comp-high {
  background: var(--danger-bg);
  color: var(--danger-color);
}

.item-score-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  min-width: 64px;
}

.item-score-value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}

.item-score-label {
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 2px;
}

.item-trend {
  width: 30px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}
</style>