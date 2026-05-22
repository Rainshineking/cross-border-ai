<script setup lang="ts">
import type { Product } from '@/types'

defineProps<{
  product: Product
}>()
</script>

<template>
  <div class="product-card">
    <div class="product-image">
      <el-icon :size="36"><Goods /></el-icon>
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-meta">
        <span class="meta-tag platform">{{ product.platform }}</span>
        <span v-if="product.competition" class="meta-tag" :class="'comp-' + product.competition">
          竞争: {{ product.competition }}
        </span>
        <span v-if="product.score" class="meta-tag score" :class="product.score >= 80 ? 'score-high' : 'score-mid'">
          评分: {{ product.score }}
        </span>
      </div>
      <div class="product-details">
        <div v-if="product.price" class="detail-item">
          <span class="detail-label">价格</span>
          <span class="detail-value price">{{ product.price }}</span>
        </div>
        <div v-if="product.monthlySales" class="detail-item">
          <span class="detail-label">月销量</span>
          <span class="detail-value">{{ product.monthlySales }}</span>
        </div>
        <div v-if="product.rating" class="detail-item">
          <span class="detail-label">评分</span>
          <span class="detail-value">{{ product.rating }}/5</span>
        </div>
      </div>
      <div v-if="product.reason" class="product-reason">
        {{ product.reason }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  display: flex;
  gap: 16px;
  background: var(--card-bg);
  border: var(--card-border);
  border-radius: var(--radius-md);
  padding: 16px;
  margin: 8px 0;
  transition: all var(--transition-normal);
  box-shadow: var(--card-shadow);
}

.product-card:hover {
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-2px);
}

.product-image {
  width: 80px;
  height: 80px;
  background: var(--primary-bg);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--primary-color);
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.meta-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.meta-tag.platform {
  background: var(--primary-bg);
  color: var(--primary-color);
}

.meta-tag.comp-低 {
  background: var(--success-bg);
  color: var(--success-color);
}

.meta-tag.comp-中 {
  background: var(--warning-bg);
  color: var(--warning-color);
}

.meta-tag.comp-高 {
  background: var(--danger-bg);
  color: var(--danger-color);
}

.meta-tag.score-high {
  background: var(--success-bg);
  color: var(--success-color);
}

.meta-tag.score-mid {
  background: var(--warning-bg);
  color: var(--warning-color);
}

.product-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 10px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-label {
  font-size: 11px;
  color: var(--text-muted);
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.detail-value.price {
  color: var(--danger-color);
  font-weight: 600;
}

.product-reason {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  padding-top: 8px;
  border-top: 1px dashed var(--border-color);
}
</style>