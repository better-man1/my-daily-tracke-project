#!/usr/bin/env python3
"""
annotate_vue.py — 为 miniapp/src/pages/ 和 miniapp/src/sub-pages/ 下所有 .vue 文件
添加详细中文注释。

用法: python annotate_vue.py
"""

import os
import glob

BASE = r"D:\Projects\ClaudeCodeProject\AAAAA\my-daily-project-claude\miniapp\src"

# 文件级别 JSDoc 注释模板
FILE_COMMENTS = {
    r"pages\index\index.vue": """<!--
/**
 * ============================================================================
 * index.vue — 首页仪表盘（Tab 主页）
 * ============================================================================
 *
 * 【页面说明】应用的主入口 Tab 页，展示每日概览仪表盘
 * 【路由路径】/pages/index/index（Tab 页，uni.switchTab 切换）
 * 【页面传参】无
 * 【Uni-app 关键 API】uni.switchTab / uni.navigateTo / uni.showToast / onShow
 * 【数据流】onShow 时并发加载仪表盘和计划，异步加载随机摘录
 * ============================================================================
 */
-->
""",

    r"pages\plan\plan.vue": """<!--
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
""",

    r"pages\accounting\accounting.vue": """<!--
/**
 * ============================================================================
 * accounting.vue — 记账管理页面（Tab 页）
 * ============================================================================
 *
 * 【页面说明】展示本月收支概览和记账记录列表，支持类型筛选和分页加载
 * 【路由路径】/pages/accounting/accounting（Tab 页，uni.switchTab）
 * 【页面传参】无
 * 【关键 API】uni.navigateTo / uni.showModal / onShow
 * ============================================================================
 */
-->
""",

    r"pages\excerpt\excerpt.vue": """<!--
/**
 * ============================================================================
 * excerpt.vue — 感悟摘录页面（Tab 页）
 * ============================================================================
 *
 * 【页面说明】展示摘录列表，支持随机回顾、来源筛选、收藏、详情弹窗
 * 【路由路径】/pages/excerpt/excerpt（Tab 页，uni.switchTab）
 * 【页面传参】无
 * 【关键 API】uni.navigateTo / uni.showModal / onShow
 * ============================================================================
 */
-->
""",

    r"pages\profile\profile.vue": """<!--
/**
 * ============================================================================
 * profile.vue — 个人中心（Tab 页）
 * ============================================================================
 *
 * 【页面说明】展示用户信息、使用统计、功能菜单、设置和退出登录
 * 【路由路径】/pages/profile/profile（Tab 页，uni.switchTab）
 * 【页面传参】无
 * 【关键 API】uni.navigateTo / uni.reLaunch / uni.showModal / onShow
 * ============================================================================
 */
-->
""",

    r"sub-pages\login\login.vue": """<!--
/**
 * ============================================================================
 * login.vue — 登录/注册页面
 * ============================================================================
 *
 * 【页面说明】用户登录和注册，支持模式切换、密码显隐
 * 【路由路径】/sub-pages/login/login
 * 【页面传参】无
 * 【关键 API】uni.switchTab / uni.showToast / uni.setStorageSync
 * ============================================================================
 */
-->
""",

    r"sub-pages\plan-add\plan-add.vue": """<!--
/**
 * ============================================================================
 * plan-add.vue — 计划新建/编辑页面
 * ============================================================================
 *
 * 【页面说明】创建或编辑每日计划，通过 id 参数区分编辑模式
 * 【路由路径】/sub-pages/plan-add/plan-add?date=...&id=...
 * 【参数】date（计划日期）id（编辑时传入）
 * 【关键 API】uni.navigateBack / uni.showToast / onLoad / picker
 * ============================================================================
 */
-->
""",

    r"sub-pages\accounting-add\accounting-add.vue": """<!--
/**
 * ============================================================================
 * accounting-add.vue — 记账新增页面
 * ============================================================================
 *
 * 【页面说明】记录一笔收支，选择类型/分类/金额/日期/账户
 * 【路由路径】/sub-pages/accounting-add/accounting-add
 * 【页面传参】无
 * 【关键 API】uni.navigateBack / uni.showToast / picker / onMounted
 * ============================================================================
 */
-->
""",

    r"sub-pages\excerpt-add\excerpt-add.vue": """<!--
/**
 * ============================================================================
 * excerpt-add.vue — 新增摘录页面
 * ============================================================================
 *
 * 【页面说明】创建一条摘录，输入内容、感悟、来源信息
 * 【路由路径】/sub-pages/excerpt-add/excerpt-add
 * 【页面传参】无
 * 【关键 API】uni.navigateBack / uni.showToast / picker
 * ============================================================================
 */
-->
""",

    r"sub-pages\summary\summary.vue": """<!--
/**
 * ============================================================================
 * summary.vue — 每日总结页面
 * ============================================================================
 *
 * 【页面说明】填写今日心情/评分/成就/改进/明日计划/感恩，支持编辑已有总结
 * 【路由路径】/sub-pages/summary/summary
 * 【页面传参】无
 * 【关键 API】uni.navigateBack / uni.showToast / onShow
 * ============================================================================
 */
-->
""",

    r"sub-pages\goal\goal.vue": """<!--
/**
 * ============================================================================
 * goal.vue — 目标管理页面
 * ============================================================================
 *
 * 【页面说明】展示个人OKR目标列表，按周期类型筛选，含进度条和关键结果
 * 【路由路径】/sub-pages/goal/goal
 * 【页面传参】无
 * 【关键 API】onShow
 * ============================================================================
 */
-->
""",
}

