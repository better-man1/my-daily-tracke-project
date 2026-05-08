<template>
  <div class="excerpt-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日摘录</h1>
        <p class="page-subtitle">积累知识，沉淀思考</p>
      </div>
      <div class="flex items-center gap-md">
        <!-- 暂隐藏按标签筛选
        <el-select
          v-model="filterTagId"
          placeholder="按标签筛选"
          clearable
          size="small"
          style="width: 140px"
          @change="loadList"
        >
        <el-option v-for="t in allTags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        -->
        <el-select
          v-model="filterSourceType"
          placeholder="按来源筛选"
          clearable
          size="small"
          style="width: 140px"
          @change="loadList"
        >
          <el-option v-for="s in sourceTypes" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="shortcuts"
          size="small"
          style="width: 240px"
          @change="loadList"
        />
        <el-input
          v-model="searchKeyword"
          placeholder="搜索摘录..."
          :prefix-icon="Search"
          size="small"
          style="width: 160px"
          @keyup.enter="doSearch"
        />
        <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增摘录</el-button>
        <el-button type="warning" size="small" @click="handleRandomReview">🎲 随机回顾</el-button>
        <el-button type="success" size="small" :icon="Download" @click="handleExportMarkdown" :loading="exporting">导出 MD</el-button>
      </div>
    </div>

    <!-- 摘录列表 -->
    <div v-loading="loading" class="excerpt-list-container">
      <el-collapse v-model="activeNames" class="custom-collapse">
        <el-collapse-item v-for="group in groupedList" :key="group.month" :name="group.month">
          <template #title>
            <div class="group-header">
              <span class="group-month">
                <el-icon><Calendar /></el-icon>
                {{ group.displayMonth }}
              </span>
              <div class="group-summary">
                <span class="count">共 {{ group.items.length }} 条摘录</span>
              </div>
            </div>
          </template>

          <div class="excerpt-grid">
            <div
              v-for="(item, idx) in group.items"
              :key="item.id"
              class="excerpt-card card card--glow animate-fade-in-up"
              :style="{ animationDelay: idx * 0.05 + 's' }"
            >
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
              <div v-if="item.sourceTitle" class="excerpt-source">📌 {{ item.sourceTitle }}</div>

              <!-- 感悟 -->
              <div v-if="item.thought" class="excerpt-thought">💭 {{ item.thought }}</div>

              <!-- 底部 -->
              <div class="excerpt-footer">
                <div class="excerpt-tags">
                  <span
                    v-for="tag in item.tags"
                    :key="tag.id"
                    class="tag"
                    :style="{ borderColor: tag.color, color: tag.color }"
                  >
                    {{ tag.name }}
                  </span>
                </div>
                <span class="excerpt-date text-muted text-xs">{{ item.excerptDate }}</span>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">📖</div>
        <p>还没有摘录，记录你的第一条知识</p>
        <el-button type="primary" size="small" @click="openAdd">添加摘录</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <!-- 分页暂时隐藏
    <el-pagination
      v-if="total > 0"
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="mt-md"
      @current-change="loadList"
    />
    -->

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="editing ? '编辑摘录' : '新增摘录'"
      width="640px"
      destroy-on-close
      class="premium-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="left">
        <div class="form-section">
          <el-form-item label="摘录内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="5"
              placeholder="在这里记录触动你的文字..."
            />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="来源类型">
              <el-select v-model="form.sourceType" style="width: 100%">
                <el-option
                  v-for="s in sourceTypes"
                  :key="s.value"
                  :label="s.label"
                  :value="s.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="所属分类">
              <el-select v-model="form.category" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="来源标题">
            <el-input v-model="form.sourceTitle" placeholder="书名、文章标题或网页链接" />
          </el-form-item>

          <el-form-item label="心得体会">
            <el-input
              v-model="form.thoughts"
              type="textarea"
              :rows="3"
              placeholder="写下你的感悟或对这段文字的思考..."
            />
          </el-form-item>

          <el-form-item label="相关标签">
            <el-select
              v-model="form.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入标签并回车"
              style="width: 100%"
            >
              <el-option v-for="tag in commonTags" :key="tag" :label="tag" :value="tag" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 随机回顾弹窗 -->
    <el-dialog
      v-model="showRandomDialog"
      title="🎲 随机回顾，温故知新"
      width="500px"
      center
    >
      <div v-if="randomExcerpt" class="random-excerpt-card">
        <div class="excerpt-content">"{{ randomExcerpt.content }}"</div>
        <div v-if="randomExcerpt.sourceTitle" class="excerpt-source">
          📌 {{ randomExcerpt.sourceTitle }}
        </div>
        <div v-if="randomExcerpt.thought" class="excerpt-thought">
          💭 {{ randomExcerpt.thought }}
        </div>
      </div>
      <div v-else class="text-center text-muted py-md">
        还没有摘录记录哦~
      </div>
      <template #footer>
        <el-button @click="showRandomDialog = false">关闭</el-button>
        <el-button type="primary" @click="handleRandomReview">再来一条</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, Search, Star, Edit, Delete, Download, Calendar } from '@element-plus/icons-vue'
