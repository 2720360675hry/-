<script setup>
import {
    Management,
    Promotion,
    UserFilled,
    User,
    Crop,
    EditPen,
    SwitchButton,
    CaretBottom
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'
 
// 导入ref
import { ref, onMounted } from 'vue'  // 添加 onMounted
//导入接口函数
import { userInfoGetService } from '@/api/user.js'
//导入pinia
import { userInfoStore } from '@/stores/userInfo.js'
const useruserInfoStore = userInfoStore();
 
import { useTokenStore } from '@/stores/token.js'
const tokenStore = useTokenStore();

// 添加头像URL的响应式引用
const avatarUrl = ref('')

//获取个人信息
const getUserInf = async () => {
    let result = await userInfoGetService();
    //存储pinia
    useruserInfoStore.setInfo(result.data);
    // 设置头像URL - 修改点：从用户信息中获取头像URL
    avatarUrl.value = useruserInfoStore.info.avatarUrl || '@/assets/touxiang.jpg'
    console.log("是管理员吗？"+useruserInfoStore.isAdmin);
}

// 组件挂载后执行getUserInf
onMounted(() => {
  getUserInf()
})

// 添加头像更新处理函数 - 修改点：新增函数
// 头像更新处理函数
const handleAvatarUpdated = (newAvatarUrl) => {
  console.log('收到头像更新事件:', newAvatarUrl)
  
  // 如果URL是相对路径，构建完整URL
  let finalUrl = newAvatarUrl
  if (newAvatarUrl.startsWith('/api/files/')) {
    finalUrl = 'http://localhost:8080' + newAvatarUrl
  }
  
  avatarUrl.value = finalUrl
  // 同时更新pinia中的头像信息
  useruserInfoStore.info.avatarUrl = finalUrl
  console.log('更新后的头像URL:', finalUrl)
  ElMessage.success('头像更新成功！')
}
 
// 导入路由
import router from '@/router'
import { ElMessage, ElMessageBox } from 'element-plus'
// 导入退出登录的接口
// import { logoutService } from '@/api/user.js'
//条目被点击后，调用的函数
const handleCommand = (command) => {
    if (command == 'logout') {
        //退出登录
        ElMessageBox.confirm(
            '你确认退出登录码？',
            '温馨提示',
            {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )
            .then(async () => {
                //用户点击了确认
                tokenStore.token = ''
                //跳转到登录页
                router.push('/login')
            })
            .catch(() => {
                //用户点击了取消
                ElMessage({
                    type: 'info',
                    message: '取消退出',
                })
            })
    } else {
        router.push(`/user/${command}`);
    }
}

</script>
 
<template>
    <el-container class="layout-container">
 
<!-- 左侧菜单 -->
<el-aside width="200px">
            <div class="el-aside__logo"></div>
            <el-menu active-text-color="#ffd04b" background-color="#232323" text-color="#fff" router>
                <el-menu-item index="/book/resources">
                    <el-icon>
                        <Book />
                    </el-icon>
                    <span>图书资源</span>
                </el-menu-item>
                <!-- 如果是管理员就显示用户管理组件 否则就显示借阅记录组件 -->
                <el-menu-item v-if="!useruserInfoStore.isAdmin" index="/book/records">
                    <el-icon>
                        <User />
                    </el-icon>
                    <span>借阅记录</span>
                </el-menu-item>
                <el-menu-item v-else index="/user/manage">
                    <el-icon>
                        <Document />
                    </el-icon>
                    <span>用户管理</span>
                </el-menu-item>
                <el-sub-menu>
                    <template #title>
                        <el-icon>
                            <UserFilled />
                        </el-icon>
                        <span>个人中心</span>
                    </template>
                    <el-menu-item index="/user/info">
                        <el-icon>
                            <User />
                        </el-icon>
                        <span>基本资料</span>
                    </el-menu-item>
                    <el-menu-item index="/user/avatar">
                        <el-icon>
                            <Crop />
                        </el-icon>
                        <span>更换头像</span>
                    </el-menu-item>
                    <el-menu-item index="/user/password">
                        <el-icon>
                            <EditPen />
                        </el-icon>
                        <span>重置密码</span>
                    </el-menu-item>
                </el-sub-menu>
            </el-menu>
        </el-aside>

        <!-- 右侧主区域 -->
        <el-container>
            <!-- 头部区域 -->
            <el-header>
                <div>当前用户：<strong>{{ useruserInfoStore.info.userName }}</strong></div>
                <el-dropdown placement="bottom-end" @command="handleCommand">
                    <span class="el-dropdown__box">
                        <el-avatar :src="avatarUrl" />
                        <el-icon>
                            <CaretBottom />
                        </el-icon>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="info" :icon="User">基本资料</el-dropdown-item>
                            <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
                            <el-dropdown-item command="password" :icon="EditPen">重置密码</el-dropdown-item>
                            <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-header>
            <!-- 中间区域 -->
            <el-main>
                <!-- 修改点：监听头像更新事件 -->
                <router-view @avatar-updated="handleAvatarUpdated"></router-view>
            </el-main>
            <!-- 底部区域 -->
            <el-footer>软件之声论坛 ©2024 Created by xumj</el-footer>
        </el-container>
    </el-container>
</template>
 
<style lang="scss" scoped>
.layout-container {
    height: 100vh;
 
    .el-aside {
        background-color: #232323;
 
        &__logo {
            height: 120px;
            background: url('@/assets/haimian.png') no-repeat center / 120px auto;
        }
 
        .el-menu {
            border-right: none;
        }
    }
 
    .el-header {
        background-color: #fff;
        display: flex;
        align-items: center;
        justify-content: space-between;
 
        .el-dropdown__box {
            display: flex;
            align-items: center;
 
            .el-icon {
                color: #999;
                margin-left: 10px;
            }
 
            &:active,
            &:focus {
                outline: none;
            }
        }
    }
 
    .el-footer {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: #666;
    }
}
</style>
