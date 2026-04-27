<script setup>
import { ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";

// 默认显示第一个tab
const activeTab = ref('current')

// 响应式变量：存储当前借阅和历史借阅数据
const currentBorrowedBooks = ref([])
const pastBorrowedRecords = ref([])

// 导入接口函数
import { userBorrowListService } from '@/api/book'
import { pastBorrowedRecordsService } from '@/api/record'
import { bookReturnService } from '@/api/book'

// 获取用户当前借阅的图书
const getUsersCurrentBorrowedBooks = async () => {
  try {
    const result = await userBorrowListService()
    currentBorrowedBooks.value = result.data || []
  } catch (error) {
    ElMessage.error('获取当前借阅图书失败')
    currentBorrowedBooks.value = []
  }
}

// 获取用户曾经的借阅记录
const getUsersPastBorrowedRecords = async () => {
  try {
    const result = await pastBorrowedRecordsService()
    pastBorrowedRecords.value = result.data || []
  } catch (error) {
    ElMessage.error('获取历史借阅记录失败')
    pastBorrowedRecords.value = []
  }
}

// 初始化加载数据
getUsersCurrentBorrowedBooks()
getUsersPastBorrowedRecords()

// 归还图书
const returnBook = async (book) => {
  await ElMessageBox.confirm(
    `确定要归还《${book.bookName}》吗？`,
    '确认归还',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await bookReturnService(book.bookId)
    ElMessage.success('图书已成功归还')
    // 刷新数据
    getUsersCurrentBorrowedBooks()
    getUsersPastBorrowedRecords()
  } catch (error) {
    ElMessage.error('归还失败：' + (error.response?.data?.msg || '系统异常'))
  }
}

// 处理tab点击（可选，若无额外逻辑可省略）
const handleTabClick = () => {
  // 可根据需求添加tab切换时的逻辑
}
</script>

<template>
  <el-card class="page-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="当前借阅" name="current">
        <el-table :data="currentBorrowedBooks" style="width: 100%">
          <el-table-column prop="bookName" label="书名" />
          <el-table-column prop="bookAuthor" label="作者" />
          <el-table-column prop="bookPress" label="出版社" />
          <el-table-column prop="bookBorrowtime" label="借阅时间" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="danger" @click="returnBook(row)">归还</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="借阅记录" name="past">
        <el-table
          :data="pastBorrowedRecords"
          style="width: 100%"
          :default-sort="{ prop: 'recordRemandtime', order: 'ascending' }"
        >
          <el-table-column prop="recordBookname" label="书名" />
          <el-table-column prop="recordBookisbn" label="ISBN" />
          <el-table-column prop="recordBorrower" label="借阅人" />
          <el-table-column prop="recordBorrowtime" label="借阅时间" sortable />
          <el-table-column prop="recordRemandtime" label="归还时间" sortable />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped>
.page-container {
  padding: 20px;
}
</style>