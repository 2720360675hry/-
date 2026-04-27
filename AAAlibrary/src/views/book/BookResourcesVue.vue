<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete, Check, Close } from '@element-plus/icons-vue'
 
import { userInfoStore } from '@/stores/userInfo.js'
const useruserInfoStore = userInfoStore();
 
// 图书表单数据模型
const bookModel = ref({
    bookId: '',
    bookName: '',
    bookIsbn: '',
    bookPress: '',
    bookAuthor: '',
    bookPagination: '',
    bookPrice: '',
    bookUploadtime: '',
    bookStatus: '',
    bookBorrower: '',
    bookBorrowtime: '',
    bookReturntime: ''
})
 
// 表单引用
const formRef = ref(null)
 
// 用户搜索时选中的状态
const state = ref('全部')

// 搜索关键词
const searchKeyword = ref('')
 
// 图书列表数据模型
const books = reactive({
  records: [],
  total: 0,
  current: 1,
  size: 5
})

// 状态映射
const statusMap = {
  '可借阅': '0',
  '已借出': '1',
  '0': '可借阅',
  '1': '已借出'
}

// 格式化状态显示
const formatStatus = (status) => {
  return statusMap[status] || status
}
 
// 表单验证规则
const rules = {
    bookName: [{ required: true, message: '请输入书名', trigger: 'blur' }],
    bookIsbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
    bookPress: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
    bookAuthor: [{ required: true, message: '请输入作者', trigger: 'blur' }],
    bookPagination: [
        { required: true, message: '请输入页数', trigger: 'blur' },
        { type: 'number', message: '页数必须是数字', trigger: 'blur' }
    ],
    bookPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}
 
// 控制抽屉是否显示
const visibleDrawer = ref(false)
 
// 导入 token
import { useTokenStore } from '@/stores/token.js'
const tokenStore = useTokenStore()

// 导入bookAddService
import { bookAddService } from '@/api/book.js'
// 添加图书
const addBook = async () => {
    if (!formRef.value) return;
    await formRef.value.validate((valid, fields) => {
        if (valid) {
            bookAddService(bookModel.value).then(result => {
                ElMessage.success(result.message ? result.message : '添加成功')
                // 刷新列表
                getBooks()
                visibleDrawer.value = false
                // 重置表单
                Object.keys(bookModel.value).forEach(key => {
                  bookModel.value[key] = ''
                })
            }).catch(error => {
                ElMessage.error(error.message || '添加失败')
            })
        } else {
            console.log('Validation failed', fields)
            ElMessage.error('请检查输入信息')
        }
    })
}

// 导入借阅图书的接口
import { bookBorrowService } from '@/api/book.js'
const borrowBook = async (bookId) => {
    await ElMessageBox.confirm('确定要借阅吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        bookBorrowService(bookId).then(result => {
            ElMessage.success(result.message ? result.message : '借阅成功')
            getBooks()
        }).catch(error => {
            ElMessage.error(error.message || '借阅失败')
        })
    }).catch(() => {
        ElMessage.info('已取消借阅')
    })
}

// 调用图书删除接口
import { bookDeleteService } from '@/api/book.js'
import { ElMessageBox } from 'element-plus'
const deleteBook = (bookId) => {
    ElMessageBox.confirm('确定要删除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        bookDeleteService(bookId).then(result => {
            ElMessage.success(result.message ? result.message : '删除成功')
            getBooks()
        }).catch(error => {
            ElMessage.error(error.message || '删除失败')
        })
    }).catch(() => {
        ElMessage.info('已取消删除')
    });
}
 
// 当前编辑的图书ID
const editingBookId = ref(null)
 
const isEditing = (row) => row.bookId === editingBookId.value
  
const startEdit = (bookId) => {
    editingBookId.value = bookId
}
  
const cancelEdit = () => {
    editingBookId.value = null
    getBooks()
}
  
import { bookUpdateService } from '@/api/book.js'
const updateBook = (book) => {
    bookUpdateService(book).then(result => {
        ElMessage.success(result.message ? result.message : '更新成功')
        getBooks()
        editingBookId.value = null
    }).catch(error => {
        ElMessage.error(error.message || '更新失败')
    })
}

