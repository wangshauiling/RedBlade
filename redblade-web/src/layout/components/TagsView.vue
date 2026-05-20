<template>
  <div class="tags-view-container">
    <el-scrollbar class="tags-scroll">
      <div class="tags-wrapper">
        <router-link
          v-for="tag in visitedViews"
          :key="tag.path"
          :to="tag.path"
          class="tag-item"
          :class="{ active: isActive(tag) }"
        >
          {{ tag.title }}
          <el-icon
            v-if="!isAffix(tag)"
            class="close-icon"
            @click.prevent.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>

    <!-- 右侧操作按钮 -->
    <div class="tags-actions">
      <el-dropdown trigger="click">
        <el-icon class="action-icon"><ArrowDown /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="refreshPage">刷新当前页</el-dropdown-item>
            <el-dropdown-item @click="closeOtherTags">关闭其他</el-dropdown-item>
            <el-dropdown-item @click="closeAllTags">关闭所有</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTagsViewStore } from '@/store/tagsView'

const route = useRoute()
const router = useRouter()
const tagsViewStore = useTagsViewStore()

const visitedViews = computed(() => tagsViewStore.visitedViews)

// 是否当前激活
const isActive = (tag) => {
  return tag.path === route.path
}

// 是否固定标签
const isAffix = (tag) => {
  return route.matched.some(r => r.path === tag.path && r.meta?.affix)
}

// 关闭标签
const closeTag = (tag) => {
  tagsViewStore.delVisitedView(tag)
  tagsViewStore.delCachedView(tag)

  // 如果关闭的是当前页面，跳转到上一个标签
  if (isActive(tag)) {
    const lastTag = visitedViews.value[visitedViews.value.length - 1]
    if (lastTag) {
      router.push(lastTag.path)
    } else {
      router.push('/home')
    }
  }
}

// 刷新页面
const refreshPage = () => {
  tagsViewStore.delCachedView(route)
  router.replace({ path: '/redirect' + route.path })
}

// 关闭其他标签
const closeOtherTags = () => {
  tagsViewStore.delOthersViews(route)
}

// 关闭所有标签
const closeAllTags = () => {
  tagsViewStore.delAllViews()
  router.push('/home')
}

// 监听路由变化，添加标签
watch(
  () => route.path,
  () => {
    if (route.name && !route.meta?.hidden) {
      tagsViewStore.addVisitedView(route)
      tagsViewStore.addCachedView(route)
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.tags-view-container {
  display: flex;
  align-items: center;
  padding: 0 10px;
  height: 100%;
}

.tags-scroll {
  flex: 1;
  overflow: hidden;
}

.tags-wrapper {
  display: flex;
  align-items: center;
  white-space: nowrap;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 10px;
  margin-right: 5px;
  font-size: 12px;
  color: #495060;
  background: #fff;
  border: 1px solid #d8dce5;
  border-radius: 3px;
  text-decoration: none;
  cursor: pointer;

  &:hover {
    color: #409eff;
  }

  &.active {
    color: #fff;
    background: #409eff;
    border-color: #409eff;
  }

  .close-icon {
    margin-left: 5px;
    font-size: 12px;
    border-radius: 50%;

    &:hover {
      background: #666;
      color: #fff;
    }
  }
}

.tags-actions {
  margin-left: 10px;

  .action-icon {
    font-size: 16px;
    cursor: pointer;
  }
}
</style>