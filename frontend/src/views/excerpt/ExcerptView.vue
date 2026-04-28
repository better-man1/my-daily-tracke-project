<template>
  <div class="excerpt-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日摘录</h1>
        <p class="page-subtitle">积累知识，沉淀思考</p>
      </div>
      <div class="flex items-center gap-md">
        <el-input v-model="searchKeyword" placeholder="搜索摘录..." :prefix-icon="Search" size="small" style="width:200px" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增摘录</el-button>
      </div>
    </div>

    <!-- 摘录卡片 -->
    <div v-loading="loading" class="excerpt-grid">
      <div v-for="(item, idx) in list" :key="item.id"
        class="excerpt-card card card--glow animate-fade-in-up"
        :style="{ animationDelay: idx * 0.05 + 's' }">
        <!-- 顶部 -->
        <div class="excerpt-header">
          <span class="source-type-badge">{{ sourceTypeLabel(item.sourceType) }}</span>
          <div class="excerpt-actions">
            <el-icon class="icon-btn" :class="{ active: item.isFavorite }" @click="toggleFav(item)">
              <Star />
            </el-icon>
            <el-icon class="icon-btn" @click="editItem(item)"><Edit /></el-icon>
            <el-icon class="icon-btn danger" @click="deleteItem(item.id)"><Delete /></el-icon>
          </div>
        </div>

        <!-- 内容 -->
        <div class="excerpt-content">"{{ item.content }}"</div>

        <!-- 来源 -->
        <div v-if="item.sourceTitle" class="excerpt-source">
          📌 {{ item.sourceTitle }}
        </div>

        <!-- 感悟 -->
        <div v-if="item.thought" class="excerpt-thought">
          💭 {{ item.thought }}
        </div>

        <!-- 底部 -->
        <div class="excerpt-footer">
          <div class="excerpt-tags">
            <span v-for="tag in item.tags" :key="tag.id" class="tag" :style="{ borderColor: tag.color, color: tag.color }">
              {{ tag.name }}
            </span>
          </div>
          <span class="excerpt-date text-muted text-xs">{{ item.excerptDate }}</span>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">📖</div>
        <p>还没有摘录，记录你的第一条知识</p>
        <el-button type="primary" size="small" @click="openAdd">添加摘录</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination v-if="total > 0"
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="mt-md"
      @current-change="loadList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editing ? '编辑摘录' : '新增摘录'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="输入摘录内容..." />
        </el-form-item>
        <div class="flex gap-md">
          <el-form-item label="来源类型" style="flex:1">
            <el-select v-model="form.sourceType">
              <el-option v-for="s in sourceTypes" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="来源标题" style="flex:2">
            <el-input v-model="form.sourceTitle" placeholder="书名/文章/视频标题" />
          </el-form-item>
        </div>
        <el-form-item label="个人感悟">
          <el-input v-model="form.thought" type="textarea" :rows="2" placeholder="写下你的感悟..." />
        </el-form-item>
        <el-form-item label="摘录日期" prop="excerptDate">
          <el-date-picker v-model="form.excerptDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search, Star, Edit, Delete } from '@element-plus/icons-vue'
import { excerptApi } from '@/api/excerpt'
import type { ExcerptItem } from '@/api/excerpt'
import dayjs from 'dayjs'

const list = ref<ExcerptItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(false)
const saving = ref(false)
const showDialog = ref(false)
const editing = ref<ExcerptItem | null>(null)
const searchKeyword = ref('')
const formRef = ref<FormInstance>()

const sourceTypes = [
  { value: 'BOOK', label: '📚 书籍' },
  { value: 'ARTICLE', label: '📰 文章' },
  { value: 'VIDEO', label: '🎬 视频' },
  { value: 'PODCAST', label: '🎙️ 播客' },
  { value: 'OTHER', label: '📌 其他' }
]

const form = reactive({
  content: '',
  sourceType: 'BOOK',
  sourceTitle: '',
  thought: '',
  excerptDate: dayjs().format('YYYY-MM-DD')
})

const rules = {
  content: [{ required: true, message: '请输入摘录内容', trigger: 'blur' }],
  excerptDate: [{ required: true, message: '请选择日期' }]
}

function sourceTypeLabel(val: string) {
  return sourceTypes.find(s => s.value === val)?.label ?? val
}

async function loadList() {
  loading.value = true
  try {
    const res = await excerptApi.page({ pageNum: pageNum.value, pageSize })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  if (!searchKeyword.value.trim()) { loadList(); return }
  loading.value = true
  try {
    const res = await excerptApi.search(searchKeyword.value, 1, pageSize)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editing.value = null
  Object.assign(form, { content: '', sourceType: 'BOOK', sourceTitle: '', thought: '', excerptDate: dayjs().format('YYYY-MM-DD') })
  showDialog.value = true
}

function editItem(item: ExcerptItem) {
  editing.value = item
  Object.assign(form, { content: item.content, sourceType: item.sourceType, sourceTitle: item.sourceTitle ?? '', thought: item.thought ?? '', excerptDate: item.excerptDate })
  showDialog.value = true
}

async function toggleFav(item: ExcerptItem) {
  await excerptApi.toggleFavorite(item.id)
  item.isFavorite = item.isFavorite ? 0 : 1
}

async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  await excerptApi.delete(id)
  ElMessage.success('已删除')
  loadList()
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (editing.value) {
      await excerptApi.update(editing.value.id, form as any)
      ElMessage.success('更新成功')
    } else {
      await excerptApi.create(form as any)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

onMounted(loadList)
</script>

<style lang="scss" scoped>
.excerpt-view {
  .excerpt-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
    gap: 16px;
  }

  .excerpt-card {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .excerpt-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .source-type-badge {
        font-size: 11px;
        padding: 2px 8px;
        border-radius: $radius-full;
        background: rgba(255, 255, 255, 0.06);
        color: $text-secondary;
      }

      .excerpt-actions {
        display: flex;
        gap: 6px;
        opacity: 0;
        transition: $transition-fast;

        .icon-btn {
          color: $text-muted;
          cursor: pointer;
          padding: 4px;
          border-radius: $radius-sm;
          font-size: 15px;
          transition: $transition-fast;

          &:hover { color: $primary-light; background: $primary-100; }
          &.active { color: $warning !important; }
          &.danger:hover { color: $danger; background: rgba($danger, 0.1); }
        }
      }
    }

    &:hover .excerpt-header .excerpt-actions {
      opacity: 1;
    }

    .excerpt-content {
      font-size: 14px;
      line-height: 1.7;
      color: $text-primary;
      font-style: italic;
      flex: 1;
    }

    .excerpt-source {
      font-size: 12px;
      color: $text-secondary;
    }

    .excerpt-thought {
      font-size: 13px;
      color: $text-secondary;
      padding: 8px 12px;
      background: $bg-input;
      border-radius: $radius-sm;
      border-left: 2px solid $primary;
    }

    .excerpt-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .excerpt-tags {
        display: flex;
        gap: 4px;
        flex-wrap: wrap;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    .empty-icon { font-size: 48px; margin-bottom: 12px; }
    p { margin-bottom: 16px; }
  }
}
</style>
