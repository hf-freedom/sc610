<template>
  <div class="order-page">
    <div class="page-header">
      <h2>订单管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建订单
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 150px;">
            <el-option label="全部" value="" />
            <el-option label="待调度" value="PENDING" />
            <el-option label="已分配" value="ASSIGNED" />
            <el-option label="运输中" value="TRANSPORTING" />
            <el-option label="已完成" value="COMPLETED" />
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
          <el-button type="primary" @click="loadOrders">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="filteredOrders" stripe border style="width: 100%;" v-loading="loading">
        <el-table-column prop="orderNo" label="订单编号" width="140" />
        <el-table-column prop="goodsName" label="货物名称" />
        <el-table-column prop="weight" label="重量(kg)" width="100">
          <template #default="{ row }">{{ row.weight?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="volume" label="体积(m³)" width="100">
          <template #default="{ row }">{{ row.volume?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="startAddress" label="发货地" />
        <el-table-column prop="endAddress" label="收货地" />
        <el-table-column prop="region" label="区域" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link>详情</el-button>
            <el-button size="small" type="danger" link @click="deleteOrder(row.id)" v-if="row.status === 'PENDING'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="新建订单" width="600px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="货物名称" required>
          <el-input v-model="orderForm.goodsName" placeholder="请输入货物名称" />
        </el-form-item>
        <el-form-item label="重量(kg)" required>
          <el-input-number v-model="orderForm.weight" :min="0" :step="10" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="体积(m³)" required>
          <el-input-number v-model="orderForm.volume" :min="0" :step="0.1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="发货地" required>
          <el-input v-model="orderForm.startAddress" placeholder="请输入发货地" />
        </el-form-item>
        <el-form-item label="收货地" required>
          <el-input v-model="orderForm.endAddress" placeholder="请输入收货地" />
        </el-form-item>
        <el-form-item label="所属区域" required>
          <el-select v-model="orderForm.region" placeholder="请选择区域" style="width: 100%;">
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
        <el-button type="primary" @click="createOrder">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api'

const orders = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)
const filterForm = ref({ status: '', region: '' })
const orderForm = ref({
  goodsName: '',
  weight: 100,
  volume: 1,
  startAddress: '',
  endAddress: '',
  region: ''
})

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    if (filterForm.value.status && order.status !== filterForm.value.status) return false
    if (filterForm.value.region && order.region !== filterForm.value.region) return false
    return true
  })
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.list()
    orders.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const createOrder = async () => {
  if (!orderForm.value.goodsName || !orderForm.value.startAddress || !orderForm.value.endAddress || !orderForm.value.region) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await orderApi.create(orderForm.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    orderForm.value = { goodsName: '', weight: 100, volume: 1, startAddress: '', endAddress: '', region: '' }
    loadOrders()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

const deleteOrder = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该订单吗？', '提示', { type: 'warning' })
    await orderApi.delete(id)
    ElMessage.success('删除成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const resetFilter = () => {
  filterForm.value = { status: '', region: '' }
  loadOrders()
}

const getStatusType = (status) => {
  const map = { PENDING: 'info', ASSIGNED: 'warning', TRANSPORTING: 'primary', COMPLETED: 'success', EXCEPTION: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待调度', ASSIGNED: '已分配', TRANSPORTING: '运输中', COMPLETED: '已完成', EXCEPTION: '异常' }
  return map[status] || status
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-page {
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
