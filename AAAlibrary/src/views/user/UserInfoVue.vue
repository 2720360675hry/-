<template>
  <div class="user-info-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>用户基本资料</h1>
      <p>查看和管理您的账户信息</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 用户信息卡片 -->
    <div v-else class="user-card">
      <div class="card-header">
        <h2>基本信息</h2>
        <el-button 
          type="primary" 
          size="small" 
          @click="editUserInfo"
          v-if="!isEditing"
        >
          编辑信息
        </el-button>
        <div v-else>
          <el-button type="success" size="small" @click="saveUserInfo">保存</el-button>
          <el-button size="small" @click="cancelEdit">取消</el-button>
        </div>
      </div>

      <div class="user-details">
        <!-- 用户信息表单 -->
        <div class="info-section">
          <el-form 
            :model="userInfo" 
            :rules="rules" 
            ref="userForm" 
            label-width="100px"
            :disabled="!isEditing"
          >
            <el-form-item label="用户ID">
              <span>{{ userInfo.userId || '无' }}</span>
            </el-form-item>
            
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="userInfo.userName" placeholder="请输入用户名"></el-input>
            </el-form-item>
            
            <el-form-item label="邮箱" prop="userEmail">
              <el-input v-model="userInfo.userEmail" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            
            <el-form-item label="角色">
              <el-tag type="primary">
                {{ formatRole(userInfo.userRole) }}
              </el-tag>
            </el-form-item>
            
            <el-form-item label="账户状态">
              <el-tag :type="userInfo.userStatus === '1' ? 'success' : 'danger'">
                {{ formatStatus(userInfo.userStatus) }}
              </el-tag>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { userInfoGetService, updateUserInfoService } from '@/api/user'

// 默认头像
const DEFAULT_AVATAR = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

export default {
  name: 'UserInfoVue',
  data() {
    return {
      userInfo: {
        userId: '',
        userName: '',
        userEmail: '',
        phone: '',
        userRole: '',
        userStatus: '',
        avatarUrl: ''
      },
      isEditing: false,
      loading: true,
      defaultAvatar: DEFAULT_AVATAR,
      originalUserInfo: {},
      rules: {
        userName: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ]
        // 移除了邮箱验证规则
      }
    }
  },
  mounted() {
    this.getUserInfo()
  },
  methods: {
    // 获取用户信息
    async getUserInfo() {
      try {
        this.loading = true
        console.log('开始获取用户信息...')
        
        const response = await userInfoGetService()
        console.log('API完整响应:', response)
        console.log('响应数据:', response.data)
        
        // 直接使用API返回的数据结构
        if (response && response.data) {
          this.userInfo = {
            userId: response.data.userId || '',
            userName: response.data.userName || '',
            userEmail: response.data.userEmail || '',
            phone: response.data.phone || '',
            userRole: response.data.userRole || 'user',
            userStatus: response.data.userStatus || '0',
            avatarUrl: response.data.avatarUrl || ''
          }
          
          console.log('设置后的用户信息:', this.userInfo)
          this.originalUserInfo = { ...this.userInfo }
        } else {
          console.error('响应数据为空')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        console.error('错误详情:', error.response || error.message)
        this.$message.error('获取用户信息失败')
      } finally {
        this.loading = false
      }
    },

    // 保存用户信息
    async saveUserInfo() {
      this.$refs.userForm.validate(async (valid) => {
        if (valid) {
          try {
            this.loading = true
            
            // 调用实际的更新API
            const response = await updateUserInfoService({
              userId: this.userInfo.userId,
              userName: this.userInfo.userName,
              userEmail: this.userInfo.userEmail,
              phone: this.userInfo.phone
              // 只传递需要更新的字段
            })
            
            console.log('更新响应:', response)
            
            if (response && response.code === 0) {
              this.$message.success('用户信息更新成功')
              this.isEditing = false
              this.originalUserInfo = { ...this.userInfo }
              
              // 可选：重新获取用户信息确保数据同步
              // await this.getUserInfo()
            } else {
              this.$message.error(response.message || '更新失败')
            }
          } catch (error) {
            console.error('更新用户信息失败:', error)
            this.$message.error('更新用户信息失败：' + (error.message || '网络错误'))
          } finally {
            this.loading = false
          }
        } else {
          this.$message.error('请正确填写表单')
          return false
        }
      })
    },
    
    // 获取完整头像URL
    getFullAvatarUrl(avatarPath) {
      if (!avatarPath) return this.defaultAvatar
      
      // 如果已经是完整URL，直接返回
      if (avatarPath.startsWith('http')) {
        return avatarPath
      }
      
      // 如果是相对路径，根据当前环境构建完整URL
      if (avatarPath.startsWith('/api/')) {
        // 对于 /api/ 开头的路径，直接使用当前域名
        return window.location.origin + avatarPath
      }
      
      // 其他情况返回默认头像
      return this.defaultAvatar
    },
    
    // 编辑用户信息
    editUserInfo() {
      this.isEditing = true
    },
    
    // 取消编辑
    cancelEdit() {
      this.isEditing = false
      this.userInfo = { ...this.originalUserInfo }
      if (this.$refs.userForm) {
        this.$refs.userForm.clearValidate()
      }
    },
    
    // 格式化角色显示
    formatRole(role) {
      const roleMap = {
        'admin': '管理员',
        'user': '普通用户'
      }
      return roleMap[role] || '普通用户'
    },
    
    // 格式化状态显示
    formatStatus(status) {
      return status === '1' ? '正常' : '已禁用'
    }
  }
}
</script>

<style scoped>
.user-info-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  margin-bottom: 10px;
  color: #303133;
}

.page-header p {
  color: #909399;
}

.user-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.user-details {
  display: flex;
  padding: 20px;
}

.info-section {
  flex: 1;
  width: 100%;
}

.loading-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-details {
    flex-direction: column;
  }
  
  .user-info-container {
    padding: 10px;
  }
}

/* 确保表单内容可见 */
:deep(.el-form-item__content) {
  min-height: 32px;
  display: flex;
  align-items: center;
}

:deep(.el-input__inner) {
  color: #606266;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-weight: 500;
}
</style>