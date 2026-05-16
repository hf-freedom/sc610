# 物流运力调度系统 技术架构文档

## 1. 技术选型

### 1.1 前端技术栈
- **框架**: Vue.js 3.x
- **构建工具**: Vite
- **路由**: Vue Router 4.x
- **状态管理**: Pinia
- **UI组件库**: Element Plus
- **HTTP客户端**: Axios
- **图表库**: ECharts

### 1.2 后端技术栈
- **框架**: Spring Boot 2.7.x
- **Java版本**: Java 8
- **构建工具**: Maven
- **数据存储**: 本地内存（ConcurrentHashMap）
- **定时任务**: Spring Scheduler
- **跨域配置**: WebMvcConfigurer

### 1.3 端口配置
- 后端服务端口: 8003
- 前端服务端口: 3003

## 2. 系统架构

### 2.1 整体架构
```
┌─────────────────┐
│   前端 (Vue3)   │
│  - Element Plus │
│  - Vue Router   │
│  - Pinia        │
│  - Axios        │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────┐
│  后端(SpringBoot)│
│  - Controller   │
│  - Service      │
│  - Model        │
│  - In-Memory DB │
└─────────────────┘
```

## 3. 后端架构设计

### 3.1 项目结构
```
backend/
├── src/main/java/com/logistics/dispatch/
│   ├── DispatchApplication.java        # 启动类
│   ├── config/                          # 配置类
│   │   ├── CorsConfig.java             # 跨域配置
│   │   └── SchedulerConfig.java        # 定时任务配置
│   ├── model/                           # 数据模型
│   │   ├── Order.java                  # 订单
│   │   ├── Vehicle.java                # 车辆
│   │   ├── Driver.java                 # 司机
│   │   ├── TransportBatch.java         # 运输批次
│   │   ├── TransportNode.java          # 运输节点
│   │   ├── Warning.java                # 预警
│   │   └── Statistics.java             # 统计
│   ├── service/                         # 业务逻辑层
│   │   ├── OrderService.java           # 订单服务
│   │   ├── VehicleService.java         # 车辆服务
│   │   ├── DriverService.java          # 司机服务
│   │   ├── DispatchService.java        # 调度服务
│   │   ├── TransportService.java       # 运输服务
│   │   ├── WarningService.java         # 预警服务
│   │   └── StatisticsService.java      # 统计服务
│   ├── controller/                      # 控制层
│   │   ├── OrderController.java
│   │   ├── VehicleController.java
│   │   ├── DriverController.java
│   │   ├── DispatchController.java
│   │   ├── TransportController.java
│   │   ├── WarningController.java
│   │   └── StatisticsController.java
│   └── repository/                      # 数据存储层（内存）
│       ├── OrderRepository.java
│       ├── VehicleRepository.java
│       ├── DriverRepository.java
│       ├── BatchRepository.java
│       └── WarningRepository.java
└── pom.xml
```

### 3.2 核心数据模型
```
Order:
- id: 订单ID
- orderNo: 订单编号
- goodsName: 货物名称
- weight: 重量
- volume: 体积
- startAddress: 起始地址
- endAddress: 目的地址
- region: 区域
- status: 状态（待调度/已分配/运输中/已完成/异常）
- createTime: 创建时间
- expectedArrivalTime: 预计到达时间
- loadingOrder: 装车顺序

Vehicle:
- id: 车辆ID
- plateNumber: 车牌号
- model: 车型
- maxWeight: 最大载重
- maxVolume: 最大容积
- region: 所属区域
- status: 状态（可用/已锁定/运输中/维护中）
- currentLoadWeight: 当前载重
- currentLoadVolume: 当前容积

Driver:
- id: 司机ID
- name: 姓名
- phone: 电话
- status: 状态（空闲/接单中/运输中）
- onTimeRate: 准时率

TransportBatch:
- id: 批次ID
- batchNo: 批次编号
- vehicleId: 车辆ID
- driverId: 司机ID
- orderIds: 订单ID列表
- route: 路线
- status: 状态
- createTime: 创建时间
```

