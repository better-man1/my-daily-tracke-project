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
  // // 状态
  // ============================================================================
  const [tags, setTags] = useState<TagItem[]>([])
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
      console.error('Failed to load tags', error)
    }
  }

  // ============================================================================
  // // 交互处理
  // ============================================================================
  /**
   * 打开新建标签弹窗
   *
   * 功能说明：初始化表单并显示弹窗（新增模式）
   * 业务逻辑：
   * 1. 清空编辑标识（设置为null表示新增）
   * 2. 重置表单名称为空字符串
   * 3. 随机选择一个预设颜色
   * 4. 显示弹窗
   *
   * 使用场景：用户点击"新建标签"按钮时调用
   */
  const handleCreateTag = () => {
    setEditingTag(null)
    form.resetFields()
    const randomColor = presetColors[Math.floor(Math.random() * presetColors.length)]
    setSelectedColor(randomColor)
    form.setFieldsValue({ color: randomColor })
    setShowTagDialog(true)
  }

  /**
   * 切换标签选中状态
   *
   * 功能说明：点击标签项时切换其选中/未选中状态
   * 业务逻辑：
   * 1. 查找标签ID是否在选中列表中
   * 2. 如果已选中，则移除（取消选中）
   * 3. 如果未选中，则添加（选中）
   * 4. 向父组件发出change事件，传递更新后的选中ID列表
   *
   * 使用场景：用户点击标签项时调用
   *
   * @param tagId - 标签ID
   */
  const toggleTag = (tagId: number) => {
    const index = localSelectedTagIds.indexOf(tagId)
    let newIds: number[]

    if (index > -1) {
      newIds = localSelectedTagIds.filter(id => id !== tagId)
    } else {
      newIds = [...localSelectedTagIds, tagId]
    }

    setLocalSelectedTagIds(newIds)
    onChange?.(newIds)
  }

  /**
   * 处理标签操作命令
   *
   * 功能说明：处理标签项的编辑和删除操作
   * 业务逻辑：
   * - 编辑命令：
   *   1. 设置编辑标识为当前标签
   *   2. 填充表单数据
   *   3. 显示弹窗
   * - 删除命令：
   *   1. 弹出确认对话框
   *   2. 调用API删除标签
   *   3. 成功后刷新标签列表
   *
   * 使用场景：用户点击标签项的操作菜单时调用
   *
   * @param cmd - 命令类型：'edit' 或 'delete'
   * @param tag - 标签对象
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
            loadTags()
          } catch (error) {
            console.error('Failed to delete tag', error)
          }
        }
      })
    }
  }

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
  /**
   * 保存标签
   *
   * 功能说明：新增或编辑标签
   * 业务逻辑：
   * 1. 验证表单数据
   * 2. 根据editingBlock状态判断新增或编辑
   * 3. 调用API保存数据
   * 4. 成功后关闭弹窗并刷新标签列表
   *
   * 使用场景：用户在标签弹窗中点击"确定"按钮时调用
   */
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
      console.error('Failed to save tag', error)
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
  useEffect(() => {
    loadTags()
  }, [])

  useEffect(() => {
    setLocalSelectedTagIds(selectedTagIds)
  }, [selectedTagIds])

  // ============================================================================
  // // 渲染
  // ============================================================================
  return (
    <div className="tag-manager">
      <div className="tag-header">
        <h4>标签管理</h4>
        <Button type="primary" size="small" icon={<PlusOutlined />} onClick={handleCreateTag}>
          新建标签
        </Button>
      </div>

      <div className="tag-list">
        {tags.map(tag => (
          <div
            key={tag.id}
            className={`tag-item ${localSelectedTagIds.includes(tag.id) ? 'selected' : ''}`}
            onClick={() => toggleTag(tag.id)}
          >
            <span className="tag-dot" style={{ background: tag.color }}></span>
            <span className="tag-name">{tag.name}</span>
            <Dropdown menu={getDropdownItems(tag)} trigger={['click']}>
              <Button
                type="text"
                size="small"
                icon={<MoreOutlined />}
                onClick={e => e.stopPropagation()}
                className="tag-more"
              />
            </Dropdown>
          </div>
        ))}

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
            <div className="color-picker">
              {presetColors.map(color => (
                <div
                  key={color}
                  className={`color-option ${selectedColor === color ? 'selected' : ''}`}
                  style={{ background: color }}
                  onClick={() => {
                    setSelectedColor(color)
                    form.setFieldsValue({ color })
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