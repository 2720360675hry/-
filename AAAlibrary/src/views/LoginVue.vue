<script setup>
import { User, Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
//引入token
import { useTokenStore } from '@/stores/token.js'
// 创建token存储对象
const tokenStore = useTokenStore()
//导入路由
import router from '@/router';
// 导入loginService 方法
import { loginService } from '@/api/user'
import { onMounted } from 'vue';


// 引入热门图书相关
import { getBookStatistics } from '@/api/static' // 根据你的实际路径调整
import { bookStatisticsStore } from '@/stores/bookStatistics'

// 创建热门图书仓库实例
const bookStore = bookStatisticsStore()

// 用于存储热门图书数据
const hotBooks = ref([])

// 获取热门图书数据
const fetchHotBooks = async () => {
  try {
    // 如果本地仓库已有数据，直接使用
    if (bookStore.bookList && bookStore.bookList.length > 0) {
      hotBooks.value = bookStore.bookList
      return
    }

    // 否则从后端获取
    const result = await getBookStatistics()
    if (result && result.data) {
      hotBooks.value = result.data
      // 存储到本地仓库
      bookStore.setBookStatistics(result.data)
    }
  } catch (error) {
    console.error('获取热门图书数据失败：', error)
    ElMessage.error('获取热门图书数据失败')
  }
}
// 组件挂载后，主动获取热门图书数据
onMounted(() => {
  fetchHotBooks();
});


//用于注册的数据模型
//这是我的前端文本框输入的注册数据对象
const registerData = ref({
    userId:0,
    userName: '',
    userEmail: '',
    userPassword: '',
    userPassword: '',
    userRole:'',
    userStatus:''
})
//登录表单数据
const loginForm = ref({
    userId:0,
    userName: '',
    userEmail: '',
    userPassword: '',
    userPassword: '',
    userRole:'',
    userStatus:''
})

const isRegister = ref(true)

//1.调用user.js中的那个注册函数
import { registerService } from '@/api/user'

// 注册接口 此处补充代码 向后台发送注册请求
const register =async () => {
    const result = await registerService(registerData.value)
    ElMessage.success(result.msg?result.msg:'注册成功')   
    //转到登陆页面
    isRegister.value = false
}

//登录接口 此处补充代码 向后台发送注册请求
// const login = async () => {
//   console.log("向后端发送登录请求")
  
//   const result = await loginService(loginForm.value)
//   // 登录成功，需要保持登录状态 保存用户的令牌
//   tokenStore.setToken(result.data)

//   ElMessage.success(result.msg?result.msg: "登录成功")
//   // 跳转到图书页面
//   router.push('/')
// }
const login = async () => {
    let result = await loginService(loginForm.value)
    //保存token
    console.log("结果是" + result);
    tokenStore.setToken(result.data)
    //  保存用户登录信息
    ElMessage.success(result.msg ? result.msg : '登录成功')
    router.push('/')
}

//自定义确认密码的校验函数
const rePasswordValid = (rule, value, callback) => {
    if (value == null || value === '') {
        return callback(new Error('请再次确认密码'))
    }
    // 注意此处获取registerData的值需要加value
    if (registerData.value.userPassword !== value) {
        return callback(new Error('两次输入密码不一致'))
    } else {
        callback()
    }
}
//用于注册的表单校验模型
const registerDataRules = ref({
    userName: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 5, max: 16, message: '用户名的长度必须为5~16位', trigger: 'blur' }
    ],
    userPassword: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 5, max: 16, message: '密码长度必须为5~16位', trigger: 'blur' }
    ],
    rePassword: [
        { validator: rePasswordValid, trigger: 'blur' }
    ]
})

//定义函数，清空数据模型的数据
const clearData = () => {
    registerData.value.userName = ''
    registerData.value.userEmail = ''
    registerData.value.userPassword = ''
    registerData.value.rePassword = ''
}
</script>

<template>
    <el-row class="login-page">
        <el-col :span="12" class="bg">
            <!-- 左侧热门图书 -->
            <div class="hot-book-rank">
                <h2>热门图书排行榜</h2>
                <div v-if="hotBooks.length > 0" class="book-list">
                    <div 
                v-for="(book, index) in hotBooks" 
                :key="index" 
                class="book-item"
                >
                <span class="rank">{{ index + 1 }}</span>
                <span class="name">{{ book.bookName }}</span>
                <span class="count">{{ book.borrowCount }} 借阅</span>
                </div>
                </div>
                <div v-else>暂无热门图书数据</div>
            </div>

        </el-col>
        <el-col :span="6" :offset="3" class="form">
            <!-- 注册表单 -->
            <el-form :rules="registerDataRules" ref="form" size="large" autocomplete="off" v-if="isRegister"
                :model="registerData">
                <el-form-item>
                    <h1>注册</h1>
                </el-form-item>
                <el-form-item prop="userName">
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.userName"></el-input>
                </el-form-item>
                <el-form-item prop="userEmail">

                    <el-input :prefix-icon="User" placeholder="请输入邮箱" v-model="registerData.userEmail"></el-input>
                </el-form-item>
                <el-form-item prop="userPassword">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入密码"
                        v-model="registerData.userPassword"></el-input>
                </el-form-item>
                <el-form-item prop="rePassword">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入再次密码"
                        v-model="registerData.rePassword"></el-input>
                </el-form-item>
                <!-- 注册按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" @click="register" auto-insert-space>
                        注册
                    </el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = false; clearData()">
                        ← 返回
                    </el-link>
                </el-form-item>
            </el-form>
            <!-- 登录表单 -->
            <el-form :rules="registerDataRules" :model="loginForm" ref="form" size="large" autocomplete="off" v-else>
                <el-form-item>
                    <h1>登录</h1>
                </el-form-item> 

                <el-form-item prop="userEmail">
                    <el-input v-model="loginForm.userEmail" :prefix-icon="User" placeholder="请输入邮箱"></el-input>
                </el-form-item>

                <el-form-item prop="userPassword">
                    <el-input v-model="loginForm.userPassword" name="userPassword" :prefix-icon="Lock" type="password"
                        placeholder="请输入密码"></el-input>
                </el-form-item>

                <el-form-item class="flex">
                    <div class="flex">
                        <el-checkbox>记住我</el-checkbox>
                        <el-link type="primary" :underline="false">忘记密码？</el-link>
                    </div>
                </el-form-item>
                <!-- 登录按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" @click="login" auto-insert-space>登录</el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = true; clearData()">
                        注册 →
                    </el-link>
                </el-form-item>
            </el-form>
        </el-col>
    </el-row>
</template>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  background-color: #fff;

  .bg {
    // background: url('@/assets/logo_bg.png') no-repeat center / cover;
    border-radius: 0 20px 20px 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .hot-book-rank {
    background-color: rgba(255, 255, 255, 0.9);
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    width: 80%;
  }

  .hot-book-rank h2 {
    text-align: center;
    margin-bottom: 20px;
    font-size: 20px;
  }

  .book-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .book-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    border-bottom: 1px solid #eee;
  }

  .rank {
    font-weight: bold;
    color: #409eff;
    margin-right: 10px;
  }

  .name {
    flex: 1;
  }

  .count {
    color: #666;
  }

  .form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    user-select: none;

    .title {
      margin: 0 auto;
    }

    .button {
      width: 100%;
    }

    .flex {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }
}
</style>

