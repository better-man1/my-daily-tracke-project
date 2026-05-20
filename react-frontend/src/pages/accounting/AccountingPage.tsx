import React, { useState, useEffect } from 'react'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { Table, Button, Modal, Form, Input, Select, DatePicker, Space, message, Popconfirm, Card, Row, Col, Statistic } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { accountingApi, type AccountingItem, type CreateAccountingRequest } from '@/api/accounting'

const { Option } = Select

/**
 * AccountingPage — 每日记账页面
 */
const AccountingPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [records, setRecords] = useState<AccountingItem[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<AccountingItem | null>(null)
  const [selectedMonth, setSelectedMonth] = useState<Dayjs>(dayjs())
  const [categories, setCategories] = useState<string[]>([])
  const [statistics, setStatistics] = useState<any>(null)
  const [form] = Form.useForm()
  const [typeFilter, setTypeFilter] = useState<'INCOME' | 'EXPENSE' | undefined>(undefined)

  useEffect(() => {
    fetchRecords()
    fetchCategories()
    fetchStatistics()
  }, [selectedMonth, typeFilter])

  const fetchRecords = async () => {
    try {
      setLoading(true)
      const startDate = selectedMonth.startOf('month').format('YYYY-MM-DD')
      const endDate = selectedMonth.endOf('month').format('YYYY-MM-DD')
      const data = await accountingApi.list({
        startDate,
        endDate,
        type: typeFilter
      })
      setRecords(data.list)
    } catch (error) {
      console.error('获取记账记录失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchCategories = async () => {
    try {
      const data = await accountingApi.getCategories()
      setCategories(data)
    } catch (error) {
      console.error('获取分类失败:', error)
    }
  }

  const fetchStatistics = async () => {
    try {
      const year = selectedMonth.year()
      const month = selectedMonth.month() + 1
      const data = await accountingApi.getMonthlyStatistics(year, month)
      setStatistics(data)
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  const handleAdd = () => {
    setEditingRecord(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record: AccountingItem) => {
    setEditingRecord(record)
    form.setFieldsValue({
      ...record,
      date: dayjs(record.date)
    })
    setModalVisible(true)
  }

  const handleDelete = async (id: number) => {
    try {
      await accountingApi.delete(id)
      message.success('删除成功')
      fetchRecords()
      fetchStatistics()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      const data: CreateAccountingRequest = {
        ...values,
        date: values.date.format('YYYY-MM-DD')
      }

      if (editingRecord) {
        await accountingApi.update(editingRecord.id, data)
        message.success('更新成功')
      } else {
        await accountingApi.create(data)
        message.success('创建成功')
      }

      setModalVisible(false)
      fetchRecords()
      fetchStatistics()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  const columns: ColumnsType<AccountingItem> = [
    {
      title: '日期',
      dataIndex: 'date',
      key: 'date',
      width: 120
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type) => (
        <span style={{ color: type === 'INCOME' ? '#52c41a' : '#ff4d4f' }}>
          {type === 'INCOME' ? '收入' : '支出'}
        </span>
      )
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 120
    },
    {
      title: '金额',
      dataIndex: 'amount',
      key: 'amount',
      width: 120,
      render: (amount, record) => (
        <span style={{ color: record.type === 'INCOME' ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }}>
          {record.type === 'INCOME' ? '+' : '-'}{amount.toFixed(2)}
        </span>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="确定删除？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>每日记账</h2>
        <Space>
          <DatePicker.MonthPicker
            value={selectedMonth}
            onChange={(date) => date && setSelectedMonth(date)}
          />
          <Select
            value={typeFilter}
            onChange={setTypeFilter}
            style={{ width: 120 }}
            allowClear
            placeholder="全部类型"
          >
            <Option value="INCOME">收入</Option>
            <Option value="EXPENSE">支出</Option>
          </Select>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建记录
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      {statistics && (
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={8}>
            <Card>
              <Statistic
                title="总收入"
                value={statistics.totalIncome}
                precision={2}
                prefix="+"
                suffix="元"
                valueStyle={{ color: '#3f8600' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="总支出"
                value={statistics.totalExpense}
                precision={2}
                prefix="-"
                suffix="元"
                valueStyle={{ color: '#cf1322' }}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card>
              <Statistic
                title="结余"
                value={statistics.balance}
                precision={2}
                suffix="元"
                valueStyle={{
                  color: statistics.balance >= 0 ? '#3f8600' : '#cf1322'
                }}
              />
            </Card>
          </Col>
        </Row>
      )}

      <Table
        columns={columns}
        dataSource={records}
        rowKey="id"
        loading={loading}
        pagination={{
          pageSize: 20,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`
        }}
      />

      <Modal
        title={editingRecord ? '编辑记录' : '新建记录'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="type"
            label="类型"
            rules={[{ required: true, message: '请选择类型' }]}
          >
            <Select>
              <Option value="INCOME">收入</Option>
              <Option value="EXPENSE">支出</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="amount"
            label="金额"
            rules={[{ required: true, message: '请输入金额' }]}
          >
            <Input type="number" step="0.01" placeholder="请输入金额" />
          </Form.Item>

          <Form.Item
            name="category"
            label="分类"
            rules={[{ required: true, message: '请选择分类' }]}
          >
            <Select
              showSearch
              placeholder="请选择分类"
              optionFilterProp="children"
            >
              {categories.map(cat => (
                <Option key={cat} value={cat}>{cat}</Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="date"
            label="日期"
            rules={[{ required: true, message: '请选择日期' }]}
            initialValue={dayjs()}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default AccountingPage