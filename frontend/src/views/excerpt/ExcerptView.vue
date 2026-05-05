<template>
  <div class="excerpt-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">每日摘录</h1>
        <p class="page-subtitle">积累知识，沉淀思考</p>
      </div>
      <div class="flex items-center gap-md">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索摘录..."
          :prefix-icon="Search"
          size="small"
          style="width:200px"
          clearable
          @keyup.enter="doSearch"
          @clear="clearSearch"
        />
        <el-button type="primary" size="small" :icon="Plus" @click="openAdd">新增摘录</el-button>
      </div>
    </div>

    <!-- 标签筛选栏 -->
    <div v-if="allTags.length" class="tag-filter">
      <span class="filter-label">标签筛选：</span>
      <span
        class="filter-tag"
        :class="{ active: !filterTagId }"
        @click="setTagFilter(null)"
      >全部</span>
      <span
        v-for="tag in allTags"
        :key="tag.id"
        class="filter-tag"
        :class="{ active: filterTagId === tag.id }"
        :style="filterTagId === tag.id ? { borderColor: tag.color, color: tag.color, background: tag.color + '18' } : {}"
        @click="setTagFilter(tag.id)"
      >{{ tag.name }}</span>
    </div>

    <!-- 来源类型筛选 -->
    <div class="type-filter">
      <el-radio-group v-model="filterSourceType" size="small" @change="loadList">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="s in sourceTypes" :key="s.value" :label="s.value">{{ s.label }}</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 摘录卡片 -->
    <div v-loading="loading" class="excerpt-grid">
      <div
        v-for="(item, idx) in list"
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
            <span
              v-for="tag in item.tags"
              :key="tag.id"
              class="tag"
              :style="{ borderColor: tag.color, color: tag.color, background: tag.color + '18' }"
            >
              {{ tag.name }}
            </span>
          </div>
          <span class="excerpt-date text-muted text-xs">{{ item.excerptDate }}</span>
        </div>
      </div>

      <div v-if="!loading && list.length === 0" class="empty-state">
        <div class="empty-icon">📖</div>
        <p>{{ searchKeyword || filterTagId ? '没有找到匹配的摘录' : '还没有摘录，记录你的第一条知识' }}</p>
        <el-button v-if="!searchKeyword && !filterTagId" type="primary" size="small" @click="openAdd">
          添加摘录
        </el-button>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      class="mt-md"
      @current-change="loadList"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="editing ? '编辑摘录' : '新增摘录'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="输入摘录内容..."
          />
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
        <el-form-item label="标签">
          <TagInput
            v-model="selectedTags"
            :options="allTags"
            placeholder="选择或创建标签..."
            @create="handleCreateTag"
          />
        </el-form-item>
        <el-form-item label="摘录日期" prop="excerptDate">
          <el-date-picker
            v-model="form.excerptDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="收藏">
          <el-switch v-model="form.isFavorite" :active-value="1" :inactive-value="0" />
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
import TagInput from '@/components/common/TagInput.vue'
import type { TagOption } from '@/components/common/TagInput.vue'
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
const filterTagId = ref<number | null>(null)
const filterSourceType = ref('')
const formRef = ref<FormInstance>()
const allTags = ref<TagOption[]>([])
const selectedTags = ref<TagOption[]>([])

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
  isFavorite: 0
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
    if (searchKeyword.value.trim()) {
      const res = await excerptApi.search(searchKeyword.value, pageNum.value, pageSize)
      list.value = res.records
      total.value = res.total
    } else {
      const res = await excerptApi.page({
        pageNum: pageNum.value,
        pageSize,
        sourceType: filterSourceType.value || undefined,
        tagId: filterTagId.value ?? undefined
      })
      list.value = res.records
      total.value = res.total
    }
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    const tags = await excerptApi.getAllTags()
    allTags.value = tags.map((t: any) => ({ id: t.id, name: t.name, color: t.color }))
  } catch {}
}

async function doSearch() {
  pageNum.value = 1
  await loadList()
}

async function clearSearch() {
  searchKeyword.value = ''
  pageNum.value = 1
  await loadList()
}

function setTagFilter(id: number | null) {
  filterTagId.value = id
  pageNum.value = 1
  loadList()
}

function openAdd() {
  editing.value = null
  selectedTags.value = []
  Object.assign(form, {
    content: '', sourceType: 'BOOK', sourceTitle: '',
    thought: '', excerptDate: dayjs().format('YYYY-MM-DD'), isFavorite: 0
  })
  showDialog.value = true
}

function editItem(item: ExcerptItem) {
  editing.value = item
  selectedTags.value = (item.tags ?? []).map(t => ({ id: t.id, name: t.name, color: t.color }))
  Object.assign(form, {
    content: item.content,
    sourceType: item.sourceType,
    sourceTitle: item.sourceTitle ?? '',
    thought: item.thought ?? '',
    excerptDate: item.excerptDate,
    isFavorite: item.isFavorite ?? 0
  })
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

async function handleCreateTag(name: string) {
  // 添加到本地可选列表（无需额外后端接口，后端会在创建摘录时自动创建标签）
  const newTag: TagOption = { name, color: '#6366f1' }
  allTags.value = [...allTags.value, newTag]
}

async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      ...form,
      tagIds: selectedTags.value.filter(t => t.id).map(t => t.id as number)
    }
    if (editing.value) {
      await excerptApi.update(editing.value.id, payload as any)
      ElMessage.success('更新成功')
    } else {
      await excerptApi.create(payload as any)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    await Promise.all([loadList(), loadTags()])
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadList()
  loadTags()
})
</script>

<style lang="scss" scoped>
.excerpt-view {
  .tag-filter {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 12px;

    .filter-label {
      font-size: 12px;
      color: $text-muted;
      flex-shrink: 0;
    }

    .filter-tag {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: $radius-full;
      border: 1px solid $border;
      font-size: 12px;
      color: $text-secondary;
      cursor: pointer;
      transition: $transition-fast;
      user-select: none;

      &:hover {
        border-color: rgba($primary, 0.4);
        color: $primary-light;
      }

      &.active {
        border-color: $primary;
        color: $primary-light;
        background: $primary-100;
      }
    }
  }

  .type-filter {
    margin-bottom: 16px;
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
          font-size: 15px;
          transition: $transition-fast;

          &:hover { color: $primary-light; background: $primary-100; }
          &.active { color: $warning !important; }
          &.danger:hover { color: $danger; background: rgba($danger, 0.1); }
        }
      }
    }

    &:hover .excerpt-header .excerpt-actions { opacity: 1; }

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
    grid-column: 1 / -1;
    text-align: center;
    padding: 60px 20px;
    color: $text-muted;
    .empty-icon { font-size: 48px; margin-bottom: 12px; }
    p { margin-bottom: 16px; }
  }
}
</style>