// 当前页码currentPage
const currentPage = ref(1)
// 每页显示的记录数
const pageSize = ref(10)

import { bookListPageService } from '@/api/book.js'

// 获取图书列表（支持搜索）
const getBooks = async () => {
  try {
    console.log('开始获取图书列表，页码:', currentPage.value, '页大小:', pageSize.value, '关键词:', searchKeyword.value, '状态:', state.value)
    
    // 构建搜索参数
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加搜索参数
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    // 添加状态参数
    if (state.value && state.value !== '全部') {
      if (state.value === '可借阅') {
        params.state = '0'
      } else if (state.value === '已借出') {
        params.state = '1'
      } else {
        params.state = state.value
      }
    }
    
    console.log('请求参数:', params)
    const res = await bookListPageService(params)
    console.log('接口返回数据:', res)
    
    if (res.code === 0 || res.code === 200) {
      if (res.data && res.data.records) {
        books.records = res.data.records
        books.total = res.data.total || 0
        books.current = res.data.current || 1
        books.size = res.data.size || pageSize.value
      } else {
        books.records = res.data || []
        books.total = res.total || books.records.length
      }
      console.log('更新后的books数据:', books)
    } else {
      ElMessage.error(res.message || '获取图书列表失败')
    }
  } catch (error) {
    console.error('获取图书列表失败:', error)
    ElMessage.error('获取图书列表失败: ' + (error.message || '未知错误'))
  }
}

// 初始化获取数据
getBooks()

const handleCurrentChange = (val) => {
  console.log('页码变化:', val)
  currentPage.value = val
  getBooks()
}

const handleSizeChange = (val) => {
  console.log('页大小变化:', val)
  pageSize.value = val
  currentPage.value = 1
  getBooks()
}

import { watch } from 'vue'

watch([currentPage, pageSize], () => {
  console.log('监听器触发，重新获取数据')
  getBooks()
})

// 搜索图书
const searchBooks = () => {
  console.log('搜索关键词:', searchKeyword.value, '状态:', state.value)
  currentPage.value = 1
  getBooks()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  state.value = '全部'
  currentPage.value = 1
  getBooks()
}

</script>

