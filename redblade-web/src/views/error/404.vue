<template>
  <div class="error-container">
    <div class="error-content">
      <el-icon :size="120" color="#409EFF"><WarningFilled /></el-icon>
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;

  .error-content {
    text-align: center;

    h1 {
      font-size: 72px;
      color: #303133;
      margin: 20px 0;
    }

    p {
      font-size: 18px;
      color: #909399;
      margin-bottom: 30px;
    }
  }
}
</style>