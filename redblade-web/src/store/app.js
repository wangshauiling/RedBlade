import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // 侧边栏是否折叠
    sidebarCollapsed: localStorage.getItem('sidebarCollapsed') === 'true' || false,
    // 当前激活的菜单
    activeMenu: localStorage.getItem('activeMenu') || '/home',
    // 设备类型
    device: 'desktop'
  }),

  getters: {
    sidebarWidth: (state) => state.sidebarCollapsed ? '64px' : '210px'
  },

  actions: {
    /**
     * 切换侧边栏折叠状态
     */
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      localStorage.setItem('sidebarCollapsed', this.sidebarCollapsed)
    },

    /**
     * 设置侧边栏折叠状态
     */
    setSidebarCollapsed(collapsed) {
      this.sidebarCollapsed = collapsed
      localStorage.setItem('sidebarCollapsed', collapsed)
    },

    /**
     * 设置当前激活菜单
     */
    setActiveMenu(menu) {
      this.activeMenu = menu
      localStorage.setItem('activeMenu', menu)
    },

    /**
     * 设置设备类型
     */
    setDevice(device) {
      this.device = device
      if (device === 'mobile') {
        this.sidebarCollapsed = true
      }
    }
  }
})