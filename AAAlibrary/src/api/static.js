// 1.引入request
import request from '@/utils/request'

// 2.写一个查询用户统计的方法
export const userCountService = () => {
  return request.get('/sys/userStatistics/getUserStatistics')
}
// 3.获取图书排行榜
export const getBookStatistics = () => { //名字改成一致
  return request({
    url: '/sys/bookStatistics/top20', //根据你实际的后端来
    method: 'GET'
  })
}