import { excerptApi } from '@/api/excerpt'
import type { ExcerptItem } from '@/api/excerpt'
import dayjs from 'dayjs'

const list = ref<ExcerptItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 200
const loading = ref(false)
const saving = ref(false)
const exporting = ref(false)
const showDialog = ref(false)
const showRandomDialog = ref(false)
const editing = ref<ExcerptItem | null>(null)
const randomExcerpt = ref<ExcerptItem | null>(null)
const searchKeyword = ref('')
const filterTagId = ref<number | undefined>(undefined)
const filterSourceType = ref<string | undefined>(undefined)
const allTags = ref<any[]>([])
const formRef = ref<FormInstance>()

const dateRange = ref<[string, string] | null>([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().endOf('month').format('YYYY-MM-DD')
])

const activeNames = ref<string[]>([dayjs().format('YYYY-MM')])

const groupedList = computed(() => {
  const groups: Record<string, ExcerptItem[]> = {}
  list.value.forEach((item) => {
    const month = dayjs(item.excerptDate).format('YYYY-MM')
    if (!groups[month]) {
      groups[month] = []
    }
    groups[month].push(item)
  })
  
  return Object.keys(groups)
    .sort((a, b) => b.localeCompare(a))
    .map((month) => ({
      month,
      displayMonth: dayjs(month).format('YYYY年MM月'),
      items: groups[month]
    }))
})

const shortcuts = [
  {
    text: '近一个月',
    value: () => {
      const start = dayjs().startOf('month').toDate()
      const end = dayjs().endOf('month').toDate()
      return [start, end]
    }
  },
  {
    text: '近三个月',
    value: () => {
      const start = dayjs().subtract(2, 'month').startOf('month').toDate()
      const end = dayjs().endOf('month').toDate()
      return [start, end]
    }
  },
  {
    text: '近半年',
    value: () => {
      const currentMonth = dayjs().month()
      const isFirstHalf = currentMonth < 6
      const start = dayjs().month(isFirstHalf ? 0 : 6).startOf('month').toDate()
      const end = dayjs().month(isFirstHalf ? 5 : 11).endOf('month').toDate()
      return [start, end]
    }
  },
  {
    text: '近一年',
    value: () => {
      const start = dayjs().startOf('year').toDate()
      const end = dayjs().endOf('year').toDate()
      return [start, end]
    }
  }
]

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
  excerptDate: dayjs().format('YYYY-MM-DD'),
  tagIds: [] as number[]
})

const rules = {
  content: [{ required: true, message: '请输入摘录内容', trigger: 'blur' }],
  excerptDate: [{ required: true, message: '请选择日期' }]
}

function sourceTypeLabel(val: string) {
  return sourceTypes.find((s) => s.value === val)?.label ?? val
}

async function loadList() {
  loading.value = true
  try {
    const res = await excerptApi.page({ 
      pageNum: pageNum.value, 
      pageSize,
      tagId: filterTagId.value || undefined,
      sourceType: filterSourceType.value || undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    })
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    allTags.value = await excerptApi.getAllTags()
  } catch (e) {
    console.error('Failed to load tags', e)
  }
}

