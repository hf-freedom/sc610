<template>
  <div class="vehicle-page">
    <div class="page-header">
      <h2>车辆管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        添加车辆
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="忙碌" value="BUSY" />
            <el-option label="运输中" value="TRANSPORTING" />
            <el-option label="维护中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-select v-model="filterForm.region" placeholder="选择区域" clearable style="width: 150px;">
            <el-option label="全部区域" value="" />
            <el-option label="华东区" value="华东区" />
            <el-option label="华北区" value="华北区" />
            <el-option label="华南区" value="华南区" />
            <el-option label="西南区" value="西南区" />
            <el-option label="华中区" value="华中区" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadVehicles">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="filteredVehicles" stripe border style="width: 100%;" v-loading="loading">
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="model" label="车型" />
        <el-table-column prop="maxWeight" label="载重(kg)" width="120">
          <template #default="{ row }">{{ row.maxWeight?.toFixed(0) }}</template>
        </el-table-column>
        <el-table-column prop="maxVolume" label="容积(m³)" width="120">
          <template #default="{ row }">{{ row.maxVolume?.toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="region" label="所属区域" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link>编辑</el-button>
            <el-button size="small" type="warning" link @click="updateStatus(row.id, 'MAINTENANCE')" v-if="row.status === 'AVAILABLE'">标记维护</el-button>
            <el-button size="small" type="danger" link @click="deleteVehicle(row.id)" v-if="row.status === 'AVAILABLE'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="添加车辆" width="600px">
      <el-form :model="vehicleForm" label-width="100px">
        <el-form-item label="车牌号" required>
          <el-input v-model="vehicleForm.plateNumber" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车型" required>
          <el-select v-model="vehicleForm.model" placeholder="请选择车型" style="width: 100%;">
            <el-option label="4.2米厢式货车" value="4.2米厢式货车" />
            <el-option label="6.8米厢式货车" value="6.8米厢式货车" />
            <el-option label="9.6米厢式货车" value="9.6米厢式货车" />
            <el-option label="13米半挂车" value="13米半挂车" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大载重(kg)" required>
          <el-input-number v-model="vehicleForm.maxWeight" :min="0" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="最大容积(m³)" required>
          <el-input-number v-model="vehicleForm.maxVolume" :min="0" :step="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="所属区域" required>
          <el-select v-model="vehicleForm.region" placeholder="请选择区域" style="width: 100%;">
            <el-option label="华东区" value="华东区" />
            <el-option label="华北区" value="华北区" />
            <el-option label="华南区" value="华南区" />
            <el-option label="西南区" value="西南区" />
            <el-option label="华中区" value="华中区" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createVehicle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { vehicleApi } from '@/api'

const vehicles = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const filterForm = ref({ status: '', region: '' })
const vehicleForm = ref({
  plateNumber: '',
  model: '',
  maxWeight: 2000,
  maxVolume: 15,
  region: ''
})

const filteredVehicles = computed(() => {
  return vehicles.value.filter(vehicle => {
    if (filterForm.value.status && vehicle.status !== filterForm.value.status) return false
    if (filterForm.value.region && vehicle.region !== filterForm.value.region) return false
    return true
  })
})

const loadVehicles = async () => {
  loading.value = true
  try {
    const res = await vehicleApi.list()
    vehicles.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const createVehicle = async () => {
  if (!vehicleForm.value.plateNumber || !vehicleForm.value.model || !vehicleForm.value.region) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await vehicleApi.create(vehicleForm.value)
    ElMessage.success('添加成功')
    showCreateDialog.value = false
    vehicleForm.value = { plateNumber: '', model: '', maxWeight: 2000, maxVolume: 15, region: '' }
    loadVehicles()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const updateStatus = async (id, status) => {
  try {
    await vehicleApi.updateStatus(id, status)
    ElMessage.success('状态更新成功')
    loadVehicles()
  } catch (e) {
    ElMessage.error('状态更新失败')
  }
}

const deleteVehicle = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该车辆吗？', '提示', { type: 'warning' })
    await vehicleApi.delete(id)
    ElMessage.success('删除成功')
    loadVehicles()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const resetFilter = () => {
  filterForm.value = { status: '', region: '' }
  loadVehicles()
}

const getStatusType = (status) => {
  const map = { AVAILABLE: 'success', BUSY: 'warning', TRANSPORTING: 'primary', MAINTENANCE: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { AVAILABLE: '可用', BUSY: '忙碌', TRANSPORTING: '运输中', MAINTENANCE: '维护中' }
  return map[status] || status
}

onMounted(() => {
  loadVehicles()
})
</script>

<style scoped>
.vehicle-page {
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
