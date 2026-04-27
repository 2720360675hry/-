// 导入request.js请求工具
import request from '@/utils/request.js'

//图书列表
export const bookListService = () => {
  return request.get('/sys/book/allBook')
}

//添加图书
// 图书新增服务
export const bookAddService = (bookData) => {
  return request.post('/sys/book/addBook', bookData)
}

// 删除图书
export const bookDeleteService = (bookId) => {
  return request.delete('/sys/book/deleteBook', { params: { bookId: bookId } })
}

// 修改图书
export const bookUpdateService = (bookData) => {
  return request.put('/sys/book/updateBook', bookData)
}

// 分页查询的方法
// 分页获取图书列表（支持搜索）
export const bookListPageService = (params) => {
  return request.get('/sys/book/pagelist', {
    params: params
  })
}

//借阅图书 发送get请求
export const bookBorrowService = (bookId) => {
  return request.get('/sys/book/borrowBook', { params: { bookId: bookId } })
}

// 用户当前借阅的图书
export const userBorrowListService = () => {
  return request.get('/sys/book/currentBorrowedBooks')
}

// 归还图书 发送get请求
export const bookReturnService = (bookId) => {
  return request.get('/sys/book/returnBook', { params: { bookId: bookId } })
}

//图书搜索接口（按状态搜索）
export const bookSearchService = (state) => {
  return request.get('/sys/book/search', {
    params: { state } // 传递状态参数
  })
}