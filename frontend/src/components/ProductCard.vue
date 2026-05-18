<script setup lang="ts">
import type { Product } from '@/types'

defineProps<{
  product: Product
}>()
</script>

<template>
  <div class="product-card">
    <div class="product-image">
      <el-icon :size="48"><Goods /></el-icon>
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-meta">
        <el-tag size="small" type="primary">{{ product.platform }}</el-tag>
        <el-tag v-if="product.competition" size="small" :type="product.competition === '低' ? 'success' : 'warning'">
          竞争: {{ product.competition }}
        </el-tag>
        <el-tag v-if="product.score" size="small" :type="product.score >= 80 ? 'success' : 'info'">
          评分: {{ product.score }}
        </el-tag>
      </div>
      <div class="product-details">
        <div v-if="product.price" class="detail-item">
          <span class="label">价格</span>
          <span class="value price">{{ product.price }}</span>
        </div>
        <div v-if="product.monthlySales" class="detail-item">
          <span class="label">月销量</span>
          <span class="value">{{ product.monthlySales }}</span>
        </div>
        <div v-if="product.rating" class="detail-item">
          <span class="label">评分</span>
          <span class="value">{{ product.rating }}/5</span>
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
  background: #fafafa;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 16px;
  margin: 8px 0;
  transition: box-shadow 0.2s;
}

.product-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.product-image {
  width: 80px;
  height: 80px;
  background: var(--primary-light);
  border-radius: 8px;
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
  margin-bottom: 6px;
}

.product-meta {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.product-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.label {
  font-size: 11px;
  color: var(--text-muted);
}

.value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.value.price {
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
