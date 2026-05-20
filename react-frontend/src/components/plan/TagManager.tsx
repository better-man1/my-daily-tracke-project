/**
 * ============================================================================
 * TagManager.tsx — 计划标签管理组件
 * ============================================================================
 *
 * 【组件说明】
 * 用于每日计划页面的标签管理，提供完整的 CRUD 功能。
 * 支持颜色选择、标签选中/取消。
 * ============================================================================
 */

import React, { useState, useEffect } from 'react'
import { Button, Modal, Form, Input, Dropdown, Space, message, Empty } from 'antd'
import { PlusOutlined, MoreOutlined } from '@ant-design/icons'
import type { MenuProps } from 'antd'
import { tagApi } from '@/api/plan'
import type { TagItem } from '@/api/plan'
import './TagManager.css'

/**
 * TagManagerProps — 声明组件外部接口的属性
 * 
 * 1. 【React 核心概念：单向数据流与父子组件通信 (Parent-Child Callback)】
 *    - 在 React 中，数据流动是单向的（自上而下，即父传子）。
 *    - 如果子组件修改了数据，不能直接篡改父组件传入的属性值，而是必须通过“调用父组件传下来的回调函数”通知父组件：
 *      - `selectedTagIds`：父组件传递进来的已选中标签数组（父传子）。
 *      - `onChange`：一个可选的回调函数。当子组件的选中项发生变化时，调用该方法通知父组件更新数据（子传父）。
 */
interface TagManagerProps {
  onChange?: (tagIds: number[]) => void
  selectedTagIds?: number[]
}

const presetColors = [
  '#6366f1', '#8b5cf6', '#ec4899',
  '#ef4444', '#f97316', '#f59e0b',
  '#10b981', '#14b8a6', '#06b6d4',
  '#3b82f6', '#64748b', '#78716c'
]

