<script setup>
import { Filter } from '@element-plus/icons-vue'

defineProps({
  label: { type: String, required: true },
  filter: { type: Object, required: true },
  type: {
    type: String,
    default: 'text',
    validator: (v) => ['text', 'enum', 'number', 'time'].includes(v),
  },
  active: { type: Boolean, default: false },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '输入关键词…' },
})
</script>

<template>
  <div class="table-filter-header">
    <span>{{ label }}</span>
    <el-popover trigger="click" placement="bottom" :width="240" :teleported="true">
      <template #reference>
        <el-icon :class="{ 'filter-active': active }" class="filter-icon">
          <Filter />
        </el-icon>
      </template>

      <template v-if="type === 'text'">
        <el-input v-model="filter.value" :placeholder="placeholder" clearable />
        <div class="filter-mode-toggle">
          <el-radio-group v-model="filter.mode" size="small">
            <el-radio-button value="fuzzy">模糊</el-radio-button>
            <el-radio-button value="exact">精确</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <template v-else-if="type === 'enum'">
        <el-checkbox-group v-model="filter.values" :class="{ 'filter-enum-vertical': options.length <= 5 }">
          <el-checkbox v-for="opt in options" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </el-checkbox>
        </el-checkbox-group>
      </template>

      <template v-else-if="type === 'number'">
        <div class="filter-range-row">
          <el-input-number v-model="filter.min" :placeholder="'最小值'" controls-position="right" class="filter-range-input" />
          <span class="filter-range-sep">—</span>
          <el-input-number v-model="filter.max" :placeholder="'最大值'" controls-position="right" class="filter-range-input" />
        </div>
      </template>

      <template v-else-if="type === 'time'">
        <div class="filter-time-stack">
          <el-date-picker
            v-model="filter.start"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            class="filter-time-item"
          />
          <el-date-picker
            v-model="filter.end"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            class="filter-time-item"
          />
        </div>
      </template>
    </el-popover>
  </div>
</template>