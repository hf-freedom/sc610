<template>
  <div class="warning-page">
    <h2 class="page-title">异常预警与调度池</h2>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card pending">
          <div class="stat-icon"><el-icon size="32"><Warning /></el-icon></div>
          <div>
            <div class="stat-number">{{ pendingCount }}</div>
            <div class="stat-label">待处理预警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card high">
          <div class="stat-icon"><el-icon size="32"><WarningFilled /></el-icon></div>
          <div>
            <div class="stat-number">{{ highCount }}</div>
            <div class="stat-label">高危预警</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pool">
          <div class="stat-icon"><el-icon size="32"><Van /></el-icon></div>
          <div>
            <div class="stat-number">{{ pendingCount }}</div>
            <div class="stat-label">调度池任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card handled">
          <div class="stat-icon"><el-icon size="32"><CircleCheck /></el-icon></div>
          <div>
            <div class="stat-number">{{ handledCount }}</div>
            <div class="stat-label">已处理预警</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" style="margin-top: 20px;">
      <el-tab-pane label="延误预警列表" name="list">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预警列表</span>
              <el-button type="primary" @click="loadWarnings">刷新</el-button>
            </div>
          </template>
          
          <el-table :data="warnings" border stripe>
            <el-table-column prop="type" label="预警类型" width="120">
              <template #default="{ row }">{{ row.type === 'DELAY' ? '延误预警' : row.type }}</template>
            </el-table-column>
            <el-table-column prop="level" label="预警级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)" size="small">
                  {{ row.level === 'HIGH' ? '高' : row.level === 'MEDIUM' ? '中' : '低' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="延误时长" width="100">
              <template #default="{ row }">{{ row.delayMinutes || 0 }}分钟</template>
            </el-table-column>
            <el-table-column prop="startAddress" label="发货地" width="100" />
            <el-table-column prop="endAddress" label="收货地" width="100" />
            <el-table-column label="货物信息" width="180">
              <template #default="{ row }">
                {{ (row.totalWeight || 0).toFixed(0) }}kg / {{ (row.totalVolume || 0).toFixed(1) }}m³
              </template>
            </el-table-column>
            <el-table-column prop="message" label="预警信息" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'" size="small">
                  {{ row.status === 'PENDING' ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="info" link @click="viewDetail(row)">查看明细</el-button>
                <el-button size="small" type="warning" link @click="openReschedule(row)" v-if="row.status === 'PENDING'">重新调度</el-button>
                <el-button size="small" type="primary" link @click="handleWarning(row)" v-if="row.status === 'PENDING'">标记处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="调度池" name="pool">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待调度任务</span>
              <div class="pool-tip">
                <el-icon><InfoFilled /></el-icon>
                选择车辆后可重新安排运输
              </div>
            </div>
          </template>
          
          <el-table :data="pendingWarnings" border stripe>
            <el-table-column prop="level" label="优先级" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)" size="small">
                  {{ row.level === 'HIGH' ? '紧急' : row.level === 'MEDIUM' ? '一般' : '低' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="运输路线" width="180">
              <template #default="{ row }">
                <el-icon><Position /></el-icon>
                {{ row.startAddress }} → {{ row.endAddress }}
              </template>
            </el-table-column>
            <el-table-column label="货物信息" width="150">
              <template #default="{ row }">
                <div>重量: {{ (row.totalWeight || 0).toFixed(0) }}kg</div>
                <div>体积: {{ (row.totalVolume || 0).toFixed(1) }}m³</div>
              </template>
            </el-table-column>
            <el-table-column label="订单数量" width="100" align="center">
              <template #default="{ row }">{{ row.orderIds?.length || 0 }}单</template>
            </el-table-column>
            <el-table-column prop="delayMinutes" label="已延误" width="100" align="center">
              <template #default="{ row }">{{ row.delayMinutes || 0 }}分钟</template>
            </el-table-column>
            <el-table-column prop="message" label="问题描述" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="warning" @click="openReschedule(row)">
                  <el-icon><Refresh /></el-icon>
                  重新安排车辆
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showHandleDialog" title="标记处理" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理人">
          <el-input v-model="handleForm.handler" placeholder="请输入处理人姓名" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="4" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="延误预警明细" width="900px">
      <div v-if="warningDetail">
        <el-descriptions :column="2" border class="detail-section">
          <el-descriptions-item label="预警类型">延误预警</el-descriptions-item>
          <el-descriptions-item label="预警级别">
            <el-tag :type="getLevelType(warningDetail.warning?.level)" size="small">
              {{ warningDetail.warning?.level === 'HIGH' ? '高' : warningDetail.warning?.level === 'MEDIUM' ? '中' : '低' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="延误时长">{{ warningDetail.warning?.delayMinutes }}分钟</el-descriptions-item>
          <el-descriptions-item label="所在区域">{{ warningDetail.warning?.region }}</el-descriptions-item>
          <el-descriptions-item label="运输路线">{{ warningDetail.warning?.startAddress }} → {{ warningDetail.warning?.endAddress }}</el-descriptions-item>
          <el-descriptions-item label="货物总量">
            {{ (warningDetail.warning?.totalWeight || 0).toFixed(0) }}kg / {{ (warningDetail.warning?.totalVolume || 0).toFixed(1) }}m³
          </el-descriptions-item>
          <el-descriptions-item label="关联订单数">{{ warningDetail.orders?.length || 0 }}单</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="warningDetail.warning?.status === 'PENDING' ? 'warning' : 'success'" size="small">
              {{ warningDetail.warning?.status === 'PENDING' ? '待处理' : '已处理' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>关联订单列表</h4>
          <el-table :data="warningDetail.orders || []" border stripe size="small">
            <el-table-column prop="orderNo" label="订单编号" width="120" />
            <el-table-column prop="goodsName" label="货物名称" />
            <el-table-column prop="weight" label="重量(kg)" width="100" />
            <el-table-column prop="volume" label="体积(m³)" width="100" />
            <el-table-column prop="startAddress" label="发货地" width="120" />
            <el-table-column prop="endAddress" label="收货地" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag type="danger" size="small">异常</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showRescheduleDialog" title="重新安排车辆" width="800px">
      <div v-if="rescheduleForm.warningId">
        <el-alert type="warning" :closable="false" class="reschedule-alert">
          <template #title>
            调度信息：{{ rescheduleForm.startAddress }} → {{ rescheduleForm.endAddress }}
          </template>
          总重量: {{ rescheduleForm.totalWeight }}kg | 总体积: {{ rescheduleForm.totalVolume }}m³
        </el-alert>

        <div class="reschedule-section">
          <h4>可用车辆列表</h4>
          <el-table 
            :data="availableVehicles" 
            border stripe 
            @selection-change="handleVehicleSelect"
            height="300"
          >
            <el-table-column type="radio" width="55" />
            <el-table-column prop="plateNumber" label="车牌号" width="120" />
            <el-table-column prop="model" label="车型" />
            <el-table-column prop="region" label="所属区域" width="120" />
            <el-table-column prop="maxWeight" label="载重(kg)" width="100" />
            <el-table-column prop="maxVolume" label="容积(m³)" width="100" />
            <el-table-column label="容量状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.maxWeight >= rescheduleForm.totalWeight && row.maxVolume >= rescheduleForm.totalVolume" 
                        type="success" size="small">可用</el-tag>
                <el-tag v-else type="danger" size="small">容量不足</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-form :model="rescheduleForm" label-width="100px" class="reschedule-section">
          <el-form-item label="选择司机">
            <el-select v-model="rescheduleForm.driverId" style="width: 100%;" placeholder="请选择司机">
              <el-option v-for="driver in availableDrivers" :key="driver.id" :label="driver.name" :value="driver.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理人">
            <el-input v-model="rescheduleForm.handler" placeholder="请输入调度员姓名" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showRescheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReschedule" :disabled="!canReschedule">确认调度</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { warningApi, vehicleApi, driverApi } from '@/api'

const warnings = ref([])
const activeTab = ref('list')
const showHandleDialog = ref(false)
const showDetailDialog = ref(false)
const showRescheduleDialog = ref(false)
const handleForm = ref({ warningId: '', handler: '', remark: '' })
const currentWarning = ref(null)
const warningDetail = ref(null)
const availableVehicles = ref([])
const availableDrivers = ref([])
const selectedVehicle = ref(null)
const rescheduleForm = ref({
  warningId: '',
  vehicleId: '',
  driverId: '',
  handler: '',
  startAddress: '',
  endAddress: '',
  totalWeight: 0,
  totalVolume: 0
})

const pendingCount = computed(() => warnings.value.filter(w => w.status === 'PENDING').length)
const handledCount = computed(() => warnings.value.filter(w => w.status === 'HANDLED').length)
const highCount = computed(() => warnings.value.filter(w => w.level === 'HIGH' && w.status === 'PENDING').length)
const pendingWarnings = computed(() => warnings.value.filter(w => w.status === 'PENDING'))
const canReschedule = computed(() => selectedVehicle.value && rescheduleForm.value.driverId && rescheduleForm.value.handler)

const loadWarnings = async () => {
  try {
    const res = await warningApi.list()
    warnings.value = Array.isArray(res) ? res : []
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

const handleWarning = (warning) => {
  currentWarning.value = warning
  handleForm.value = { warningId: warning.id, handler: '', remark: '' }
  showHandleDialog.value = true
}

const submitHandle = async () => {
  if (!handleForm.value.handler) {
    ElMessage.warning('请输入处理人')
    return
  }
  try {
    await warningApi.handle(handleForm.value.warningId, handleForm.value.handler, handleForm.value.remark)
    ElMessage.success('处理成功')
    showHandleDialog.value = false
    loadWarnings()
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

const viewDetail = async (warning) => {
  try {
    const res = await warningApi.getDetail(warning.id)
    warningDetail.value = res
    showDetailDialog.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const openReschedule = async (warning) => {
  currentWarning.value = warning
  rescheduleForm.value = {
    warningId: warning.id,
    vehicleId: '',
    driverId: '',
    handler: '',
    startAddress: warning.startAddress,
    endAddress: warning.endAddress,
    totalWeight: warning.totalWeight,
    totalVolume: warning.totalVolume
  }
  selectedVehicle.value = null
  
  try {
    const [vehicles, drivers] = await Promise.all([
      vehicleApi.available(warning.region, warning.totalWeight, warning.totalVolume),
      driverApi.available()
    ])
    availableVehicles.value = Array.isArray(vehicles) ? vehicles : []
    availableDrivers.value = Array.isArray(drivers) ? drivers : []
    showRescheduleDialog.value = true
  } catch (e) {
    ElMessage.error('加载可用资源失败')
  }
}

const handleVehicleSelect = (selection) => {
  if (selection && selection.length > 0) {
    selectedVehicle.value = selection[0]
    rescheduleForm.value.vehicleId = selection[0].id
  } else {
    selectedVehicle.value = null
    rescheduleForm.value.vehicleId = ''
  }
}

const submitReschedule = async () => {
  if (!canReschedule.value) {
    ElMessage.warning('请选择车辆和司机，并填写处理人')
    return
  }
  try {
    await warningApi.reschedule(
      rescheduleForm.value.warningId,
      rescheduleForm.value.vehicleId,
      rescheduleForm.value.driverId,
      rescheduleForm.value.handler
    )
    ElMessage.success('重新调度成功，车辆已分配')
    showRescheduleDialog.value = false
    loadWarnings()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '调度失败')
  }
}

const getLevelType = (level) => {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[level] || 'info'
}

onMounted(() => {
  loadWarnings()
  loadDrivers()
})
</script>

<style scoped>
.warning-page {
  padding: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-card.pending {
  border-left: 4px solid #f59e0b;
}

.stat-card.high {
  border-left: 4px solid #ef4444;
}

.stat-card.pool {
  border-left: 4px solid #3b82f6;
}

.stat-card.handled {
  border-left: 4px solid #10b981;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pending .stat-icon {
  background: #fef3c7;
  color: #f59e0b;
}

.high .stat-icon {
  background: #fee2e2;
  color: #ef4444;
}

.pool .stat-icon {
  background: #dbeafe;
  color: #3b82f6;
}

.handled .stat-icon {
  background: #d1fae5;
  color: #10b981;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pool-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #1e293b;
}

.reschedule-alert {
  margin-bottom: 20px;
}

.reschedule-section {
  margin-top: 20px;
}

.reschedule-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #1e293b;
}
</style>
