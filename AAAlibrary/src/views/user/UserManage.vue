<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElTable, ElTableColumn, ElButton, ElTag, ElAlert, ElSkeleton, ElIcon } from 'element-plus';
import { Delete } from '@element-plus/icons-vue'; // 导入正确的“删除/禁用”图标
import { useTokenStore } from '@/stores/token.js';
import { 
  userInfoGetService, 
  getNormalUsersService, 
  disableUserService ,
  enableUserService
} from '@/api/user.js';

// 路由实例
const router = useRouter();
// Token状态管理
const tokenStore = useTokenStore();

// 状态数据
const currentUser = ref(null); // 当前用户信息
const normalUsers = ref([]);   // 普通用户列表
const loading = ref(false);    // 加载状态
const errorMsg = ref('');      // 错误信息

// 判断是否为管理员
const isAdmin = computed(() => {
  return currentUser.value && currentUser.value.userRole === 'ADMIN';
});

// 获取当前用户信息
const getCurrentUser = async () => {
  try {
    const res = await userInfoGetService();
    currentUser.value = res.data;
    // 管理员加载用户列表
    if (isAdmin.value) {
      loadNormalUsers();
    }
  } catch (err) {
    ElMessage.error('登录状态失效，请重新登录');
    tokenStore.removeToken();
    router.push('/login');
  }
};

// 加载普通用户列表
const loadNormalUsers = async () => {
  loading.value = true;
  errorMsg.value = '';
  try {
    const res = await getNormalUsersService();
    normalUsers.value = res.data;
  } catch (err) {
    errorMsg.value = err.response?.data?.msg || '获取用户列表失败';
    console.error('加载用户列表错误:', err);
  } finally {
    loading.value = false;
  }
};

// 禁用用户
const handleDisable = async (userId) => {
  try {
    const res = await disableUserService(userId); 
    ElMessage.success(res.msg || '用户已禁用');
    loadNormalUsers();
  } catch (err) {
    // 细化错误提示（根据后端返回的msg）
    ElMessage.error(err.response?.data?.msg || '禁用失败，请检查权限或用户状态');
  }
};
// 解禁用户
const handleEnable = async (userId) => {
  try {
    const res = await enableUserService(userId);
    ElMessage.success(res.msg || '用户已解禁');
    loadNormalUsers(); // 刷新列表
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '解禁失败');
  }
};

// 退出登录
const logout = () => {
  tokenStore.removeToken();
  router.push('/login');
};

// 页面初始化
onMounted(() => {
  if (!tokenStore.token) {
    router.push('/login');
    return;
  }
  getCurrentUser();
});
</script>

<template>
  <div class="user-manage-page min-h-screen bg-gray-50">
    <!-- 导航栏 -->
    <nav class="bg-blue-600 text-white shadow-md">
      <div class="container mx-auto px-4 py-3 flex justify-between items-center">
        <div class="text-xl font-bold">用户管理系统</div>
        <div class="flex items-center gap-4" v-if="currentUser">
          <span>{{ currentUser.userName }}</span>
          <ElTag :type="isAdmin ? 'success' : 'info'">
            {{ isAdmin ? '管理员' : '普通用户' }}
          </ElTag>
          <ElButton 
            type="danger" 
            size="small" 
            @click="logout"
            icon="el-icon-logout"
          >
            退出
          </ElButton>
        </div>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="container mx-auto px-4 py-6">
      <div class="mb-6">
        <h2 class="text-2xl font-semibold text-gray-800">用户管理</h2>
        <p class="text-gray-600">管理系统内所有普通用户账户</p>
      </div>

      <!-- 权限提示（非管理员） -->
      <ElAlert 
        v-if="currentUser && !isAdmin"
        title="权限不足"
        message="您没有用户管理权限，请使用管理员账号登录"
        type="warning"
        show-icon
        class="mb-6"
      />

      <!-- 管理员功能区 -->
      <div v-if="isAdmin">
        <ElCard shadow="hover">
          <template #header>
            <div class="flex justify-between items-center">
              <span>普通用户列表</span>
              <span class="text-gray-500">
                共 {{ normalUsers.length }} 位用户
              </span>
            </div>
          </template>

          <!-- 加载状态 -->
          <ElSkeleton v-if="loading" :rows="6" animated class="my-4" />

          <!-- 错误提示 -->
          <ElAlert 
            v-if="errorMsg && !loading"
            :message="errorMsg"
            type="error"
            show-icon
            class="my-4"
          />

          <!-- 用户表格 -->
          <ElTable 
            v-if="!loading && normalUsers.length"
            :data="normalUsers"
            border
            stripe
            style="width: 100%"
          >
            <ElTableColumn 
              prop="userId" 
              label="用户ID" 
              width="80" 
              align="center"
            />
            <ElTableColumn 
              prop="userName" 
              label="用户名" 
              align="center"
            />
            <ElTableColumn 
              prop="userEmail" 
              label="邮箱" 
              align="center"
            />
            <ElTableColumn 
              prop="userRole" 
              label="角色" 
              width="100" 
              align="center"
            >
              <template #default="scope">
                <ElTag type="info" size="small">
                  {{ scope.row.userRole === 'user' ? '普通用户' : scope.row.userRole }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn 
              prop="userStatus" 
              label="状态" 
              width="100" 
              align="center"
            >
              <template #default="scope">
                <ElTag 
                  :type="scope.row.userStatus === '0' ? 'success' : 'danger'"
                  size="small"
                >
                  {{ scope.row.userStatus === '0' ? '正常' : '已禁用' }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="200" align="center">
              <template #default="scope">
                <!-- 禁用按钮：仅对正常状态用户显示 -->
                <ElButton 
                  v-if="scope.row.userStatus === '0'"
                  type="danger"
                  size="mini"
                  @click="handleDisable(scope.row.userId)"
                  icon="Delete"
                >
                  禁用
                </ElButton>
                
                <!-- 解禁按钮：仅对禁用状态用户显示 -->
                <ElButton 
                  v-if="scope.row.userStatus === '1'"
                  type="success"
                  size="mini"
                  @click="handleEnable(scope.row.userId)"
                  icon="Check"
                >
                  解禁
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>

          <!-- 空数据提示 -->
          <div 
            v-if="!loading && !normalUsers.length && !errorMsg"
            class="py-12 text-center text-gray-500"
          >
            <i class="el-icon-user-off text-4xl mb-3"></i>
            <p>暂无普通用户数据</p>
          </div>
        </ElCard>
      </div>
    </div>
  </div>
</template>

<style lang="scss"> // 或 style（根据您的实际语法）
.user-manage-page {
  .el-card__header {
    padding: 12px 20px;
  }
  .el-table {
    margin-top: 15px;
  }
}
</style>