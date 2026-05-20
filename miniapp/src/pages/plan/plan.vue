<!--
/**
 * ============================================================================
 * plan.vue — 计划管理页面（Tab 页）
 * ============================================================================
 *
 * 【页面说明】展示指定日期的计划列表，支持日期翻页、状态筛选、切换完成/延期
 * 【路由路径】/pages/plan/plan（Tab 页，uni.switchTab）
 * 【页面传参】无
 * 【关键 API】uni.switchTab / uni.navigateTo / uni.showModal / onShow
 * 【数据流】onShow 并发加载列表和统计，操作后局部刷新
 * ============================================================================
 */
-->

<template>
  <view class="page-container">
    <dt-navbar title="计划管理">
      <template #right>
        <view class="nav-add-btn" @tap="goAdd">
          <text class="nav-add-icon">+</text>
        </view>
      </template>
    </dt-navbar>

    <view class="page-content">
      <!-- 日期选择器 -->
      <view class="date-selector">
        <view class="date-arrow" @tap="changeDate(-1)">
          <text class="arrow-icon">‹</text>
        </view>
        <view class="date-display" @tap="showDatePicker">
          <text class="date-text">{{ displayDate }}</text>
          <text class="date-weekday">{{ weekdayStr }}</text>
        </view>
        <view class="date-arrow" @tap="changeDate(1)">
          <text class="arrow-icon">›</text>
        </view>
        <view v-if="currentDate !== today" class="date-today-btn" @tap="goToday">
          <text class="date-today-text">今天</text>
        </view>
      </view>

      <!-- 统计栏 -->
      <view v-if="stats" class="stats-bar fade-in">
        <view class="stats-item">
          <text class="stats-value">{{ stats.total }}</text>
          <text class="stats-label">总计</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value text-success">{{ stats.done }}</text>
          <text class="stats-label">已完成</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value text-primary">{{ stats.inProgress }}</text>
          <text class="stats-label">进行中</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value text-warning">{{ stats.todo }}</text>
          <text class="stats-label">待办</text>
        </view>
        <view class="stats-divider" />
        <view class="stats-item">
          <text class="stats-value" :class="rateClass">{{ stats.completionRate || 0 }}%</text>
          <text class="stats-label">完成率</text>
        </view>
      </view>

      <!-- 筛选标签 -->
      <scroll-view scroll-x class="filter-scroll">
        <view class="filter-tags">
          <view v-for="f in filters" :key="f.value"
            class="filter-tag"
            :class="{ 'filter-tag--active': activeFilter === f.value }"
            @tap="activeFilter = f.value">
            <text class="filter-tag__text">{{ f.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 计划列表 -->
      <view v-if="filteredPlans.length > 0" class="plan-list">
        <view v-for="(item, idx) in filteredPlans" :key="item.id"
          class="plan-card slide-up"
          :style="{ animationDelay: (idx * 0.05) + 's' }">

          <!-- 优先级指示条 -->
          <view class="plan-card__priority-bar" :class="'priority-' + item.priority.toLowerCase()" />

          <view class="plan-card__main">
            <!-- 复选框 -->
            <view class="plan-card__check"
              :class="{ 'plan-card__check--done': item.status === 'DONE' }"
              @tap.stop="toggleStatus(item)">
              <text v-if="item.status === 'DONE'" class="check-icon">✓</text>
            </view>

            <!-- 内容 -->
            <view class="plan-card__body" @tap="goDetail(item)">
              <text class="plan-card__title"
                :class="{ 'plan-card__title--done': item.status === 'DONE' }">
                {{ item.title }}
              </text>
              <view class="plan-card__info">
                <text class="plan-card__tag"
                  :class="'tag-' + item.priority.toLowerCase()">
                  {{ item.priority }}
                </text>
                <text class="plan-card__cat">{{ categoryLabel(item.category) }}</text>
                <text v-if="item.estimatedMins" class="plan-card__time">
                  ⏱ {{ item.estimatedMins }}min
                </text>
              </view>
            </view>

            <!-- 操作按钮 -->
            <view class="plan-card__actions">
              <view v-if="item.status !== 'DONE'" class="action-btn" @tap.stop="postponePlan(item)">
                <text class="action-icon">→</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <dt-empty v-else icon="📋" text="今天还没有计划" action-text="添加计划" @action="goAdd" />
    </view>

    <!-- 浮动添加按钮 -->
    <view class="fab-button safe-area-bottom" @tap="goAdd">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// Vue 3 Composition API
import { ref, computed } from 'vue'
// Uni-app 生命周期
import { onShow } from '@dcloudio/uni-app'
// 计划 API — 列表/统计/更新/延期
import { planApi, type PlanItem, type PlanStatistics } from '@/api/plan'
// 日期工具
import { formatDate, getWeekDay, formatDateCN } from '@/utils/date'
// dayjs — 轻量日期处理库
import dayjs from 'dayjs'

const today = formatDate()
const currentDate = ref(today)
const plans = ref<PlanItem[]>([])
const stats = ref<PlanStatistics | null>(null)
const activeFilter = ref('ALL')

const filters = [
  { label: '全部', value: 'ALL' },
  { label: '待办', value: 'TODO' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

/**
 * 显示日期文本
 *
 * 功能说明：将日期转换为更友好的显示格式
 * 业务逻辑：
 * - 今天：显示"今天"
 * - 昨天/明天：显示相对日期
 * - 其他：显示中文格式（如"2024年1月1日"）
 *
 * 使用场景：在日期选择器中心显示
 *
 * @returns 格式化后的日期字符串
 */
const displayDate = computed(() => {
  if (currentDate.value === today) return '今天'
  const d = dayjs(currentDate.value)
  const yesterday = dayjs().subtract(1, 'day').format('YYYY-MM-DD')
  const tomorrow = dayjs().add(1, 'day').format('YYYY-MM-DD')
  if (currentDate.value === yesterday) return '昨天'
  if (currentDate.value === tomorrow) return '明天'
  return formatDateCN(currentDate.value)
})

/**
 * 星期几文本
 *
 * 功能说明：获取当前日期对应的星期几
 * 使用场景：在日期选择器中心显示星期信息
 *
 * @returns 星期几文本（如"星期一"）
 */
const weekdayStr = computed(() => getWeekDay(currentDate.value))

/**
 * 完成率样式类
 *
 * 功能说明：根据完成率返回对应的颜色类名
 * 业务逻辑：
 * - >= 80%：text-success（绿色）
 * - >= 50%：text-primary（主色）
 * - < 50%：text-warning（橙色）
 *
 * 使用场景：在统计栏中为完成率添加颜色
 *
 * @returns 颜色类名
 */
const rateClass = computed(() => {
  const rate = stats.value?.completionRate || 0
  if (rate >= 80) return 'text-success'
  if (rate >= 50) return 'text-primary'
  return 'text-warning'
})

/**
 * 筛选后的计划列表
 *
 * 功能说明：根据当前筛选条件过滤计划列表
 * 业务逻辑：
 * - 筛选条件为"全部"时返回所有计划
 * - 否则只返回状态匹配的计划
 *
 * 使用场景：渲染计划列表时使用
 *
 * @returns 过滤后的计划数组
 */
const filteredPlans = computed(() => {
  if (activeFilter.value === 'ALL') return plans.value
  return plans.value.filter(p => p.status === activeFilter.value)
})

/**
 * 分类标签映射
 *
 * 功能说明：将分类英文代码转换为中文标签
 * 映射关系：
 * - WORK → 工作
 * - STUDY → 学习
 * - LIFE → 生活
 * - HEALTH → 健康
 * - 其他 → 原值
 *
 * 使用场景：在计划卡片上显示分类标签
 *
 * @param cat - 分类英文代码
 * @returns 分类中文名称
 */
const categoryLabel = (cat: string) => {
  const map: Record<string, string> = { WORK: '工作', STUDY: '学习', LIFE: '生活', HEALTH: '健康' }
  return map[cat] || cat
}

/**
 * 切换日期
 *
 * 功能说明：向前或向后切换一天
 * 业务逻辑：
 * 1. 使用dayjs计算新日期
 * 2. 更新currentDate状态
 * 3. 重新加载该日期的计划数据
 *
 * 使用场景：用户点击日期箭头按钮时调用
 *
 * @param delta - 切换天数（-1表示前一天，1表示后一天）
 */
function changeDate(delta: number) {
  currentDate.value = dayjs(currentDate.value).add(delta, 'day').format('YYYY-MM-DD')
  loadData()
}


// ============================================================================
// 页面导航
// ============================================================================

/**
 * 跳转到今天
 *
 * 功能说明：重置日期为今天并重新加载数据
 * 使用场景：用户点击"今天"按钮时调用
 */
function goToday() {
  currentDate.value = today
  loadData()
}

/**
 * 显示日期选择器
 *
 * 功能说明：弹出系统日期选择器让用户选择日期
 * 注意：预留接口，待后续实现
 */
function showDatePicker() {
  // 使用 uni 内置日期选择器
}


// ============================================================================
// 数据加载
// ============================================================================

/**
 * 加载计划数据和统计信息
 *
 * 功能说明：并发请求当前日期的计划列表和统计数据
 * 业务逻辑：
 * 1. 并行调用两个接口：计划列表和统计数据
 * 2. 更新plans和stats响应式变量
 * 3. 错误处理：记录错误日志但不中断流程
 *
 * 使用场景：页面显示、日期改变、计划操作后调用
 */
async function loadData() {
  try {
    const [planList, planStats] = await Promise.all([
      planApi.list(currentDate.value),
      planApi.statistics(currentDate.value)
    ])
    plans.value = planList || []
    stats.value = planStats || null
  } catch (err) {
    console.error('Load plans error:', err)
  }
}


// ============================================================================
// 交互操作
// ============================================================================

/**
 * 切换任务完成状态
 *
 * 功能说明：点击复选框切换任务的完成/未完成状态
 * 业务逻辑：
 * 1. 计算新状态（DONE ↔ TODO）
 * 2. 调用API更新状态
 * 3. 本地更新状态（乐观更新）
 * 4. 刷新统计数据
 * 5. 失败时显示错误提示
 *
 * 使用场景：用户点击任务复选框时调用
 *
 * @param item - 计划任务对象
 */
async function toggleStatus(item: PlanItem) {
  const newStatus = item.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    await planApi.updateStatus(item.id, newStatus)
    item.status = newStatus as PlanItem['status']
    // 刷新统计
    const s = await planApi.statistics(currentDate.value)
    stats.value = s
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

/**
 * 延期计划
 *
 * 功能说明：将计划延期到明天
 * 业务逻辑：
 * 1. 弹出确认对话框
 * 2. 用户确认后调用延期API
 * 3. 成功后显示提示并刷新列表
 * 4. 失败时显示错误提示
 *
 * 使用场景：用户点击任务右侧的延期箭头按钮时调用
 *
 * @param item - 计划任务对象
 */
async function postponePlan(item: PlanItem) {
  uni.showModal({
    title: '延期计划',
    content: `确定将「${item.title}」延期到明天吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          await planApi.postpone(item.id)
          uni.showToast({ title: '已延期', icon: 'success' })
          loadData()
        } catch {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 跳转到新增计划页面
 *
 * 功能说明：打开计划新增页面，默认日期为当前选择日期
 * 使用场景：用户点击浮动添加按钮或导航栏添加按钮时调用
 */
function goAdd() {
  uni.navigateTo({
    url: `/sub-pages/plan-add/plan-add?date=${currentDate.value}`
  })
}

/**
 * 跳转到计划详情（编辑）页面
 *
 * 功能说明：打开计划编辑页面，复用新增页面组件
 * 业务逻辑：传递计划ID和日期参数
 *
 * 使用场景：用户点击任务卡片时调用
 *
 * @param item - 计划任务对象
 */
function goDetail(item: PlanItem) {
  // 简单编辑——复用新增页面
  uni.navigateTo({
    url: `/sub-pages/plan-add/plan-add?id=${item.id}&date=${currentDate.value}`
  })
}


// ============================================================================
// 生命周期
// ============================================================================
onShow(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
// 日期选择器
.date-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  padding: 20rpx 0;
  margin-bottom: 16rpx;
}

.date-arrow {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $dt-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  .arrow-icon {
    font-size: 44rpx;
    color: $dt-text-secondary;
    font-weight: 300;
    line-height: 1;
  }
}

.date-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 160rpx;

  .date-text {
    font-size: $dt-font-xl;
    font-weight: 700;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
  }

  .date-weekday {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }
}

.date-today-btn {
  padding: 8rpx 24rpx;
  background: $dt-primary-bg;
  border-radius: $dt-radius-full;

  .date-today-text {
    font-size: $dt-font-xs;
    color: $dt-primary;
    font-weight: 500;
  }
}

// 统计栏
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  padding: 24rpx 16rpx;
  margin-bottom: $dt-space-lg;
  box-shadow: $dt-shadow-sm;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.stats-value {
  font-size: $dt-font-lg;
  font-weight: 700;
  color: $dt-text-primary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }
}

.stats-label {
  font-size: $dt-font-xs;
  color: $dt-text-secondary;
  @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
}

.stats-divider {
  width: 1rpx;
  height: 48rpx;
  background: $dt-divider;
  @media (prefers-color-scheme: dark) { background: $dt-dark-border; }
}

// 筛选标签
.filter-scroll {
  white-space: nowrap;
  margin-bottom: $dt-space-lg;
}

.filter-tags {
  display: inline-flex;
  gap: 16rpx;
  padding: 0 4rpx;
}

.filter-tag {
  display: inline-flex;
  padding: 12rpx 28rpx;
  border-radius: $dt-radius-full;
  background: $dt-bg-card;
  box-shadow: $dt-shadow-sm;
  transition: all 0.2s;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &--active {
    background: $dt-primary;
    box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.3);
  }

  &__text {
    font-size: $dt-font-sm;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &--active &__text {
    color: #FFFFFF;
    font-weight: 500;
  }
}

// 计划卡片
.plan-list {
  padding-bottom: 120rpx;
}

.plan-card {
  background: $dt-bg-card;
  border-radius: $dt-radius-lg;
  margin-bottom: 16rpx;
  overflow: hidden;
  box-shadow: $dt-shadow-sm;
  position: relative;

  @media (prefers-color-scheme: dark) { background: $dt-dark-bg-card; }

  &__priority-bar {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx;

    &.priority-p0 { background: $dt-danger; }
    &.priority-p1 { background: $dt-warning; }
    &.priority-p2 { background: $dt-primary; }
    &.priority-p3 { background: $dt-info; }
  }

  &__main {
    display: flex;
    align-items: center;
    padding: 28rpx 24rpx 28rpx 32rpx;
    gap: 20rpx;
  }

  &__check {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    border: 3rpx solid $dt-border;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: all 0.3s ease;

    &--done {
      background: $dt-success;
      border-color: $dt-success;
    }

    .check-icon {
      color: #fff;
      font-size: 26rpx;
      font-weight: 700;
    }
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    min-width: 0;
  }

  &__title {
    font-size: $dt-font-md;
    font-weight: 500;
    color: $dt-text-primary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-primary; }

    &--done {
      text-decoration: line-through;
      color: $dt-text-placeholder;
    }
  }

  &__info {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  &__tag {
    font-size: 20rpx;
    padding: 2rpx 12rpx;
    border-radius: 6rpx;
    font-weight: 500;

    &.tag-p0 { background: rgba(239, 68, 68, 0.1); color: $dt-danger; }
    &.tag-p1 { background: rgba(245, 158, 11, 0.1); color: $dt-warning; }
    &.tag-p2 { background: rgba(99, 102, 241, 0.1); color: $dt-primary; }
    &.tag-p3 { background: rgba(107, 114, 128, 0.1); color: $dt-info; }
  }

  &__cat, &__time {
    font-size: $dt-font-xs;
    color: $dt-text-secondary;
    @media (prefers-color-scheme: dark) { color: $dt-dark-text-secondary; }
  }

  &__actions {
    flex-shrink: 0;
  }
}

.action-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(245, 158, 11, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;

  .action-icon {
    font-size: 28rpx;
    color: $dt-warning;
  }
}

// 导航栏添加按钮
.nav-add-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $dt-primary;
  display: flex;
  align-items: center;
  justify-content: center;

  .nav-add-icon {
    color: #fff;
    font-size: 36rpx;
    font-weight: 300;
    line-height: 1;
  }
}

// 浮动按钮
.fab-button {
  position: fixed;
  right: 32rpx;
  bottom: calc(180rpx + env(safe-area-inset-bottom));
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $dt-primary, $dt-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.4);
  z-index: 100;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.9);
  }

  .fab-icon {
    color: #FFFFFF;
    font-size: 56rpx;
    font-weight: 300;
    line-height: 1;
  }
}
</style>