const TagManager: React.FC<TagManagerProps> = ({ onChange, selectedTagIds = [] }) => {
  // ============================================================================
  // // 状态（State）
  // ============================================================================
  
  // 标签数据库列表状态
  const [tags, setTags] = useState<TagItem[]>([])
  
  /**
   * 2. 【React 核心概念：派生状态与 Props 同步 (Syncing State with Props)】
   *    - 挑战：父组件可能在外部清除选中标签，此时子组件的本地 `localSelectedTagIds` 如何保持最新？
   *    - 方案：通过 `useState(selectedTagIds)` 进行初始化，并配合底层的 `useEffect` 副作用监听：
   *      - 一旦父组件传入的 `selectedTagIds` 引用或内容发生变化，就自动调用 `setLocalSelectedTagIds` 进行本地覆盖同步。
   */
  const [localSelectedTagIds, setLocalSelectedTagIds] = useState<number[]>(selectedTagIds)
  const [showTagDialog, setShowTagDialog] = useState(false)
  const [editingTag, setEditingTag] = useState<TagItem | null>(null)
  const [saving, setSaving] = useState(false)
  const [form] = Form.useForm()
  const [selectedColor, setSelectedColor] = useState<string>(presetColors[0])

  // ============================================================================
  // // 数据加载
  // ============================================================================
  const loadTags = async () => {
    try {
      const data = await tagApi.list()
      setTags(data)
    } catch (error) {
      console.error('加载标签失败', error)
    }
  }

  // ============================================================================
  // // 交互处理
  // ============================================================================
  /**
   * 打开新建标签弹窗
   */
  const handleCreateTag = () => {
    setEditingTag(null)
    form.resetFields()
    // 随机挑选预设颜色，提升用户体验
    const randomColor = presetColors[Math.floor(Math.random() * presetColors.length)]
    setSelectedColor(randomColor)
    form.setFieldsValue({ color: randomColor })
    setShowTagDialog(true)
  }

  /**
   * 切换标签的选中/取消状态
   */
  const toggleTag = (tagId: number) => {
    const index = localSelectedTagIds.indexOf(tagId)
    let newIds: number[]

    // 若已经选中过，过滤掉它（取消选择）；否则使用解构追加到新数组（选择）
    if (index > -1) {
      newIds = localSelectedTagIds.filter(id => id !== tagId)
    } else {
      newIds = [...localSelectedTagIds, tagId]
    }

    // 1. 更新本地组件渲染状态
    setLocalSelectedTagIds(newIds)
    // 2. 触发回调，通知父组件：最新的已选标签 ID 数组为 newIds
    onChange?.(newIds)
  }

  /**
   * 编辑/删除标签的下拉选项操作
   */
  const handleTagCommand = (cmd: 'edit' | 'delete', tag: TagItem) => {
    if (cmd === 'edit') {
      setEditingTag(tag)
      setSelectedColor(tag.color)
      form.setFieldsValue({
        name: tag.name,
        color: tag.color
      })
      setShowTagDialog(true)
    } else if (cmd === 'delete') {
      Modal.confirm({
        title: '提示',
        content: `确认删除标签"${tag.name}"？`,
        okType: 'danger',
        onOk: async () => {
          try {
            await tagApi.delete(tag.id)
            message.success('删除成功')
            loadTags() // 刷新列表
          } catch (error) {
            console.error('删除标签失败', error)
          }
        }
      })
    }
  }

  // 生成操作菜单项的数据格式 (Antd Dropdown 规范)
  const getDropdownItems = (tag: TagItem): MenuProps => ({
    items: [
      {
        key: 'edit',
        label: '编辑',
        onClick: () => handleTagCommand('edit', tag)
      },
      {
        key: 'delete',
        label: <span style={{ color: '#ef4444' }}>删除</span>,
        onClick: () => handleTagCommand('delete', tag)
      }
    ]
  })

  // ============================================================================
  // // 表单提交
  // ============================================================================
  const saveTag = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)

      if (editingTag) {
        await tagApi.update(editingTag.id, values)
        message.success('更新成功')
      } else {
        await tagApi.create(values)
        message.success('创建成功')
      }

      setShowTagDialog(false)
      loadTags()
    } catch (error) {
      console.error('保存标签失败', error)
      if (error instanceof Error && error.message !== 'Validation failed') {
        message.error('保存失败')
      }
    } finally {
      setSaving(false)
    }
  }

  // ============================================================================
  // // 生命周期
  // ============================================================================
  
  // 首次挂载时，从数据库获取最新标签列表
  useEffect(() => {
    loadTags()
  }, [])

  // 依赖项包含 selectedTagIds，当父组件在外部做清除/变动时，实时同步更新本地 state
  useEffect(() => {
    setLocalSelectedTagIds(selectedTagIds)
  }, [selectedTagIds])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <div className="tag-manager">
      {/* 头部标题区 */}
      <div className="tag-header">
        <h4>标签管理</h4>
        <Button type="primary" size="small" icon={<PlusOutlined />} onClick={handleCreateTag}>
          新建标签
        </Button>
      </div>

      {/* 标签网格列表展示 */}
      <div className="tag-list">
        {tags.map(tag => (
          <div
            key={tag.id}
            // 动态设置选中样式类
            className={`tag-item ${localSelectedTagIds.includes(tag.id) ? 'selected' : ''}`}
            onClick={() => toggleTag(tag.id)}
          >
            <span className="tag-dot" style={{ background: tag.color }}></span>
            <span className="tag-name">{tag.name}</span>
            {/*
              阻止事件冒泡 e.stopPropagation()：
              Dropdown 上的三个点操作按钮本身被放置在 .tag-item 卡片里。
              我们点击“下拉操作”时，并不希望触发 toggleTag 切换其选中状态。
              故必须通过调用 stopPropagation() 截断事件向上层 DOM 冒泡传播。
            */}
            <Dropdown menu={getDropdownItems(tag)} trigger={['click']}>
              <Button
                type="text"
                size="small"
                icon={<MoreOutlined />}
                onClick={e => e.stopPropagation()} // 阻止冒泡，避免触发切换标签选中
                className="tag-more"
              />
            </Dropdown>
          </div>
        ))}

        {/* 标签为空时的缺省提示 */}
        {tags.length === 0 && (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="暂无标签，点击上方按钮创建"
          />
        )}
      </div>

      {/* 编辑/创建标签弹窗 */}
      <Modal
        title={editingTag ? '编辑标签' : '新建标签'}
        open={showTagDialog}
        onOk={saveTag}
        onCancel={() => setShowTagDialog(false)}
        confirmLoading={saving}
        width={400}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="标签名称"
            rules={[
              { required: true, message: '请输入标签名称' },
              { max: 50, message: '标签名称不能超过50个字符' }
            ]}
          >
            <Input placeholder="输入标签名称" />
          </Form.Item>

          <Form.Item
            name="color"
            label="标签颜色"
            initialValue={presetColors[0]}
          >
            {/* 颜色选择小圆点面板 */}
            <div className="color-picker">
              {presetColors.map(color => (
                <div
                  key={color}
                  className={`color-option ${selectedColor === color ? 'selected' : ''}`}
                  style={{ background: color }}
                  onClick={() => {
                    setSelectedColor(color)
                    form.setFieldsValue({ color }) // 表单手动设置颜色字段的值
                  }}
                ></div>
              ))}
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TagManager