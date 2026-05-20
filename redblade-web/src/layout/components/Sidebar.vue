<template>
  <div class="sidebar-container">
    <!-- Logo -->
    <div class="sidebar-logo">
      <img src="@/assets/logo.svg" alt="logo" />
      <h1 v-show="!collapsed">RedBlade</h1>
    </div>

    <!-- 菜单 -->
    <el-scrollbar class="sidebar-menu">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :unique-opened="true"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <template v-for="route in routes" :key="route.path">
          <!-- 没有子菜单 -->
          <el-menu-item
            v-if="!route.children && !route.meta?.hidden"
            :index="route.path"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>

          <!-- 有子菜单 -->
          <el-sub-menu
            v-else-if="route.children && !route.meta?.hidden"
            :index="route.path"
          >
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>

            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="`${route.path}/${child.path}`"
              v-show="!child.meta?.hidden"
            >
              <el-icon v-if="child.meta?.icon">
                <component :is="child.meta.icon" />
              </el-icon>
              <template #title>{{ child.meta?.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { constantRoutes, asyncRoutes } from '@/router'

const route = useRoute()
const appStore = useAppStore()

const collapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const routes = computed(() => [...constantRoutes.filter(r => r.component), ...asyncRoutes])
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b2f3a;
  overflow: hidden;

  img {
    width: 32px;
    height: 32px;
  }

  h1 {
    margin-left: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.sidebar-menu {
  flex: 1;
  overflow: hidden;

  :deep(.el-menu) {
    border-right: none;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
  }

  :deep(.el-menu-item.is-active) {
    background-color: #1f2d3d !important;
  }

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background-color: #263445 !important;
  }
}
</style>