async function doSearch() {
  if (!searchKeyword.value.trim()) {
    loadList()
    return
  }
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
  Object.assign(form, {
    content: '',
    sourceType: 'BOOK',
    sourceTitle: '',
    thought: '',
    excerptDate: dayjs().format('YYYY-MM-DD'),
    tagIds: []
  })
  showDialog.value = true
}

function editItem(item: ExcerptItem) {
  editing.value = item
  Object.assign(form, {
    content: item.content,
    sourceType: item.sourceType,
    sourceTitle: item.sourceTitle ?? '',
    thought: item.thought ?? '',
    excerptDate: item.excerptDate,
    tagIds: item.tags.map(t => t.id)
  })
  showDialog.value = true
}

async function handleTagCreate(tagName: string) {
  try {
    const res = await excerptApi.createTag(tagName)
    // 重新加载所有标签以获取最新列表
    await loadTags()
    // 将新创建的标签ID添加到选中列表
    if (!form.tagIds.includes(res.id)) {
      form.tagIds.push(res.id)
    }
  } catch (e) {
    ElMessage.error('创建标签失败')
  }
}

async function toggleFav(item: ExcerptItem) {
  await excerptApi.toggleFavorite(item.id)
  item.isFavorite = item.isFavorite ? 0 : 1
}

async function deleteItem(id: number) {
  await ElMessageBox.confirm('确认删除？', '提示', { 
    type: 'warning',
    customClass: 'delete-confirm-box'
  })
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

async function handleExportMarkdown() {
  exporting.value = true
  try {
    const content = await excerptApi.exportMarkdown()
    const blob = new Blob([content], { type: 'text/markdown' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `daily-excerpts-${dayjs().format('YYYY-MM-DD')}.md`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export error:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

async function handleRandomReview() {
  try {
    const res = await excerptApi.getRandom()
    if (res) {
      randomExcerpt.value = res
      showRandomDialog.value = true
    } else {
      ElMessage.info('暂无摘录记录')
    }
  } catch (error) {
    console.error('Failed to get random excerpt:', error)
  }
}

onMounted(() => {
  loadList()
  loadTags()
})
</script>

<style lang="scss" scoped>
.excerpt-view {
  .excerpt-list-container {
    .custom-collapse {
      border: none;
      --el-collapse-header-bg-color: transparent;
      --el-collapse-content-bg-color: transparent;
      
      :deep(.el-collapse-item__header) {
        border-bottom: none;
        height: auto;
        padding: 12px 0;
        line-height: 1.4;
      }

      :deep(.el-collapse-item__wrap) {
        border-bottom: none;
      }

      :deep(.el-collapse-item__content) {
        padding-bottom: 20px;
      }
    }

    .group-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding-right: 12px;

      .group-month {
        font-size: 16px;
        font-weight: 700;
        color: $text-primary;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .group-summary {
        display: flex;
        gap: 16px;
        font-size: 13px;
        background: $bg-page;
        padding: 4px 16px;
        border-radius: $radius-full;
        align-items: center;
        color: $text-secondary;
        font-weight: 500;
      }
    }
  }

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
          font-size: 18px;
          transition: $transition-fast;

          &:hover {
            color: $primary-light;
            background: $primary-100;
          }
          &.active {
            color: $warning !important;
          }
          &.danger:hover {
            color: $danger;
            background: rgba($danger, 0.1);
          }
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
    .empty-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }
    p {
      margin-bottom: 16px;
    }
  }

  .random-excerpt-card {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.02);
    border-radius: $radius-md;
    border: 1px solid $border;

    .excerpt-content {
      font-size: 16px;
      line-height: 1.8;
      color: $text-primary;
      font-style: italic;
      text-align: center;
    }

    .excerpt-source {
      font-size: 13px;
      color: $text-secondary;
      text-align: right;
    }

    .excerpt-thought {
      font-size: 14px;
      color: $text-primary;
      padding: 12px;
      background: $bg-input;
      border-radius: $radius-sm;
      border-left: 3px solid $warning;
    }
  }
}
</style>
