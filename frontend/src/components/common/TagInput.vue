<template>
  <div class="tag-input">
    <!-- 已选标签 -->
    <div class="tags-wrap">
      <span
        v-for="tag in selectedTags"
        :key="tag.id ?? tag.name"
        class="tag tag--removable"
        :style="tagStyle(tag)"
      >
        {{ tag.name }}
        <span class="tag-remove" @click="removeTag(tag)">×</span>
      </span>

      <!-- 输入框 -->
      <input
        v-if="selectedTags.length < maxTags"
        ref="inputRef"
        v-model="inputVal"
        class="tag-input-field"
        :placeholder="selectedTags.length === 0 ? placeholder : ''"
        @keydown.enter.prevent="handleEnter"
        @keydown.backspace="handleBackspace"
        @input="handleInput"
        @focus="showDropdown = true"
        @blur="handleBlur"
      />
    </div>

    <!-- 下拉建议 -->
    <div v-if="showDropdown && filteredOptions.length" class="tag-dropdown">
      <div
        v-for="opt in filteredOptions"
        :key="opt.id"
        class="tag-option"
        @mousedown.prevent="selectTag(opt)"
      >
        <span class="opt-dot" :style="{ background: opt.color || '#6366f1' }" />
        <span class="opt-name">{{ opt.name }}</span>
      </div>
      <!-- 允许新建 -->
      <div
        v-if="allowCreate && inputVal.trim() && !exactMatch"
        class="tag-option tag-option--create"
        @mousedown.prevent="createTag"
      >
        <span class="opt-icon">＋</span>
        <span>创建标签 "{{ inputVal.trim() }}"</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

export interface TagOption {
  id?: number
  name: string
  color?: string
}

const props = withDefaults(defineProps<{
  modelValue: TagOption[]
  options?: TagOption[]        // 候选标签列表
  placeholder?: string
  maxTags?: number
  allowCreate?: boolean       // 允许输入新标签
}>(), {
  options: () => [],
  placeholder: '添加标签...',
  maxTags: 10,
  allowCreate: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', tags: TagOption[]): void
  (e: 'create', name: string): void   // 创建新标签事件
}>()

const inputRef = ref<HTMLInputElement>()
const inputVal = ref('')
const showDropdown = ref(false)

const selectedTags = computed(() => props.modelValue)

const filteredOptions = computed(() => {
  const keyword = inputVal.value.trim().toLowerCase()
  return props.options.filter(opt =>
    !selectedTags.value.find(t => t.name === opt.name) &&
    (!keyword || opt.name.toLowerCase().includes(keyword))
  ).slice(0, 8)
})

const exactMatch = computed(() =>
  props.options.some(o => o.name.toLowerCase() === inputVal.value.trim().toLowerCase())
)

const tagColors = ['#6366f1', '#10b981', '#f59e0b', '#ec4899', '#3b82f6', '#8b5cf6', '#ef4444', '#06b6d4']

function tagStyle(tag: TagOption) {
  const color = tag.color || '#6366f1'
  return {
    borderColor: color,
    color: color,
    background: color + '18'
  }
}

function selectTag(tag: TagOption) {
  if (selectedTags.value.length >= props.maxTags) return
  emit('update:modelValue', [...selectedTags.value, tag])
  inputVal.value = ''
  showDropdown.value = false
  inputRef.value?.focus()
}

function removeTag(tag: TagOption) {
  emit('update:modelValue', selectedTags.value.filter(t => t.name !== tag.name))
}

function createTag() {
  const name = inputVal.value.trim()
  if (!name) return
  const newTag: TagOption = {
    name,
    color: tagColors[Math.floor(Math.random() * tagColors.length)]
  }
  selectTag(newTag)
  emit('create', name)
}

function handleEnter() {
  if (filteredOptions.value.length === 1) {
    selectTag(filteredOptions.value[0])
  } else if (props.allowCreate && inputVal.value.trim()) {
    createTag()
  }
}

function handleBackspace() {
  if (!inputVal.value && selectedTags.value.length) {
    emit('update:modelValue', selectedTags.value.slice(0, -1))
  }
}

function handleInput() {
  showDropdown.value = true
}

function handleBlur() {
  setTimeout(() => { showDropdown.value = false }, 150)
}
</script>

<style lang="scss" scoped>
.tag-input {
  position: relative;
  min-height: 38px;

  .tags-wrap {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
    padding: 6px 10px;
    background: $bg-input;
    border: 1px solid $border;
    border-radius: $radius-sm;
    cursor: text;
    transition: $transition-fast;
    min-height: 38px;

    &:focus-within {
      border-color: $primary;
      box-shadow: 0 0 0 2px rgba($primary, 0.15);
    }
  }

  .tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 2px 8px;
    border-radius: $radius-full;
    border: 1px solid;
    font-size: 12px;
    line-height: 1.5;
    white-space: nowrap;

    &--removable {
      .tag-remove {
        cursor: pointer;
        font-size: 14px;
        line-height: 1;
        opacity: 0.7;
        transition: $transition-fast;

        &:hover { opacity: 1; }
      }
    }
  }

  .tag-input-field {
    border: none;
    background: transparent;
    outline: none;
    color: $text-primary;
    font-size: 13px;
    min-width: 80px;
    flex: 1;
    padding: 2px 0;

    &::placeholder {
      color: $text-muted;
    }
  }

  .tag-dropdown {
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    right: 0;
    background: $bg-card;
    border: 1px solid $border;
    border-radius: $radius-sm;
    box-shadow: $shadow-md;
    z-index: 100;
    overflow: hidden;

    .tag-option {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      font-size: 13px;
      color: $text-secondary;
      cursor: pointer;
      transition: $transition-fast;

      &:hover {
        background: $bg-input;
        color: $text-primary;
      }

      &--create {
        color: $primary-light;
        border-top: 1px solid $border;

        .opt-icon { font-size: 14px; }

        &:hover { background: $primary-100; }
      }

      .opt-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;
      }
    }
  }
}
</style>
