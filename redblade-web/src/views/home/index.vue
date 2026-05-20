<template>
  <div class="home-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ nickname }}！</h2>
          <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
        </div>
        <div class="welcome-illustration">
          <el-icon :size="100" color="#409EFF"><Sunny /></el-icon>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ onlineCount }}</div>
              <div class="stat-label">在线用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon :size="30"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">128</div>
              <div class="stat-label">文档数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon :size="30"><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">56</div>
              <div class="stat-label">消息通知</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon :size="30"><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">12</div>
              <div class="stat-label">待办事项</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card class="quick-entry">
      <template #header>
        <span>快捷入口</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="item in quickLinks" :key="item.name">
          <div class="quick-item" @click="handleQuickLink(item)">
            <el-icon :size="40" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.name }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 系统信息 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">RedBlade 企业级管理系统</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="前端框架">Vue 3.4 + Vite 5</el-descriptions-item>
            <el-descriptions-item label="UI框架">Element Plus 2.6</el-descriptions-item>
            <el-descriptions-item label="后端框架">Spring Boot 3.2.5</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
            <el-descriptions-item label="组织">{{ userInfo.orgName }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ roles.join(', ') || '暂无角色' }}</el-descriptions-item>
            <el-descriptions-item label="数据权限">{{ dataScopeText }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getOnlineCount } from '@/api/online'

const router = useRouter()
const userStore = useUserStore()

const onlineCount = ref(0)

const nickname = computed(() => userStore.nickname)
const userInfo = computed(() => userStore.userInfo)
const roles = computed(() => userStore.roles)

const dataScopeText = computed(() => {
  const scopeMap = {
    '1': '全部数据',
    '2': '自定义数据',
    '3': '本组织数据',
    '4': '本组织及以下',
    '5': '仅本人数据'
  }
  return scopeMap[userStore.userInfo?.dataScope] || '未知'
})

const currentDate = computed(() => {
  const now = new Date()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})

const quickLinks = [
  { name: '用户管理', icon: 'User', color: '#409EFF', path: '/system/user' },
  { name: '角色管理', icon: 'UserFilled', color: '#67C23A', path: '/system/role' },
  { name: '菜单管理', icon: 'Menu', color: '#E6A23C', path: '/system/menu' },
  { name: '部门管理', icon: 'OfficeBuilding', color: '#F56C6C', path: '/system/dept' },
  { name: '在线用户', icon: 'Connection', color: '#909399', path: '/monitor/online' },
  { name: 'API文档', icon: 'Document', color: '#9C27B0', path: '/api/doc.html', external: true }
]

const handleQuickLink = (item) => {
  if (item.external) {
    window.open(item.path, '_blank')
  } else {
    router.push(item.path)
  }
}

const fetchOnlineCount = async () => {
  try {
    const res = await getOnlineCount()
    onlineCount.value = res.data
  } catch (error) {
    console.error('获取在线用户数失败', error)
  }
}

onMounted(() => {
  fetchOnlineCount()
})
</script>

<style lang="scss" scoped>
.home-container {
  .welcome-card {
    margin-bottom: 20px;

    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .welcome-text {
        h2 {
          font-size: 24px;
          margin-bottom: 10px;
        }

        p {
          color: #909399;
        }
      }
    }
  }

  .stat-row {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .stat-info {
        margin-left: 20px;

        .stat-value {
          font-size: 28px;
          font-weight: 600;
        }

        .stat-label {
          color: #909399;
          margin-top: 5px;
        }
      }
    }
  }

  .quick-entry {
    margin-bottom: 20px;

    .quick-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 0;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        background: #f5f7fa;
      }

      span {
        margin-top: 10px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>