<template>
  <div class="dashboard">
    <h2 class="page-title">数据概览</h2>
    
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon order-icon">
            <el-icon size="28"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon vehicle-icon">
            <el-icon size="28"><Van /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalVehicles || 0 }}</div>
            <div class="stat-label">总车辆数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon driver-icon">
            <el-icon size="28"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalDrivers || 0 }}</div>
            <div class="stat-label">总司机数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon transport-icon">
            <el-icon size="28"><Position /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.transportingOrders || 0 }}</div>
            <div class="stat-label">运输中订单</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="12">
        <el-card class="mini-chart-card">
          <div class="stat-header">
            <h3>订单状态分布</h3>
          </div>
          <div ref="orderStatusChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="mini-chart-card">
          <div class="stat-header">
            <h3>车辆利用率</h3>
            <div class="stat-percent">{{ statistics.vehicleUtilizationRate || 0 }}%</div>
          </div>
          <div ref="vehicleUtilChart" style="height: 250px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 24px;">
      <el-col :span="8">
        <el-card class="mini-chart-card">
          <div class="stat-header">
            <h3>准时率</h3>
            <div class="stat-percent">{{ statistics.driverOnTimeRate || 0 }}%</div>
          </div>
          <div ref="onTimeChart" style="height: 250px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="mini-chart-card">
          <div class="stat-header">
            <h3>延误率</h3>
            <div class="stat-percent danger">{{ statistics.routeDelayRate || 0 }}%</div>
          </div>
          <div ref="delayChart" style="height: 250px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="mini-chart-card">
          <div class="stat-header">
            <h3>车辆状态</h3>
            <div class="stat-row">
              <span class="stat-text">可用: <span class="success">{{ statistics.availableVehicles || 0 }}</span></span>
              <span class="stat-text">忙碌: <span class="warning">{{ statistics.busyVehicles || 0 }}</span></span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import * as echarts from 'echarts'

const store = useAppStore()
const statistics = ref({})
const orderStatusChart = ref(null)
const vehicleUtilChart = ref(null)
const onTimeChart = ref(null)
const delayChart = ref(null)

const loadData = async () => {
  await store.fetchStatistics()
  statistics.value = store.statistics
  
  renderOrderStatusChart()
  renderVehicleUtilChart()
  renderOnTimeChart()
  renderDelayChart()
}

const renderOrderStatusChart = () => {
  if (!orderStatusChart.value) return
  const chart = echarts.init(orderStatusChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: statistics.value.pendingOrders || 0, name: '待调度', itemStyle: { color: '#60a5fa' } },
        { value: statistics.value.transportingOrders || 0, name: '运输中', itemStyle: { color: '#fbbf24' } },
        { value: statistics.value.completedOrders || 0, name: '已完成', itemStyle: { color: '#34d399' } },
        { value: statistics.value.exceptionOrders || 0, name: '异常', itemStyle: { color: '#f87171' } }
      ]
    }]
  })
}

const renderVehicleUtilChart = () => {
  if (!vehicleUtilChart.value) return
  const chart = echarts.init(vehicleUtilChart.value)
  const rate = statistics.value.vehicleUtilizationRate || 0
  chart.setOption({
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 5,
      axisLine: {
        lineStyle: { width: 20, color: [[0.3, '#f87171'], [0.7, '#fbbf24'], [1, '#34d399']] }
      },
      pointer: { itemStyle: { color: '#3b82f6' }, length: '60%', width: 6 },
      axisTick: { length: 0 },
      splitLine: { length: 0 },
      axisLabel: { show: false },
      detail: { formatter: '{value}%', offsetCenter: [0, -20], fontSize: 24, fontWeight: 'bold' },
      data: [{ value: rate }]
    }]
  })
}

const renderOnTimeChart = () => {
  if (!onTimeChart.value) return
  const chart = echarts.init(onTimeChart.value)
  const rate = statistics.value.driverOnTimeRate || 0
  chart.setOption({
    series: [{
      type: 'bar',
      data: [{ name: '准时率', value: rate, itemStyle: { color: '#3b82f6' } }],
      barWidth: 40,
      label: { show: true, position: 'top', formatter: '{c}%' }
    }],
    xAxis: { type: 'category', data: ['准时率'], axisLabel: { show: false } },
    yAxis: { max: 100, type: 'value', axisLabel: { formatter: '{value}%' } },
    grid: { left: 40, right: 20, top: 40, bottom: 30 }
  })
}

const renderDelayChart = () => {
  if (!delayChart.value) return
  const chart = echarts.init(delayChart.value)
  const rate = statistics.value.routeDelayRate || 0
  chart.setOption({
    series: [{
      type: 'bar',
      data: [{ name: '延误率', value: rate, itemStyle: { color: rate > 10 ? '#f87171' : '#fbbf24' } }],
      barWidth: 40,
      label: { show: true, position: 'top', formatter: '{c}%' }
    }],
    xAxis: { type: 'category', data: ['延误率'], axisLabel: { show: false } },
    yAxis: { max: 100, type: 'value', axisLabel: { formatter: '{value}%' } },
    grid: { left: 40, right: 20, top: 40, bottom: 30 }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 24px;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.order-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #3b82f6;
}

.vehicle-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #10b981;
}

.driver-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #f59e0b;
}

.transport-icon {
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  color: #6366f1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1e293b;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.mini-chart-card {
  height: 100%;
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stat-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.stat-percent {
  font-size: 24px;
  font-weight: bold;
  color: #3b82f6;
}

.stat-percent.danger {
  color: #f87171;
}

.stat-row {
  display: flex;
  gap: 24px;
}

.success {
  color: #10b981;
  font-size: 18px;
  font-weight: 600;
}

.warning {
  color: #f59e0b;
  font-size: 18px;
  font-weight: 600;
}
</style>
