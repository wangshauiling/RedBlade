<template>
  <div class="user-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span>用户列表</span>
          <div class="table-actions">
            <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
            <el-button type="danger" icon="Delete" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
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
        <el-table-column prop="deptName" label="部门" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
            <el-button type="warning" link icon="Lock" @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isAdd">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

const searchForm = reactive({
  username: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const formRef = ref(null)
const isAdd = ref(true)

const formData = reactive({
  userCode: '',
  username: '',
  nickname: '',
  password: '',
  status: '0'
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const dialogTitle = computed(() => isAdd.value ? '新增用户' : '编辑用户')

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchTableData()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.status = ''
  handleSearch()
}

// 获取表格数据
const fetchTableData = async () => {
  loading.value = true
  try {
    // TODO: 调用后端接口
    // 模拟数据
    tableData.value = [
      { userCode: 'admin', username: 'admin', nickname: '超级管理员', orgName: 'RedBlade', deptName: '研发部', status: '0', createTime: '2026-05-20 10:00:00' },
      { userCode: 'user001', username: 'zhangsan', nickname: '张三', orgName: 'RedBlade', deptName: '研发部', status: '0', createTime: '2026-05-20 11:00:00' },
      { userCode: 'user002', username: 'lisi', nickname: '李四', orgName: 'RedBlade', deptName: '市场部', status: '1', createTime: '2026-05-20 12:00:00' }
    ]
    pagination.total = 3
  } finally {
    loading.value = false
  }
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = () => {
  pagination.page = 1
  fetchTableData()
}

const handlePageChange = () => {
  fetchTableData()
}

// 新增
const handleAdd = () => {
  isAdd.value = true
  Object.assign(formData, {
    userCode: '',
    username: '',
    nickname: '',
    password: '',
    status: '0'
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isAdd.value = false
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchTableData()
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchTableData()
  })
}

// 重置密码
const handleResetPwd = (row) => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    inputPattern: /^.{4,}$/,
    inputErrorMessage: '密码长度不能少于4位'
  }).then(({ value }) => {
    ElMessage.success(`用户 "${row.username}" 密码已重置为: ${value}`)
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success(isAdd.value ? '新增成功' : '编辑成功')
      dialogVisible.value = false
      fetchTableData()
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 初始化
fetchTableData()
</script>

<style lang="scss" scoped>
.user-container {
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