<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="28"><Van /></el-icon>
        <span>物流调度系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#3b82f6"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/vehicle">
          <el-icon><Van /></el-icon>
          <span>车辆管理</span>
        </el-menu-item>
        <el-menu-item index="/driver">
          <el-icon><User /></el-icon>
          <span>司机管理</span>
        </el-menu-item>
        <el-menu-item index="/dispatch">
          <el-icon><Operation /></el-icon>
          <span>智能调度</span>
        </el-menu-item>
        <el-menu-item index="/transport">
          <el-icon><Position /></el-icon>
          <span>运输监控</span>
        </el-menu-item>
        <el-menu-item index="/warning">
          <el-icon><Warning /></el-icon>
          <span>异常预警</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>统计分析</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">
          <span>物流运力调度管理系统</span>
        </div>
        <div class="header-user">
          <el-avatar :size="32" icon="UserFilled" />
          <span>管理员</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #1e293b;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  gap: 8px;
  border-bottom: 1px solid #334155;
}

.header {
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  color: white;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.main-content {
  background-color: #f1f5f9;
  padding: 24px;
  overflow-y: auto;
}

.el-menu {
  border: none;
}
</style>
