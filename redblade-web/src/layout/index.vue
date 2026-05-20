<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <Sidebar class="sidebar" />

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <Navbar class="navbar" />

      <!-- 标签页导航 -->
      <TagsView class="tags-view" />

      <!-- 内容区 -->
      <AppMain class="app-main" />
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import TagsView from './components/TagsView.vue'
import AppMain from './components/AppMain.vue'
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  width: 100%;
  height: 100vh;
}

.sidebar {
  width: 210px;
  height: 100%;
  background: #304156;
  transition: width 0.3s;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
}

.tags-view {
  height: 34px;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
}

.app-main {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background: #f0f2f5;
}
</style>