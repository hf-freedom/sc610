<template>
  <div class="dispatch-page">
    <h2 class="page-title">智能调度</h2>
    
    <el-card class="match-card">
      <template #header>
        <div class="card-header">
          <span>车辆匹配</span>
          <el-button type="primary" @click="matchVehicles">
            <el-icon><Refresh /></el-icon>
            刷新匹配
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="matchForm">
        <el-form-item label="区域">
          <el-select v-model="matchForm.region" placeholder="选择区域" clearable style="width: 150px;">
            <el-option label="华东区" value="华东区" />
            <el-option label="华北区" value="华北区" />
            <el-option label="华南区" value="华南区" />
            <el-option label="西南区" value="西南区" />
            <el-option label="华中区" value="华中区" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计重量(kg)">
          <el-input-number v-model="matchForm.weight" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="预计体积(m³)">
          <el-input-number v-model="matchForm.volume" :min="0" :step="1" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待调度订单 ({{ pendingOrders.length }})</span>
          </template>
          <el-table :data="pendingOrders" border stripe height="400" @selection-change="handleOrderSelect">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="orderNo" label="订单编号" width="120" />
            <el-table-column prop="goodsName" label="货物名称" />
            <el-table-column prop="weight" label="重量" width="90">
              <template #default="{ row }">{{ (row.weight || 0).toFixed(0) }}</template>
            </el-table-column>
            <el-table-column prop="volume" label="体积" width="90">
              <template #default="{ row }">{{ (row.volume || 0).toFixed(1) }}</template>
            </el-table-column>
            <el-table-column prop="region" label="区域" width="90" />
            <el-table-column prop="startAddress" label="发货地" />
            <el-table-column prop="endAddress" label="收货地" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>可用车辆 ({{ availableVehicles.length }})</span>
          </template>
          <el-table :data="availableVehicles" border stripe height="400" @selection-change="handleVehicleSelect">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="plateNumber" label="车牌号" width="110" />
            <el-table-column prop="model" label="车型" width="130" />
            <el-table-column prop="maxWeight" label="载重" width="90">
              <template #default="{ row }">{{ (row.maxWeight || 0).toFixed(0) }}</template>
            </el-table-column>
            <el-table-column prop="maxVolume" label="容积" width="90">
              <template #default="{ row }">{{ (row.maxVolume || 0).toFixed(1) }}</template>
            </el-table-column>
            <el-table-column prop="region" label="区域" width="90" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>选择司机</span>
          </template>
          <el-select v-model="selectedDriverId" placeholder="请选择司机" style="width: 100%;">
            <el-option v-for="driver in availableDrivers" :key="driver.id" :label="driver.name" :value="driver.id" />
          </el-select>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="action-card">
          <div class="action-info">
            <div>已选订单: <span class="highlight">{{ selectedOrders.length }}</span> 单</div>
            <div>总重量: <span class="highlight">{{ totalWeight.toFixed(0) }}kg</span></div>
            <div>总体积: <span class="highlight">{{ totalVolume.toFixed(1) }}m³</span></div>
          </div>
          <el-button type="primary" size="large" @click="createBatch" :disabled="!canCreate">
            <el-icon><Van /></el-icon>
            创建运输批次
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>运输批次列表</span>
      </template>
      <el-table :data="batches" border stripe>
        <el-table-column prop="batchNo" label="批次编号" width="140" />
        <el-table-column prop="startAddress" label="发货地" />
        <el-table-column prop="endAddress" label="收货地" />
        <el-table-column prop="totalWeight" label="总重量" width="100">
          <template #default="{ row }">{{ (row.totalWeight || 0).toFixed(0) }}</template>
        </el-table-column>
        <el-table-column prop="totalVolume" label="总体积" width="100">
          <template #default="{ row }">{{ (row.totalVolume || 0).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi, vehicleApi, driverApi, dispatchApi } from '@/api'

const pendingOrders = ref([])
const availableVehicles = ref([])
const availableDrivers = ref([])
const batches = ref([])
const selectedOrders = ref([])
const selectedVehicles = ref([])
const selectedDriverId = ref('')
const matchForm = ref({ region: '', weight: 0, volume: 0 })

const totalWeight = computed(() => selectedOrders.value.reduce((sum, o) => sum + (o.weight || 0), 0))
const totalVolume = computed(() => selectedOrders.value.reduce((sum, o) => sum + (o.volume || 0), 0))

const canCreate = computed(() => {
  return selectedOrders.value.length > 0 && selectedVehicles.value.length === 1 && selectedDriverId.value
})

const loadPendingOrders = async () => {
  try {
    const res = await orderApi.list()
    pendingOrders.value = (Array.isArray(res) ? res : []).filter(o => o.status === 'PENDING')
  } catch (e) {
    console.error(e)
  }
}

const matchVehicles = async () => {
  try {
    const res = await vehicleApi.available(matchForm.value.region, matchForm.value.weight, matchForm.value.volume)
    availableVehicles.value = Array.isArray(res) ? res : []
    loadPendingOrders()
  } catch (e) {
    console.error(e)
  }
}

const loadDrivers = async () => {
  try {
    const res = await driverApi.available()
    availableDrivers.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  }
}

const loadBatches = async () => {
  try {
    const res = await dispatchApi.batchList()
    batches.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  }
}

const handleOrderSelect = (selection) => {
  selectedOrders.value = selection
}

const handleVehicleSelect = (selection) => {
  selectedVehicles.value = selection
}

const createBatch = async () => {
  try {
    const orderIds = selectedOrders.value.map(o => o.id)
    const vehicleId = selectedVehicles.value[0].id
    await dispatchApi.merge({ orderIds, vehicleId, driverId: selectedDriverId.value })
    ElMessage.success('批次创建成功')
    selectedOrders.value = []
    selectedVehicles.value = []
    selectedDriverId.value = ''
    loadPendingOrders()
    loadBatches()
  } catch (e) {
    ElMessage.error('批次创建失败: ' + (e.response?.data?.message || e.message))
  }
}

const getStatusType = (status) => {
  const map = { ASSIGNED: 'warning', ACCEPTED: 'primary', IN_TRANSIT: 'primary', COMPLETED: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ASSIGNED: '已分配', ACCEPTED: '已接单', IN_TRANSIT: '运输中', COMPLETED: '已完成' }
  return map[status] || status
}

onMounted(() => {
  matchVehicles()
  loadDrivers()
  loadBatches()
})
</script>

<style scoped>
.dispatch-page {
  padding: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-info {
  display: flex;
  gap: 30px;
  font-size: 16px;
}

.highlight {
  color: #3b82f6;
  font-weight: 600;
  font-size: 18px;
}
</style>
