import { defineStore } from "pinia";
import { ref } from 'vue';

/*
  前端用来存储热门图书的仓库
*/
export const bookStatisticsStore = defineStore('bookStatistics', () => {
  //1.bookStatistics
  const bookList = ref([])

  //2.定义修改的方法
  const setBookStatistics = (newBookStatistics) => {
    bookList.value = newBookStatistics
  }

  //3.定义移除方法
  const removeBookStatistics = () => {
    bookList.value = []
  }

  return {
    bookList, setBookStatistics, removeBookStatistics
  }
},
{
  //参数持久化
  persist: true
})