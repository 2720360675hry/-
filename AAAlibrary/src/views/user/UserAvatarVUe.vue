<template>
  <div class="user-avatar-container">
    <div class="avatar-upload-card">
      <h2 class="title">上传头像</h2>

      <!-- 上传区域 -->
      <div class="upload-section">
        <h3>上传新头像</h3>
        
        <!-- 上传控件 -->
        <div class="upload-area">
          <input 
            type="file" 
            ref="fileInput"
            @change="handleFileSelect"
            accept="image/jpeg,image/png,image/gif"
            class="file-input"
            hidden
          />
          
          <div 
            class="upload-box"
            @click="triggerFileInput"
            @dragover.prevent
            @drop="handleDrop"
          >
            <div class="upload-icon">
              <i class="el-icon-upload"></i>
            </div>
            <p class="upload-text">点击或拖拽图片到此处上传</p>
            <p class="upload-hint">支持 JPG、PNG、GIF 格式，大小不超过 2MB</p>
          </div>
        </div>

        <!-- 新头像预览 -->
        <div v-if="newAvatarPreview" class="new-avatar-preview">
          <h4>新头像预览</h4>
          <div class="preview-container">
            <img 
              :src="newAvatarPreview" 
              alt="新头像预览"
              class="preview-image"
            />
            <div class="preview-actions">
              <button 
                @click="confirmUpload" 
                :disabled="uploading"
                class="confirm-btn"
              >
                {{ uploading ? '上传中...' : '确认使用' }}
              </button>
              <button 
                @click="cancelPreview" 
                class="cancel-btn"
              >
                取消
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploading" class="upload-progress">
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: progress + '%' }"
          ></div>
        </div>
        <span class="progress-text">{{ progress }}%</span>
      </div>

      <!-- 消息提示 -->
      <div v-if="message" :class="['message', messageType]">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script>
import { uploadAvatarService } from '@/api/user.js'

export default {
  name: 'UserAvatarVue',
  data() {
    return {
      currentAvatarUrl: '', // 当前头像URL
      newAvatarFile: null, // 新选择的文件
      newAvatarPreview: '', // 新头像预览URL
      uploading: false, // 上传状态
      progress: 0, // 上传进度
      message: '', // 提示消息
      messageType: 'success' // 消息类型: success/error
    }
  },
  
  mounted() {
    // 初始化时获取当前用户头像
    this.loadCurrentAvatar()
  },
  
  methods: {
    // 触发文件选择
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    // 处理文件选择
    handleFileSelect(event) {
      const file = event.target.files[0]
      if (file) {
        this.validateAndPreviewFile(file)
      }
    },
    
    // 处理拖放文件
    handleDrop(event) {
      event.preventDefault()
      const file = event.dataTransfer.files[0]
      if (file) {
        this.validateAndPreviewFile(file)
      }
    },
    
    // 验证文件并生成预览
    validateAndPreviewFile(file) {
      // 验证文件类型
      const validTypes = ['image/jpeg', 'image/png', 'image/gif']
      if (!validTypes.includes(file.type)) {
        this.showMessage('请选择 JPG、PNG 或 GIF 格式的图片', 'error')
        return
      }
      
      // 验证文件大小 (2MB)
      const maxSize = 2 * 1024 * 1024
      if (file.size > maxSize) {
        this.showMessage('图片大小不能超过 2MB', 'error')
        return
      }
      
      this.newAvatarFile = file
      
      // 生成预览
      const reader = new FileReader()
      reader.onload = (e) => {
        this.newAvatarPreview = e.target.result
      }
      reader.readAsDataURL(file)
    },
    
    // 确认上传
    async confirmUpload() {
      if (!this.newAvatarFile) return
      
      this.uploading = true
      this.progress = 0
      
      try {
        // 调用上传接口
        const response = await uploadAvatarService(this.newAvatarFile)
        
        if (response.code === 0) {
          this.progress = 100
          
          const avatarUrl = response.data
          
          // 更新当前头像显示
          this.currentAvatarUrl = avatarUrl
          
          this.showMessage('头像上传成功！', 'success')
          
          // 触发全局头像更新事件
          this.$emit('avatar-updated', avatarUrl)
          
          // 重置状态
          setTimeout(() => {
            this.resetUploadState()
          }, 1500)
        } else {
          throw new Error(response.message || '上传失败')
        }
        
      } catch (error) {
        this.showMessage(error.message || '头像上传失败，请重试', 'error')
        this.uploading = false
        this.progress = 0
      }
    },
    
    // 取消预览
    cancelPreview() {
      this.resetUploadState()
    },
    
    // 重置上传状态
    resetUploadState() {
      this.newAvatarFile = null
      this.newAvatarPreview = ''
      this.uploading = false
      this.progress = 0
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = ''
      }
    },
    
    // 加载当前头像
    loadCurrentAvatar() {
      // 这里应该从用户信息中获取当前头像URL
      // 示例：从 Vuex store 或 localStorage 获取
      // this.currentAvatarUrl = this.$store.state.user.avatarUrl
    },
    
    // 显示消息
    showMessage(text, type = 'success') {
      this.message = text
      this.messageType = type
      
      // 3秒后自动清除消息
      setTimeout(() => {
        this.message = ''
      }, 3000)
    }
  }
}
</script>

<style scoped>
/* 原有的样式保持不变 */
.user-avatar-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.avatar-upload-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 24px;
}

.current-avatar-section,
.upload-section {
  margin-bottom: 30px;
}

.current-avatar-section h3,
.upload-section h3 {
  color: #666;
  margin-bottom: 15px;
  font-size: 16px;
}

.avatar-preview {
  text-align: center;
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e8f4fd;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border: 2px dashed #dcdfe6;
}

.avatar-placeholder i {
  font-size: 40px;
  color: #c0c4cc;
}

.upload-area {
  margin-bottom: 20px;
}

.upload-box {
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-box:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.new-avatar-preview h4 {
  color: #666;
  margin-bottom: 15px;
}

.preview-container {
  text-align: center;
}

.preview-image {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 15px;
  border: 2px solid #e8f4fd;
}

.preview-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.confirm-btn,
.cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.confirm-btn {
  background: #409eff;
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background: #66b1ff;
}

.confirm-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

.cancel-btn {
  background: #f4f4f5;
  color: #606266;
}

.cancel-btn:hover {
  background: #e9e9eb;
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 20px 0;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #409eff;
  transition: width 0.3s;
}

.progress-text {
  font-size: 14px;
  color: #666;
  min-width: 40px;
}

.message {
  padding: 12px 16px;
  border-radius: 4px;
  margin-top: 20px;
  text-align: center;
}

.message.success {
  background: #f0f9ff;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.message.error {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}
</style>