# Import comment replacements for each file
IMPORT_COMMENTS = {
    r"pages\index\index.vue": {
        "import { ref, computed } from 'vue'": "// Vue 3 Composition API — ref 响应式声明，computed 计算属性\nimport { ref, computed } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期 onShow — Tab 页每次显示时触发\nimport { onShow } from '@dcloudio/uni-app'",
        "import { useUserStore } from '@/stores/user'": "// Pinia 用户 Store — 获取登录状态和用户信息\nimport { useUserStore } from '@/stores/user'",
        "import { dashboardApi } from '@/api/dashboard'": "// 仪表盘 API — 获取今日概览数据\nimport { dashboardApi } from '@/api/dashboard'",
        "import { planApi, type PlanItem } from '@/api/plan'": "// 计划 API — 获取列表、更新状态\nimport { planApi, type PlanItem } from '@/api/plan'",
        "import { excerptApi, type ExcerptItem } from '@/api/excerpt'": "// 摘录 API — 获取随机摘录\nimport { excerptApi, type ExcerptItem } from '@/api/excerpt'",
        "import { formatDate, getWeekDay, formatMoney } from '@/utils/date'": "// 日期工具 — formatDate 格式化、getWeekDay 星期、formatMoney 金额\nimport { formatDate, getWeekDay, formatMoney } from '@/utils/date'",
    },
    r"pages\plan\plan.vue": {
        "import { ref, computed } from 'vue'": "// Vue 3 Composition API\nimport { ref, computed } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期\nimport { onShow } from '@dcloudio/uni-app'",
        "import { planApi, type PlanItem, type PlanStatistics } from '@/api/plan'": "// 计划 API — 列表/统计/更新/延期\nimport { planApi, type PlanItem, type PlanStatistics } from '@/api/plan'",
        "import { formatDate, getWeekDay, formatDateCN } from '@/utils/date'": "// 日期工具\nimport { formatDate, getWeekDay, formatDateCN } from '@/utils/date'",
        "import dayjs from 'dayjs'": "// dayjs — 轻量日期处理库\nimport dayjs from 'dayjs'",
    },
    r"pages\accounting\accounting.vue": {
        "import { ref, computed } from 'vue'": "// Vue 3 Composition API\nimport { ref, computed } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期\nimport { onShow } from '@dcloudio/uni-app'",
        "import { accountingApi, type AccountingItem, type AccountingStats } from '@/api/accounting'": "// 记账 API — 分页列表/月度统计/删除\nimport { accountingApi, type AccountingItem, type AccountingStats } from '@/api/accounting'",
        "import { formatMoney } from '@/utils/date'": "// 金额格式化\nimport { formatMoney } from '@/utils/date'",
        "import dayjs from 'dayjs'": "// dayjs 日期处理\nimport dayjs from 'dayjs'",
    },
    r"pages\excerpt\excerpt.vue": {
        "import { ref } from 'vue'": "// Vue 3 Composition API\nimport { ref } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期\nimport { onShow } from '@dcloudio/uni-app'",
        "import { excerptApi, type ExcerptItem } from '@/api/excerpt'": "// 摘录 API — 随机/列表/收藏/删除\nimport { excerptApi, type ExcerptItem } from '@/api/excerpt'",
    },
    r"pages\profile\profile.vue": {
        "import { ref, computed } from 'vue'": "// Vue 3 Composition API\nimport { ref, computed } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期\nimport { onShow } from '@dcloudio/uni-app'",
        "import { useUserStore } from '@/stores/user'": "// Pinia 用户 Store\nimport { useUserStore } from '@/stores/user'",
        "import { summaryApi } from '@/api/summary'": "// 总结 API — 打卡/今日总结\nimport { summaryApi } from '@/api/summary'",
        "import { goalApi } from '@/api/goal'": "// 目标 API — 统计\nimport { goalApi } from '@/api/goal'",
        "import { excerptApi } from '@/api/excerpt'": "// 摘录 API\nimport { excerptApi } from '@/api/excerpt'",
        "import { planApi } from '@/api/plan'": "// 计划 API\nimport { planApi } from '@/api/plan'",
    },
    r"sub-pages\login\login.vue": {
        "import { ref } from 'vue'": "// Vue 3 Composition API\nimport { ref } from 'vue'",
        "import { useUserStore } from '@/stores/user'": "// Pinia 用户 Store\nimport { useUserStore } from '@/stores/user'",
        "import { authApi } from '@/api/auth'": "// 认证 API — 登录/注册\nimport { authApi } from '@/api/auth'",
    },
    r"sub-pages\plan-add\plan-add.vue": {
        "import { ref, computed, onMounted } from 'vue'": "// Vue 3 Composition API\nimport { ref, computed, onMounted } from 'vue'",
        "import { onLoad } from '@dcloudio/uni-app'": "// Uni-app onLoad — 页面加载时接收参数\nimport { onLoad } from '@dcloudio/uni-app'",
        "import { planApi, type PlanItem } from '@/api/plan'": "// 计划 API — 创建/更新\nimport { planApi, type PlanItem } from '@/api/plan'",
        "import { getToday } from '@/utils/date'": "// 日期工具\nimport { getToday } from '@/utils/date'",
    },
    r"sub-pages\accounting-add\accounting-add.vue": {
        "import { ref, onMounted } from 'vue'": "// Vue 3 Composition API\nimport { ref, onMounted } from 'vue'",
        "import { accountingApi } from '@/api/accounting'": "// 记账 API — 创建/获取分类\nimport { accountingApi } from '@/api/accounting'",
        "import { getToday } from '@/utils/date'": "// 日期工具\nimport { getToday } from '@/utils/date'",
        "import dayjs from 'dayjs'": "// dayjs 日期处理\nimport dayjs from 'dayjs'",
    },
    r"sub-pages\excerpt-add\excerpt-add.vue": {
        "import { ref } from 'vue'": "// Vue 3 Composition API\nimport { ref } from 'vue'",
        "import { excerptApi } from '@/api/excerpt'": "// 摘录 API — 创建\nimport { excerptApi } from '@/api/excerpt'",
        "import { getToday } from '@/utils/date'": "// 日期工具\nimport { getToday } from '@/utils/date'",
    },
    r"sub-pages\summary\summary.vue": {
        "import { ref } from 'vue'": "// Vue 3 Composition API\nimport { ref } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期\nimport { onShow } from '@dcloudio/uni-app'",
        "import { summaryApi, type SummaryItem } from '@/api/summary'": "// 总结 API — 打卡/今日总结/创建/更新\nimport { summaryApi, type SummaryItem } from '@/api/summary'",
        "import { getToday } from '@/utils/date'": "// 日期工具\nimport { getToday } from '@/utils/date'",
    },
    r"sub-pages\goal\goal.vue": {
        "import { ref, computed } from 'vue'": "// Vue 3 Composition API\nimport { ref, computed } from 'vue'",
        "import { onShow } from '@dcloudio/uni-app'": "// Uni-app 生命周期\nimport { onShow } from '@dcloudio/uni-app'",
        "import { goalApi, type GoalItem } from '@/api/goal'": "// 目标 API — 获取列表\nimport { goalApi, type GoalItem } from '@/api/goal'",
    },
}


