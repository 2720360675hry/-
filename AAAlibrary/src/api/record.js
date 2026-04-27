// 导入request
import request from '@/utils/request'
 
// 查询用户借阅记录
export const pastBorrowedRecordsService = () => {
    return request.get('/sys/record/pastBorrowedRecords')
}
 