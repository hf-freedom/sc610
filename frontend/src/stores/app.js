import { defineStore } from 'pinia'
import { ref } from 'vue'
import { orderApi, vehicleApi, driverApi, statisticsApi } from '@/api'

export const useAppStore = defineStore('app', () => {
  const orders = ref([])
  const vehicles = ref([])
  const drivers = ref([])
  const statistics = ref({})

  const fetchOrders = async () => {
    const res = await orderApi.list()
    orders.value = Array.isArray(res) ? res : []
  }

  const fetchVehicles = async () => {
    const res = await vehicleApi.list()
    vehicles.value = Array.isArray(res) ? res : []
  }

  const fetchDrivers = async () => {
    const res = await driverApi.list()
    drivers.value = Array.isArray(res) ? res : []
  }

  const fetchStatistics = async () => {
    const res = await statisticsApi.overview()
    statistics.value = res || {}
  }

  return {
    orders,
    vehicles,
    drivers,
    statistics,
    fetchOrders,
    fetchVehicles,
    fetchDrivers,
    fetchStatistics
  }
})