def annotate_file(filepath):
    """为单个 .vue 文件添加注释"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    original = content

    rel_path = os.path.relpath(filepath, BASE)

    # 1. 添加文件级 JSDoc 注释块
    file_header = FILE_COMMENTS.get(rel_path)
    if file_header and not content.lstrip().startswith('<!--'):
        content = file_header + '\n' + content

    # 2. 添加 import 语句注释
    import_map = IMPORT_COMMENTS.get(rel_path, {})
    for old_import, new_import in import_map.items():
        if old_import in content and new_import not in content:
            content = content.replace(old_import, new_import)

    # 3. 在 script 块内添加 section 分隔注释
    sections = {
        "const userStore = useUserStore()": "\n// ============================================================================\n// 状态与数据\n// ============================================================================\n",
        "async function load": "\n// ============================================================================\n// 数据加载\n// ============================================================================\n",
        "function go": "\n// ============================================================================\n// 页面导航\n// ============================================================================\n",
        "async function toggle": "\n// ============================================================================\n// 交互操作\n// ============================================================================\n",
        "async function handle": "\n// ============================================================================\n// 表单提交\n// ============================================================================\n",
        "onShow((": "\n// ============================================================================\n// 生命周期\n// ============================================================================\n",
        "onLoad((": "\n// ============================================================================\n// 生命周期\n// ============================================================================\n",
        "onMounted((": "\n// ============================================================================\n// 生命周期\n// ============================================================================\n",
    }

    for marker, comment in sections.items():
        if marker in content and comment not in content:
            idx = content.find(marker)
            last_newline = content.rfind('\n', 0, idx)
            content = content[:last_newline+1] + comment + content[last_newline+1:]

    # Write only if changed
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"  [OK] Annotated: {rel_path}")
    else:
        print(f"  [--] No changes: {rel_path}")


def main():
    dirs_to_scan = [
        os.path.join(BASE, "pages"),
        os.path.join(BASE, "sub-pages"),
    ]
    for scan_dir in dirs_to_scan:
        if not os.path.isdir(scan_dir):
            print(f"  [SKIP] Directory not found: {scan_dir}")
            continue
        print(f"\nScanning: {scan_dir}")
        for filepath in glob.glob(os.path.join(scan_dir, "**", "*.vue"), recursive=True):
            annotate_file(filepath)

    print("\nDone!")


if __name__ == "__main__":
    main()