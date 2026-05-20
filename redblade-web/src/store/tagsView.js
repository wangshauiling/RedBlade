import { defineStore } from 'pinia'

export const useTagsViewStore = defineStore('tagsView', {
  state: () => ({
    // 已访问的页面
    visitedViews: JSON.parse(localStorage.getItem('visitedViews') || '[]'),
    // 缓存的页面
    cachedViews: JSON.parse(localStorage.getItem('cachedViews') || '[]')
  }),

  actions: {
    /**
     * 添加已访问页面
     */
    addVisitedView(view) {
      if (this.visitedViews.some(v => v.path === view.path)) {
        return
      }
      this.visitedViews.push({
        path: view.path,
        name: view.name,
        title: view.meta?.title || 'no-name',
        fullPath: view.fullPath,
        query: view.query
      })
      this.saveVisitedViews()
    },

    /**
     * 添加缓存页面
     */
    addCachedView(view) {
      if (view.name && !this.cachedViews.includes(view.name)) {
        this.cachedViews.push(view.name)
        this.saveCachedViews()
      }
    },

    /**
     * 删除已访问页面
     */
    delVisitedView(view) {
      const index = this.visitedViews.findIndex(v => v.path === view.path)
      if (index > -1) {
        this.visitedViews.splice(index, 1)
        this.saveVisitedViews()
      }
    },

    /**
     * 删除缓存页面
     */
    delCachedView(view) {
      const index = this.cachedViews.indexOf(view.name)
      if (index > -1) {
        this.cachedViews.splice(index, 1)
        this.saveCachedViews()
      }
    },

    /**
     * 关闭其他页面
     */
    delOthersViews(view) {
      this.visitedViews = this.visitedViews.filter(v => v.path === view.path)
      this.cachedViews = view.name ? [view.name] : []
      this.saveVisitedViews()
      this.saveCachedViews()
    },

    /**
     * 关闭所有页面
     */
    delAllViews() {
      this.visitedViews = []
      this.cachedViews = []
      this.saveVisitedViews()
      this.saveCachedViews()
    },

    /**
     * 保存到本地存储
     */
    saveVisitedViews() {
      localStorage.setItem('visitedViews', JSON.stringify(this.visitedViews))
    },

    saveCachedViews() {
      localStorage.setItem('cachedViews', JSON.stringify(this.cachedViews))
    }
  }
})