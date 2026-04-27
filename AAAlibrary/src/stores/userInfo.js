import { defineStore } from "pinia"
import { ref } from 'vue'
 
export const userInfoStore = defineStore('userInfo', () => {
    //1.定义用户信息
    const info = ref({})
    const isAdmin = ref(false)
    //2.定义修改用户信息的方法
    const setInfo = (newInfo) => {
        info.value = newInfo
        isAdmin.value = newInfo.userRole === 'ADMIN'
    }
    //3.定义清空用户信息的方法
    const removeInfo = () => {
        info.value = {}
    }
 
    return { info, isAdmin, setInfo, removeInfo }
}, {
    persist: true
})