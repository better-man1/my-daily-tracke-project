<template>
  <div class="tag-manager">
    <div class="tag-header">
      <h4>标签管理</h4>
      <el-button type="primary" size="small" :icon="Plus" @click="handleCreateTag">新建标签</el-button>
    </div>

    <div class="tag-list">
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="tag-item"
        :class="{ selected: selectedTagIds.includes(tag.id) }"
        @click="toggleTag(tag.id)"
      >
        <span class="tag-dot" :style="{ background: tag.color }"></span>
        <span class="tag-name">{{ tag.name }}</span>
        <el-dropdown trigger="click" @command="(cmd: string) => handleTagCommand(cmd, tag)">
          <el-icon class="tag-more"><MoreFilled /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit">编辑</el-dropdown-item>
              <el-dropdown-item command="delete" class="danger">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div v-if="tags.length === 0" class="empty-tags">
        暂无标签，点击上方按钮创建
      </div>
    </div>

    <!-- 编辑/创建标签弹窗 -->
    <el-dialog
      v-model="showTagDialog"
      :title="editingTag ? '编辑标签' : '新建标签'"
      width="400px"
      destroy-on-close
    >
      <el-form ref="tagFormRef" :model="tagForm" :rules="tagRules" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <div class="color-picker">
            <div
              v-for="color in presetColors"
              :key="color"
              class="color-option"
              :class="{ selected: tagForm.color === color }"
              :style="{ background: color }"
              @click="tagForm.color = color"
            ></div>
            <el-color-picker v-model="tagForm.color" show-alpha />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTagDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { tagApi } from '@/api/plan'
import type { TagItem } from '@/api/plan'

const emit = defineEmits<{
  (e: 'change', tagIds: number[]): void
}>()

const tags = ref<TagItem[]>([])
const selectedTagIds = ref<number[]>([])
const showTagDialog = ref(false)
const editingTag = ref<TagItem | null>(null)
const saving = ref(false)
const tagFormRef = ref<FormInstance>()

const presetColors = [
  '#6366f1', '#8b5cf6', '#ec4899',
  '#ef4444', '#f97316', '#f59e0b',
  '#10b981', '#14b8a6', '#06b6d4',
  '#3b82f6', '#64748b', '#78716c'
]

const tagForm = reactive({
  name: '',
  color: '#6366f1'
})

const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { max: 50, message: '标签名称不能超过50个字符', trigger: 'blur' }
  ]
}

async function loadTags() {
  try {
    tags.value = await tagApi.list()
  } catch (error) {
    console.error('Failed to load tags', error)
  }
}

function handleCreateTag() {
  editingTag.value = null
  tagForm.name = ''
  tagForm.color = presetColors[Math.floor(Math.random() * presetColors.length)]
  showTagDialog.value = true
}

function toggleTag(tagId: number) {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value.push(tagId)
  }
  emit('change', selectedTagIds.value)
}

function handleTagCommand(cmd: string, tag: TagItem) {
  if (cmd === 'edit') {
    editingTag.value = tag
    tagForm.name = tag.name
    tagForm.color = tag.color
    showTagDialog.value = true
  } else if (cmd === 'delete') {
    ElMessageBox.confirm(`确认删除标签"${tag.name}"？`, '提示', { type: 'warning' }).then(async () => {
      try {
        await tagApi.delete(tag.id)
        ElMessage.success('删除成功')
        loadTags()
      } catch (error) {
        console.error('Failed to delete tag', error)
      }
    })
  }
}

async function saveTag() {
  await tagFormRef.value?.validate()
  saving.value = true
  try {
    if (editingTag.value) {
      await tagApi.update(editingTag.value.id, tagForm)
      ElMessage.success('更新成功')
    } else {
      await tagApi.create(tagForm)
      ElMessage.success('创建成功')
    }
    showTagDialog.value = false
    loadTags()
  } catch (error) {
    console.error('Failed to save tag', error)
  } finally {
    saving.value = false
  }
}

onMounted(loadTags)
</script>

<style lang="scss" scoped>
.tag-manager {
  .tag-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h4 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  .tag-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .tag-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid $border;
      border-radius: $radius-sm;
      cursor: pointer;
      transition: $transition-fast;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }

      &.selected {
        background: rgba(99, 102, 241, 0.1);
        border-color: $primary;
      }

      .tag-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        flex-shrink: 0;
      }

      .tag-name {
        flex: 1;
        font-size: 14px;
        color: $text-primary;
      }

      .tag-more {
        color: $text-muted;
        cursor: pointer;
        padding: 4px;
        border-radius: $radius-sm;
        transition: $transition-fast;

        &:hover {
          color: $text-primary;
          background: rgba(255, 255, 255, 0.06);
        }
      }
    }

    .empty-tags {
      text-align: center;
      padding: 20px;
      color: $text-muted;
      font-size: 13px;
    }
  }

  .color-picker {
    display: flex;
    gap: 8px;
    align-items: center;

    .color-option {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      cursor: pointer;
      border: 2px solid transparent;
      transition: $transition-fast;

      &.selected {
        border-color: $primary;
        box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
      }

      &:hover {
        transform: scale(1.1);
      }
    }
  }
}

:deep(.danger) {
  color: $danger !important;
}
</style>
