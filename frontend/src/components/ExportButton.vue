<!--
/**
 * ============================================================================
 * ExportButton.vue — 数据导出按钮组件
 * ============================================================================
 *
 * 【组件说明】
 * 基于 Element Plus Dropdown 封装的数据导出按钮，支持三种导出格式：
 * Excel (.xlsx)、CSV (.csv) 和 JSON (.json)。
 * ============================================================================
 */
-->

<template>
  <el-dropdown trigger="click" @command="handleExport">
    <el-button type="primary" :loading="loading">
      <el-icon class="el-icon--left"><Download /></el-icon>
      导出数据
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="xlsx">
          <el-icon><Document /></el-icon>
          Excel (.xlsx)
        </el-dropdown-item>
        <el-dropdown-item command="csv">
          <el-icon><Files /></el-icon>
          CSV (.csv)
        </el-dropdown-item>
        <el-dropdown-item command="json">
          <el-icon><Tickets /></el-icon>
          JSON (.json)
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, ArrowDown, Document, Files, Tickets } from '@element-plus/icons-vue'
import { exportData, type ExportFormat } from '@/utils/export'

interface Props {
  data: any[]
  filename?: string
  sheetName?: string
}

// ============================================================================
// // 状态
// ============================================================================

// ============================================================================
// // 状态
// ============================================================================

const props = withDefaults(defineProps<Props>(), {
  filename: '',
  sheetName: 'Sheet1'
})

const loading = ref(false)

// ============================================================================
// // 交互处理
// ============================================================================

// ============================================================================
// // 交互处理
// ============================================================================

async function handleExport(format: ExportFormat) {
  if (!props.data || props.data.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300)) // 模拟加载

    exportData(props.data, {
      filename: props.filename,
      format,
      sheetName: props.sheetName
    })

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.el-dropdown {
  display: inline-block;
}

.el-dropdown-menu {
  .el-icon {
    margin-right: 8px;
  }
}
</style>
