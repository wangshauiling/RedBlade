<template>
  <div class="online-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="组织">
          <el-input v-model="searchForm.orgCode" placeholder="请输入组织编码" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>在线用户列表</span>
          <div class="table-actions">
            <el-button type="danger" icon="Delete" @click="handleBatchKickout" :disabled="selectedRows.length === 0">
              批量下线
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="userCode" label="用户编码" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="orgName" label="组织" width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="expireTime" label="过期时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleKickout(row)">强制下线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOnlineUsers, kickoutUser, kickoutBatch } from '@/api/online'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

const searchForm = reactive({
  orgCode: '',
  username: ''
})

// 获取在线用户列表
const fetchTableData = async () => {
  loading.value = true
  try {
    const res = await getOnlineUsers(searchForm)
    tableData.value = res.data || []
  } catch (error) {
    console.error('获取在线用户失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchTableData()
}

// 重置
const handleReset = () => {
  searchForm.orgCode = ''
  searchForm.username = ''
  fetchTableData()
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 强制下线
const handleKickout = (row) => {
  ElMessageBox.confirm(`确定要强制用户 "${row.username}" 下线吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await kickoutUser(row.token)
      ElMessage.success('已强制下线')
      fetchTableData()
    } catch (error) {
      console.error('强制下线失败', error)
    }
  })
}

// 批量下线
const handleBatchKickout = () => {
  const tokens = selectedRows.value.map(row => row.token)
  ElMessageBox.confirm(`确定要强制选中的 ${tokens.length} 个用户下线吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await kickoutBatch(tokens)
      ElMessage.success('已批量下线')
      fetchTableData()
    } catch (error) {
      console.error('批量下线失败', error)
    }
  })
}

onMounted(() => {
  fetchTableData()
})
</script>

<style lang="scss" scoped>
.online-container {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>