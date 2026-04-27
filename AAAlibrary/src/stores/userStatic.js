import { defineStore } from "pinia";
import { ref } from 'vue';

// 存放令牌

export const userStaticStore = defineStore('userStatic', () => {
    //1.定义热门用户信息
    const userList = ref('')

    //2.定义userList的方法
    const setUserList = (newUserList) => {
        userList.value = newUserList
    }

    //3.定义移除
    const removeUserList = () => {
        userList.value = ''
    }
    return {
        userList,setUserList,removeUserList
    }
},
    //参数持久化
    {
        persist: true
    }
)