## 4. 前端架构设计

### 4.1 项目结构
```
frontend/
├── src/
│   ├── main.js                       # 入口文件
│   ├── App.vue                       # 根组件
│   ├── router/index.js               # 路由配置
│   ├── store/                        # Pinia状态管理
│   │   ├── order.js
│   │   ├── vehicle.js
│   │   ├── driver.js
│   │   └── statistics.js
│   ├── api/                          # API接口
│   │   ├── request.js                # Axios封装
│   │   ├── order.js
│   │   ├── vehicle.js
│   │   ├── driver.js
│   │   ├── dispatch.js
│   │   └── statistics.js
│   ├── views/                        # 页面组件
│   │   ├── Dashboard/                # 仪表盘
│   │   ├── Order/                    # 订单管理
│   │   ├── Vehicle/                  # 车辆管理
│   │   ├── Driver/                   # 司机管理
│   │   ├── Dispatch/                 # 调度中心
│   │   ├── Transport/                # 运输监控
│   │   ├── Warning/                  # 异常预警
│   │   └── Statistics/               # 统计分析
│   ├── components/                   # 公共组件
│   │   ├── Layout/                   # 布局组件
│   │   └── Common/                   # 通用组件
│   └── utils/                        # 工具函数
└── package.json
```

### 4.2 路由规划
| 路径 | 页面 | 说明 |
|------|------|------|
| /dashboard | 仪表盘 | 数据概览 |
| /order | 订单管理 | 订单列表、创建、详情 |
| /vehicle | 车辆管理 | 车辆列表、状态管理 |
| /driver | 司机管理 | 司机列表、信息管理 |
| /dispatch | 调度中心 | 订单调度、批次管理 |
| /transport | 运输监控 | 实时运输状态 |
| /warning | 异常预警 | 异常订单、预警列表 |
| /statistics | 统计分析 | 各类统计报表 |

## 5. 核心业务流程

### 5.1 订单创建流程
1. 用户填写订单信息
2. 系统计算重量、体积
3. 订单入库，状态：待调度

### 5.2 智能调度流程
1. 获取待调度订单
2. 根据区域、车型、容量筛选可用车辆
3. 同路线订单合并
4. 创建运输批次
5. 锁定车辆状态

### 5.3 运输执行流程
1. 司机接单 → 锁定车辆和订单
2. 装车 → 记录装车顺序
3. 节点更新 → 计算预计到达时间
4. 超时检测 → 生成延误预警
5. 完成运输 → 恢复车辆状态

### 5.4 定时任务流程
1. 每5分钟检查运输中订单
2. 对比当前时间与预计到达时间
3. 超时未更新则生成预警
4. 预警通知管理员

## 6. API接口设计

### 6.1 订单接口
- POST /api/order - 创建订单
- GET /api/order - 获取订单列表
- GET /api/order/{id} - 获取订单详情
- PUT /api/order/{id}/status - 更新订单状态

### 6.2 车辆接口
- POST /api/vehicle - 添加车辆
- GET /api/vehicle - 获取车辆列表
- GET /api/vehicle/available - 获取可用车辆
- PUT /api/vehicle/{id}/status - 更新车辆状态

### 6.3 调度接口
- POST /api/dispatch/match - 智能匹配车辆
- POST /api/dispatch/merge - 合并订单批次
- POST /api/dispatch/assign - 分配司机

### 6.4 运输接口
- POST /api/transport/accept - 司机接单
- POST /api/transport/loading - 装车确认
- POST /api/transport/node - 更新运输节点
- POST /api/transport/complete - 完成运输

### 6.5 统计接口
- GET /api/statistics/vehicle-utilization - 车辆利用率
- GET /api/statistics/driver-ontime - 司机准时率
- GET /api/statistics/route-delay - 线路延误率
