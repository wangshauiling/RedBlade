<template>
  <div class="navbar-container">
    <!-- 左侧 -->
    <div class="navbar-left">
      <!-- 折叠按钮 -->
      <el-icon class="collapse-btn" @click="toggleSidebar">
        <component :is="collapsed ? 'Expand' : 'Fold'" />
      </el-icon>

      <!-- 面包屑 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
          {{ item.meta?.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧 -->
    <div class="navbar-right">
      <!-- 全屏按钮 -->
      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="navbar-icon" @click="toggleFullscreen">
          <FullScreen />
        </el-icon>
      </el-tooltip>

      <!-- 用户信息 -->
      <el-dropdown class="user-dropdown" trigger="click">
        <div class="user-info">
          <el-avatar :size="30" icon="UserFilled" />
          <span class="username">{{ nickname }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="goProfile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item @click="goChangePassword">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const collapsed = computed(() => appStore.sidebarCollapsed)
const nickname = computed(() => userStore.nickname || '用户')

// 面包屑
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

// 切换侧边栏
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

// 全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 个人中心
const goProfile = () => {
  router.push('/profile')
}

// 修改密码
const goChangePassword = () => {
  router.push('/change-password')
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logoutAction()
  })
}
</script>

<style lang="scss" scoped>
.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 100%;
}

.navbar-left {
  display: flex;
  align-items: center;

  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    margin-right: 15px;
  }

  .el-breadcrumb {
    font-size: 14px;
  }
}

.navbar-right {
  display: flex;
  align-items: center;

  .navbar-icon {
    font-size: 20px;
    cursor: pointer;
    margin-right: 15px;
  }
}

.user-dropdown {
  cursor: pointer;

  .user-info {
    display: flex;
    align-items: center;

    .username {
      margin: 0 8px;
      font-size: 14px;
    }
  }
}
</style>