import request from './request'

export const orderApi = {
  list: () => request.get('/order'),
  getById: (id) => request.get(`/order/${id}`),
  create: (data) => request.post('/order', data),
  updateStatus: (id, status) => request.put(`/order/${id}/status`, { status }),
  delete: (id) => request.delete(`/order/${id}`)
}

export const vehicleApi = {
  list: () => request.get('/vehicle'),
  getById: (id) => request.get(`/vehicle/${id}`),
  available: (region, weight, volume) => request.get('/vehicle/available', { params: { region, weight, volume } }),
  create: (data) => request.post('/vehicle', data),
  updateStatus: (id, status) => request.put(`/vehicle/${id}/status`, { status }),
  delete: (id) => request.delete(`/vehicle/${id}`)
}

export const driverApi = {
  list: () => request.get('/driver'),
  getById: (id) => request.get(`/driver/${id}`),
  available: () => request.get('/driver/status/AVAILABLE'),
  create: (data) => request.post('/driver', data),
  updateStatus: (id, status) => request.put(`/driver/${id}/status`, { status }),
  delete: (id) => request.delete(`/driver/${id}`)
}

export const dispatchApi = {
  match: (region, weight, volume) => request.get('/dispatch/match', { params: { region, weight, volume } }),
  merge: (data) => request.post('/dispatch/merge', data),
  batchList: () => request.get('/dispatch/batch'),
  batchListByStatus: (status) => request.get(`/dispatch/batch/status/${status}`)
}

export const transportApi = {
  accept: (batchId, driverId) => request.post('/transport/accept', { batchId, driverId }),
  start: (batchId) => request.post('/transport/start', { batchId }),
  complete: (batchId) => request.post('/transport/complete', { batchId }),
  updateNode: (data) => request.post('/transport/node', data),
  recordLoading: (batchId, orderIds) => request.post('/transport/loading', { batchId, orderIds }),
  transporting: () => request.get('/transport/transporting')
}

export const warningApi = {
  list: () => request.get('/warning'),
  getDetail: (id) => request.get(`/warning/${id}/detail`),
  handle: (id, handler, remark) => request.put(`/warning/${id}/handle`, { handler, remark }),
  reschedule: (id, vehicleId, driverId, handler) => request.post(`/warning/${id}/reschedule`, { vehicleId, driverId, handler })
}

export const statisticsApi = {
  overview: () => request.get('/statistics/overview'),
  orderStatus: () => request.get('/statistics/order-status'),
  region: () => request.get('/statistics/region')
}