<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span v-if="useruserInfoStore.isAdmin">
                    <el-button type="primary" @click="visibleDrawer = true">添加图书</el-button>
                </span>
            </div>
        </template>
        <!-- 搜索表单 -->
        <el-form inline>
            <el-form-item label="关键词：">
                <el-input 
                    v-model="searchKeyword" 
                    placeholder="请输入书名、作者或出版社" 
                    style="width: 200px"
                    clearable
                    @keyup.enter="searchBooks"
                ></el-input>
            </el-form-item>
            <!-- <el-form-item label="状态：">
                <el-select placeholder="请选择" v-model="state">
                    <el-option label="全部" value="全部"></el-option>
                    <el-option label="可借阅" value="可借阅"></el-option>
                    <el-option label="已借出" value="已借出"></el-option>
                </el-select>
            </el-form-item> -->
            <el-form-item>
                <el-button type="primary" @click="searchBooks">搜索</el-button>
                <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
        </el-form>
        <!-- 图书列表 -->
        <el-table :data="books.records" style="width: 100%" :default-sort="{ prop: 'bookName', order: 'descending' }">
            <el-table-column label="书名" sortable prop="bookName">
                <template #default="{ row }">
                    <el-input v-if="isEditing(row)" v-model="row.bookName" />
                    <span v-else>{{ row.bookName }}</span>
                </template>
            </el-table-column>
            <el-table-column label="ISBN" prop="bookIsbn">
                <template #default="{ row }">
                    <el-input v-if="isEditing(row)" v-model="row.bookIsbn" />
                    <span v-else>{{ row.bookIsbn }}</span>
                </template>
            </el-table-column>
            <el-table-column label="出版社" prop="bookPress">
                <template #default="{ row }">
                    <el-input v-if="isEditing(row)" v-model="row.bookPress" />
                    <span v-else>{{ row.bookPress }}</span>
                </template>
            </el-table-column>
            <el-table-column label="作者" prop="bookAuthor">
                <template #default="{ row }">
                    <el-input v-if="isEditing(row)" v-model="row.bookAuthor" />
                    <span v-else>{{ row.bookAuthor }}</span>
                </template>
            </el-table-column>
            <el-table-column label="页数" prop="bookPagination">
                <template #default="{ row }">
                    <el-input v-if="isEditing(row)" v-model.number="row.bookPagination" />
                    <span v-else>{{ row.bookPagination }}</span>
                </template>
            </el-table-column>
            <el-table-column label="价格" prop="bookPrice">
                <template #default="{ row }">
                    <el-input v-if="isEditing(row)" v-model="row.bookPrice" />
                    <span v-else>{{ row.bookPrice }}</span>
                </template>
            </el-table-column>
            <el-table-column label="上传时间" prop="bookUploadtime"></el-table-column>
            <el-table-column label="状态" prop="bookStatus" sortable>
                <template #default="{ row }">
                    {{ formatStatus(row.bookStatus) }}
                </template>
            </el-table-column>
            <el-table-column label="借阅人" prop="bookBorrower"></el-table-column>
            <el-table-column label="借阅时间" prop="bookBorrowtime"></el-table-column>
            <el-table-column label="归还时间" prop="bookReturntime"></el-table-column>
            <el-table-column v-if="useruserInfoStore.isAdmin" label="操作" width="150">
                <template #default="{ row }">
                    <el-button v-if="!isEditing(row)" :icon="Edit" circle plain type="primary"
                        @click="startEdit(row.bookId)"></el-button>
                    <el-button v-else :icon="Check" circle plain type="success" @click="updateBook(row)"></el-button>
                    <el-button v-if="isEditing(row)" :icon="Close" circle plain type="danger"
                        @click="cancelEdit()"></el-button>
                    <el-button v-if="!isEditing(row)" :icon="Delete" circle plain type="danger"
                        @click="deleteBook(row.bookId)"></el-button>
                </template>
            </el-table-column>
            <el-table-column v-else label="操作" width="100">
                <template #default="{ row }">
                    <el-button v-if="row.bookStatus == 0" plain type="primary"
                        @click="borrowBook(row.bookId)">借阅</el-button>
                    <el-button v-else plain disabled type="primary">已借出</el-button>
                </template>
            </el-table-column>
            <template #empty>
                <el-empty description="没有数据" />
            </template>
        </el-table>

        <!-- 分页 -->
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 15, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="books.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
        
        <!-- 抽屉 -->
        <el-drawer v-model="visibleDrawer" title="添加图书" direction="rtl" size="50%">
            <el-form :model="bookModel" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="书名" prop="bookName">
                    <el-input v-model="bookModel.bookName" placeholder="请输入书名"></el-input>
                </el-form-item>
                <el-form-item label="ISBN" prop="bookIsbn">
                    <el-input v-model="bookModel.bookIsbn" placeholder="请输入ISBN"></el-input>
                </el-form-item>
                <el-form-item label="出版社" prop="bookPress">
                    <el-input v-model="bookModel.bookPress" placeholder="请输入出版社"></el-input>
                </el-form-item>
                <el-form-item label="作者" prop="bookAuthor">
                    <el-input v-model="bookModel.bookAuthor" placeholder="请输入作者"></el-input>
                </el-form-item>
                <el-form-item label="页数" prop="bookPagination">
                    <el-input v-model.number="bookModel.bookPagination" placeholder="请输入页数"></el-input>
                </el-form-item>
                <el-form-item label="价格" prop="bookPrice">
                    <el-input v-model="bookModel.bookPrice" placeholder="请输入价格"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="addBook">保存</el-button>
                    <el-button @click="visibleDrawer = false">取消</el-button>
                </el-form-item>
            </el-form>
        </el-drawer>
    </el-card>
</template>
 
<style scoped>
/* 保持原有样式 */
</style>