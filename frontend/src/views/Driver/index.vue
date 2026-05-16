<template>
  <div class="driver-page">
    <div class="page-header">
      <h2>司机管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        添加司机
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="空闲" value="AVAILABLE" />
            <el-option label="接单中" value="BUSY" />
            <el-option label="运输中" value="TRANSPORTING" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDrivers">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="filteredDrivers" stripe border style="width: 100%;" v-loading="loading">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="onTimeRate" label="准时率" width="120">
          <template #default="{ row }">{{ (row.onTimeRate || 0).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="总订单数" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button size="small" type="danger" link @click="deleteDriver(row.id)" v-if="row.status === 'AVAILABLE'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="添加司机" width="600px">
      <el-form :model="driverForm" label-width="100px">
        <el-form-item label="姓名" required>
          <el-input v-model="driverForm.name" placeholder="请输入司机姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="driverForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createDriver">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { driverApi } from '@/api'

const drivers = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const filterForm = ref({ status: '' })
const driverForm = ref({ name: '', phone: '' })

const filteredDrivers = computed(() => {
  return drivers.value.filter(driver => {
    if (filterForm.value.status && driver.status !== filterForm.value.status) return false
    return true
  })
})

const loadDrivers = async () => {
  loading.value = true
  try {
    const res = await driverApi.list()
    drivers.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const createDriver = async () => {
  if (!driverForm.value.name || !driverForm.value.phone) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await driverApi.create(driverForm.value)
    ElMessage.success('添加成功')
    showCreateDialog.value = false
    driverForm.value = { name: '', phone: '' }
    loadDrivers()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const deleteDriver = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该司机吗？', '提示', { type: 'warning' })
    await driverApi.delete(id)
    ElMessage.success('删除成功')
    loadDrivers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const resetFilter = () => {
  filterForm.value = { status: '' }
  loadDrivers()
}

const getStatusType = (status) => {
  const map = { AVAILABLE: 'success', BUSY: 'warning', TRANSPORTING: 'primary' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { AVAILABLE: '空闲', BUSY: '接单中', TRANSPORTING: '运输中' }
  return map[status] || status
}

onMounted(() => {
  loadDrivers()
})
</script>

<style scoped>
.driver-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  padding: 0;
}
</style>
