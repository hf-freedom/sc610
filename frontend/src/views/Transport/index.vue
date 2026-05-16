<template>
  <div class="transport-page">
    <h2 class="page-title">运输监控</h2>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="mini-stat">
          <div class="stat-number">{{ transportingCount }}</div>
          <div class="stat-label">运输中批次</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="mini-stat">
          <div class="stat-number">{{ delayedCount }}</div>
          <div class="stat-label">已延误批次</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="mini-stat">
          <div class="stat-number">{{ todayCompleted }}</div>
          <div class="stat-label">今日完成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="mini-stat">
          <div class="stat-number">{{ onTimeRate.toFixed(1) }}%</div>
          <div class="stat-label">准时率</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>运输批次列表</span>
          <el-button type="primary" @click="loadTransporting">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="batches" border stripe>
        <el-table-column prop="batchNo" label="批次编号" width="140" />
        <el-table-column prop="startAddress" label="发货地" width="100" />
        <el-table-column prop="endAddress" label="收货地" width="100" />
        <el-table-column prop="totalWeight" label="总重量" width="90">
          <template #default="{ row }">{{ (row.totalWeight || 0).toFixed(0) }}kg</template>
        </el-table-column>
        <el-table-column prop="totalVolume" label="总体积" width="90">
          <template #default="{ row }">{{ (row.totalVolume || 0).toFixed(1) }}m³</template>
        </el-table-column>
        <el-table-column label="预计到达" width="160">
          <template #default="{ row }">
            <span v-if="row.expectedArrivalTime" class="eta-text">
              <el-icon><Clock /></el-icon>
              {{ formatDate(row.expectedArrivalTime) }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" link @click="showDetail(row)">查看详情</el-button>
            <el-button size="small" type="success" link @click="recordLoading(row)" v-if="row.status === 'ACCEPTED'">记录装车</el-button>
            <el-button size="small" type="success" link @click="acceptBatch(row)" v-if="row.status === 'ASSIGNED'">司机接单</el-button>
            <el-button size="small" type="primary" link @click="startTransport(row)" v-if="row.status === 'ACCEPTED'">开始运输</el-button>
            <el-button size="small" type="warning" link @click="updateNode(row)" v-if="row.status === 'IN_TRANSIT'">更新节点</el-button>
            <el-button size="small" type="success" link @click="completeBatch(row)" v-if="row.status === 'IN_TRANSIT'">完成运输</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showNodeDialog" title="更新运输节点" width="500px">
      <el-form :model="nodeForm" label-width="100px">
        <el-form-item label="当前节点">
          <el-select v-model="nodeForm.nodeName" style="width: 100%;">
            <el-option label="已离开发货地" value="已离开发货地" />
            <el-option label="运输途中 - 中转站A" value="运输途中 - 中转站A" />
            <el-option label="运输途中 - 中转站B" value="运输途中 - 中转站B" />
            <el-option label="已到达目的城市" value="已到达目的城市" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前位置">
          <el-input v-model="nodeForm.location" placeholder="请输入当前位置" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNodeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNodeUpdate">确认更新</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="运输批次详情" width="900px">
      <div v-if="currentBatch" class="batch-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次编号">{{ currentBatch.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="运输状态">
            <el-tag :type="getStatusType(currentBatch.status)">{{ getStatusText(currentBatch.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="运输路线">{{ currentBatch.startAddress }} → {{ currentBatch.endAddress }}</el-descriptions-item>
          <el-descriptions-item label="预计到达">
            <span class="eta-text">
              <el-icon><Clock /></el-icon>
              {{ formatDate(currentBatch.expectedArrivalTime) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDate(currentBatch.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(currentBatch.completeTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4><el-icon><Van /></el-icon> 订单装车顺序</h4>
          <el-table :data="batchOrders" border stripe size="small">
            <el-table-column prop="loadingOrder" label="装车顺序" width="90" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.loadingOrder" type="warning" size="small">第{{ row.loadingOrder }}车</el-tag>
                <span v-else class="text-muted">未装车</span>
              </template>
            </el-table-column>
            <el-table-column prop="orderNo" label="订单编号" width="120" />
            <el-table-column prop="goodsName" label="货物名称" />
            <el-table-column prop="weight" label="重量(kg)" width="100" />
            <el-table-column prop="volume" label="体积(m³)" width="100" />
            <el-table-column prop="startAddress" label="发货地" width="100" />
            <el-table-column prop="endAddress" label="收货地" width="100" />
          </el-table>
        </div>

        <div class="detail-section">
          <h4><el-icon><Location /></el-icon> 运输节点追踪</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(node, index) in getNodeHistory()"
              :key="index"
              :timestamp="formatDate(node.arrivalTime)"
              placement="top"
              :type="getNodeType(index)">
              <div class="node-content">
                <strong>{{ node.nodeName }}</strong>
                <p class="node-location">{{ node.location }}</p>
                <p class="node-remark" v-if="node.remark">{{ node.remark }}</p>
              </div>
            </el-timeline-item>
            <el-timeline-item type="primary" placement="top">
              <div class="node-content">
                <strong v-if="currentBatch.status === 'COMPLETED'">已送达目的地</strong>
                <strong v-else>运输中...</strong>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showLoadingDialog" title="记录装车顺序" width="600px">
      <div v-if="currentBatch">
        <p class="loading-tip">拖拽订单调整装车顺序</p>
        <el-table :data="loadingOrders" border stripe row-key="id">
          <el-table-column label="装车顺序" width="80" align="center">
            <template #default="{ $index }">
              <el-tag type="warning" size="small">第{{ $index + 1 }}车</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="订单编号" />
          <el-table-column prop="goodsName" label="货物名称" />
          <el-table-column prop="weight" label="重量(kg)" width="100" />
          <el-table-column prop="volume" label="体积(m³)" width="100" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showLoadingDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLoading">确认装车</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Van, Location } from '@element-plus/icons-vue'
import { dispatchApi, transportApi, orderApi } from '@/api'

const batches = ref([])
const showNodeDialog = ref(false)
const showDetailDialog = ref(false)
const showLoadingDialog = ref(false)
const nodeForm = ref({ nodeName: '', location: '', batchId: '' })
const currentBatch = ref(null)
const batchOrders = ref([])
const loadingOrders = ref([])

const transportingCount = computed(() => batches.value.filter(b => b.status === 'IN_TRANSIT').length)
const delayedCount = computed(() => batches.value.filter(b => b.status === 'EXCEPTION').length)
const todayCompleted = computed(() => batches.value.filter(b => b.status === 'COMPLETED').length)
const onTimeRate = computed(() => batches.value.length ? ((batches.value.filter(b => b.status === 'COMPLETED').length / batches.value.length) * 100) : 100)

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const getNodeType = (index) => {
  const types = ['success', 'warning', 'primary']
  return types[index % types.length]
}

const getNodeHistory = () => {
  if (!currentBatch.value || !currentBatch.value.nodeHistory) {
    return [{ nodeName: '订单创建', location: currentBatch.value?.startAddress || '', arrivalTime: currentBatch.value?.createTime, remark: '运输批次已创建' }]
  }
  const history = [{ nodeName: '订单创建', location: currentBatch.value.startAddress, arrivalTime: currentBatch.value.createTime, remark: '运输批次已创建' }]
  return [...history, ...currentBatch.value.nodeHistory]
}

const loadTransporting = async () => {
  try {
    const res = await dispatchApi.batchList()
    batches.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  }
}

const showDetail = async (batch) => {
  currentBatch.value = batch
  batchOrders.value = []
  if (batch.orderIds && batch.orderIds.length > 0) {
    const allOrders = await orderApi.list()
    batchOrders.value = allOrders.filter(o => batch.orderIds.includes(o.id))
  }
  showDetailDialog.value = true
}

const recordLoading = async (batch) => {
  currentBatch.value = batch
  loadingOrders.value = []
  if (batch.orderIds && batch.orderIds.length > 0) {
    const allOrders = await orderApi.list()
    loadingOrders.value = allOrders.filter(o => batch.orderIds.includes(o.id))
  }
  showLoadingDialog.value = true
}

const submitLoading = async () => {
  try {
    const orderIds = loadingOrders.value.map(o => o.id)
    await transportApi.recordLoading(currentBatch.value.id, orderIds)
    ElMessage.success('装车记录已保存')
    showLoadingDialog.value = false
    loadTransporting()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const acceptBatch = async (batch) => {
  ElMessageBox.confirm(`司机确认接单？`, '提示', { type: 'info' }).then(async () => {
    try {
      await transportApi.accept(batch.id, batch.driverId || 'driver-1')
      ElMessage.success('接单成功')
      loadTransporting()
    } catch (e) {
      ElMessage.error('接单失败')
    }
  }).catch(() => {})
}

const startTransport = async (batch) => {
  try {
    await transportApi.start(batch.id)
    ElMessage.success('运输已开始')
    loadTransporting()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const updateNode = (batch) => {
  currentBatch.value = batch
  nodeForm.value = { nodeName: '', location: '', batchId: batch.id }
  showNodeDialog.value = true
}

const submitNodeUpdate = async () => {
  if (!nodeForm.value.nodeName) {
    ElMessage.warning('请选择节点')
    return
  }
  try {
    await transportApi.updateNode(nodeForm.value)
    ElMessage.success('节点更新成功')
    showNodeDialog.value = false
    loadTransporting()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const completeBatch = async (batch) => {
  ElMessageBox.confirm(`确认完成该运输批次？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await transportApi.complete(batch.id)
      ElMessage.success('运输已完成')
      loadTransporting()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const getStatusType = (status) => {
  const map = { ASSIGNED: 'warning', ACCEPTED: 'info', IN_TRANSIT: 'primary', COMPLETED: 'success', EXCEPTION: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ASSIGNED: '已分配', ACCEPTED: '已接单', IN_TRANSIT: '运输中', COMPLETED: '已完成', EXCEPTION: '异常' }
  return map[status] || status
}

onMounted(() => {
  loadTransporting()
})
</script>

<style scoped>
.transport-page {
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

.mini-stat {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 8px;
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

.eta-text {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
  font-size: 13px;
}

.text-muted {
  color: #94a3b8;
}

.batch-detail {
  padding: 10px 0;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #1e293b;
}

.node-content {
  padding: 4px 0;
}

.node-content strong {
  color: #1e293b;
  font-size: 14px;
}

.node-location {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #64748b;
}

.node-remark {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.loading-tip {
  margin: 0 0 12px 0;
  color: #64748b;
  font-size: 13px;
}
</style>
