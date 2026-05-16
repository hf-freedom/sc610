<template>
  <div class="statistics-page">
    <h2 class="page-title">统计分析</h2>
    
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header><span>核心指标概览</span></template>
          <div ref="overviewChart" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header><span>订单状态分布</span></template>
          <div ref="orderStatusChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>区域订单分布</span></template>
          <div ref="regionChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header><span>车辆利用率趋势</span></template>
          <div ref="vehicleChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>司机准时率排名</span></template>
          <div ref="driverChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'

const overviewChart = ref(null)
const orderStatusChart = ref(null)
const regionChart = ref(null)
const vehicleChart = ref(null)
const driverChart = ref(null)

const overviewData = ref({
  totalOrders: 156,
  completedOrders: 120,
  transportingOrders: 25,
  pendingOrders: 10,
  exceptionOrders: 1,
  totalVehicles: 15,
  availableVehicles: 8,
  busyVehicles: 7,
  totalDrivers: 12,
  availableDrivers: 6,
  vehicleUtilizationRate: 46.7,
  driverOnTimeRate: 94.5,
  routeDelayRate: 8.3
})

const loadCharts = async () => {
  try {
    const res = await statisticsApi.overview()
    if (res && Object.keys(res).length > 0) {
      overviewData.value = { ...overviewData.value, ...res }
    }
  } catch (e) {
    console.error(e)
  }
  await nextTick()
  renderAllCharts()
}

const renderAllCharts = () => {
  renderOverviewChart()
  renderOrderStatusChart()
  renderRegionChart()
  renderVehicleChart()
  renderDriverChart()
}

const renderOverviewChart = () => {
  if (!overviewChart.value) return
  const chart = echarts.init(overviewChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 5,
        axisLine: {
          lineStyle: {
            width: 30,
            color: [[0.3, '#ef4444'], [0.7, '#f59e0b'], [1, '#10b981']]
          }
        },
        pointer: { itemStyle: { color: '#3b82f6' }, length: '60%', width: 8 },
        axisTick: { length: 0 },
        splitLine: { length: 0 },
        axisLabel: { distance: -60, fontSize: 14, color: '#64748b' },
        detail: {
          formatter: '{value}%',
          offsetCenter: [0, -30],
          fontSize: 28,
          fontWeight: 'bold',
          color: '#3b82f6'
        },
        data: [{ value: overviewData.value.vehicleUtilizationRate, name: '车辆利用率' }],
        title: { offsetCenter: [0, 10], fontSize: 16 }
      }
    ]
  })
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
        { value: overviewData.value.pendingOrders, name: '待调度', itemStyle: { color: '#60a5fa' } },
        { value: overviewData.value.transportingOrders, name: '运输中', itemStyle: { color: '#fbbf24' } },
        { value: overviewData.value.completedOrders, name: '已完成', itemStyle: { color: '#34d399' } },
        { value: overviewData.value.exceptionOrders, name: '异常', itemStyle: { color: '#f87171' } }
      ],
      label: { formatter: '{b}: {c} ({d}%)' }
    }]
  })
}

const renderRegionChart = () => {
  if (!regionChart.value) return
  const chart = echarts.init(regionChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['华东区', '华北区', '华南区', '西南区', '华中区'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [45, 32, 38, 25, 16],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#3b82f6' },
          { offset: 1, color: '#1d4ed8' }
        ])
      },
      barWidth: 50
    }]
  })
}

const renderVehicleChart = () => {
  if (!vehicleChart.value) return
  const chart = echarts.init(vehicleChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      type: 'line',
      data: [42, 58, 65, 52, 68, 35, 28],
      smooth: true,
      lineStyle: { width: 3, color: '#3b82f6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
          { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
        ])
      },
      symbol: 'circle',
      symbolSize: 8
    }]
  })
}

const renderDriverChart = () => {
  if (!driverChart.value) return
  const chart = echarts.init(driverChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 80 },
    yAxis: { type: 'category', data: ['司机A', '司机B', '司机C', '司机D', '司机E'] },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      type: 'bar',
      data: [98.5, 96.2, 94.8, 92.5, 89.3],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#10b981' },
          { offset: 1, color: '#059669' }
        ])
      },
      label: { show: true, position: 'right', formatter: '{c}%' },
      barWidth: 30
    }]
  })
}

onMounted(() => {
  loadCharts()
  window.addEventListener('resize', () => {
    // Handle resize
  })
})
</script>

<style scoped>
.statistics-page {
  padding: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
}
</style>
