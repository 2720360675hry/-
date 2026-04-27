<template>
  <div class="password-reset-container">
    <div class="password-reset-wrapper">
      <!-- 顶部标题 -->
      <div class="header-section">
        <h2 class="title">修改密码</h2>
        <p class="subtitle">为了您的账户安全，请定期修改密码</p>
      </div>

      <!-- 修改密码表单 -->
      <el-card class="password-form-card" shadow="never">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="120px"
          class="password-form"
          status-icon
        >
          <!-- 原密码 -->
          <el-form-item label="原密码：" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前使用的密码"
              show-password
              clearable
              size="large"
              @keyup.enter="handleSubmit"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 新密码 -->
          <el-form-item label="新密码：" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位字符）"
              show-password
              clearable
              size="large"
              @keyup.enter="handleSubmit"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
            <!-- 密码强度提示 -->
            <div v-if="passwordForm.newPassword" class="password-tips">
              <div class="strength-indicator">
                <div 
                  class="strength-bar" 
                  :class="getPasswordStrengthClass(1)"
                ></div>
                <div 
                  class="strength-bar" 
                  :class="getPasswordStrengthClass(2)"
                ></div>
                <div 
                  class="strength-bar" 
                  :class="getPasswordStrengthClass(3)"
                ></div>
              </div>
              <span class="strength-text">{{ passwordStrengthText }}</span>
            </div>
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item label="确认密码：" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
              clearable
              size="large"
              @keyup.enter="handleSubmit"
            >
              <template #prefix>
                <el-icon><Check /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <!-- 操作按钮 -->
          <el-form-item class="form-actions">
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="handleSubmit"
              class="submit-btn"
              size="large"
            >
              <template #icon>
                <el-icon><Check /></el-icon>
              </template>
              {{ loading ? '修改中...' : '确认修改' }}
            </el-button>
            <el-button 
              @click="handleReset"
              class="reset-btn"
              size="large"
            >
              <template #icon>
                <el-icon><Refresh /></el-icon>
              </template>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 安全提示 -->
      <el-card class="tips-card" shadow="never">
        <template #header>
          <div class="tips-header">
            <el-icon><InfoFilled /></el-icon>
            <span>安全提示</span>
          </div>
        </template>
        <ul class="tips-list">
          <li>密码长度至少6位，建议使用字母、数字和符号组合</li>
          <li>定期修改密码可以提高账户安全性</li>
          <li>请不要使用与其他网站相同的密码</li>
          <li>修改密码后，建议重新登录以确保安全</li>
        </ul>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Key, Check, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { changePassword } from '@/api/user'

// 响应式数据
const loading = ref(false)
const passwordFormRef = ref()

// 表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算密码强度
const passwordStrength = computed(() => {
  const password = passwordForm.newPassword
  if (!password || password.length < 6) return 0
  
  let strength = 0
  if (/[a-z]/.test(password)) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^a-zA-Z0-9]/.test(password)) strength++
  
  return Math.min(strength, 3)
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  const texts = ['', '密码强度：弱', '密码强度：中', '密码强度：强']
  return texts[strength]
})

const getPasswordStrengthClass = (level) => {
  if (passwordStrength.value >= level) {
    const classes = ['', 'weak', 'medium', 'strong']
    return `active ${classes[passwordStrength.value]}`
  }
  return ''
}

// 表单验证规则
const validateOldPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入原密码'))
  } else if (value.trim().length === 0) {
    callback(new Error('原密码不能为空'))
  } else {
    callback()
  }
}

const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (value === passwordForm.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, validator: validateOldPassword, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 提交修改 - 修复消息提示颜色
const handleSubmit = async () => {
  if (!passwordFormRef.value) return
  
  try {
    // 表单验证
    await passwordFormRef.value.validate()
    
    // 确认操作
    await ElMessageBox.confirm(
      '确定要修改密码吗？修改成功后建议重新登录。',
      '确认修改密码',
      {
        confirmButtonText: '确定修改',
        cancelButtonText: '再想想',
        type: 'warning',
        center: true,
        confirmButtonClass: 'confirm-btn'
      }
    )

    loading.value = true

    // 使用正确的API接口
    const requestData = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }

    console.log('发送修改密码请求:', requestData)

    const response = await changePassword(requestData)

    console.log('完整后端响应:', response)
    console.log('响应状态码:', response.code)
    console.log('响应消息:', response.message)
    console.log('响应数据:', response.data)

    if (response.code === 200 || response.code === 0 || response.status === 200) {
      // 使用 ElMessage.success 显示绿色成功提示
      ElMessage.success('密码修改成功！')
      
      // 清空表单
      handleReset()
      
      // 成功提示 - 使用 info 类型显示蓝色提示
      setTimeout(() => {
        ElMessage.info('为了账户安全，建议您重新登录')
      }, 1500)
      
    } else {
      // 错误情况使用 ElMessage.error 显示红色提示
      ElMessage.error('密码修改失败，请重试',error)
    }

  } catch (error) {
    console.error('修改密码完整错误信息:', error)
    
    if (error === 'cancel') {
      // 用户取消操作使用 info 类型
      ElMessage.info('已取消修改')
    } else if (error.response) {
      // 后端返回的错误使用 error 类型
      const errorMsg = error.response.data?.message || error.response.data?.msg || '修改密码失败'
      ElMessage.error(errorMsg)
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      ElMessage.error(error.message || '修改密码失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  passwordFormRef.value?.resetFields()
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style scoped>
.password-reset-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.password-reset-wrapper {
  width: 100%;
  max-width: 520px;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
  color: white;
}

.header-section .title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.header-section .subtitle {
  font-size: 14px;
  opacity: 0.9;
}

.password-form-card {
  border-radius: 12px;
  margin-bottom: 20px;
  border: none;
}

.password-form {
  padding: 10px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

.password-tips {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.strength-indicator {
  display: flex;
  gap: 4px;
}

.strength-bar {
  width: 60px;
  height: 4px;
  background-color: #e0e0e0;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-bar.active.weak {
  background-color: #ff4d4f;
}

.strength-bar.active.medium {
  background-color: #faad14;
}

.strength-bar.active.strong {
  background-color: #52c41a;
}

.strength-text {
  font-size: 12px;
  color: #909399;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .el-form-item__content {
  justify-content: center;
}

.submit-btn {
  width: 140px;
  border-radius: 8px;
}

.reset-btn {
  width: 120px;
  border-radius: 8px;
}

.tips-card {
  border-radius: 12px;
  border: none;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #1890ff;
}

.tips-header .el-icon {
  color: #1890ff;
}

.tips-list {
  color: #606266;
  line-height: 1.8;
  padding-left: 16px;
  margin: 0;
}

.tips-list li {
  margin-bottom: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .password-reset-container {
    padding: 20px 16px;
  }
  
  .password-reset-wrapper {
    max-width: 100%;
  }
  
  .header-section .title {
    font-size: 24px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  .form-actions .el-form-item__content {
    flex-direction: column;
    gap: 12px;
  }
  
  .submit-btn,
  .reset-btn {
    width: 100%;
  }
}

/* 自定义确认按钮样式 */
:deep(.confirm-btn) {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
}

:deep(.confirm-btn:hover) {
  background-color: #ff7875;
  border-color: #ff7875;
}
</style>