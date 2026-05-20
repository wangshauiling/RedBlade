<template>
  <div class="app-main-container">
    <router-view v-slot="{ Component }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="key" />
        </keep-alive>
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useTagsViewStore } from '@/store/tagsView'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

const key = computed(() => route.path)
const cachedViews = computed(() => tagsViewStore.cachedViews)
</script>

<style lang="scss" scoped>
.app-main-container {
  width: 100%;
  height: 100%;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>