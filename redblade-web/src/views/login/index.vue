<template>
  <div class="login-container">
    <!-- 左侧背景区域 -->
    <div class="login-left">
      <div class="login-bg-content">
        <div class="logo-wrapper">
          <img src="@/assets/logo.svg" alt="logo" class="bg-logo" />
          <span class="logo-text">RedBlade</span>
        </div>
        <h2>企业级智能管理平台</h2>
        <p>Enterprise Intelligent Management Platform</p>
        <div class="tech-lines">
          <div class="line"></div>
          <div class="line"></div>
          <div class="line"></div>
        </div>
        <div class="features">
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="feature-text">
              <span class="title">多组织架构</span>
              <span class="desc">数据隔离 安全可靠</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="feature-text">
              <span class="title">权限管控</span>
              <span class="desc">细粒度 灵活配置</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><Globe /></el-icon>
            </div>
            <div class="feature-text">
              <span class="title">国际化</span>
              <span class="desc">多语言 全球部署</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="icon-box">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="feature-text">
              <span class="title">模型驱动</span>
              <span class="desc">低代码 高效开发</span>
            </div>
          </div>
        </div>
      </div>
      <!-- 科技感装饰 -->
      <div class="tech-decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
        <div class="dot dot-1"></div>
        <div class="dot dot-2"></div>
        <div class="dot dot-3"></div>
      </div>
    </div>

    <!-- 右侧登录区域 -->
    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h1>账号登录</h1>
          <p>请输入您的账号信息</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
        >
          <el-form-item prop="orgCode">
            <el-input
              v-model="loginForm.orgCode"
              placeholder="组织编码"
              prefix-icon="OfficeBuilding"
            />
          </el-form-item>

          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="captcha" v-if="captchaEnabled">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captcha"
                placeholder="验证码"
                prefix-icon="Picture"
                @keyup.enter="handleLogin"
              />
              <img
                :src="captchaImg"
                alt="验证码"
                class="captcha-img"
                @click="getCaptchaImage"
              />
            </div>
          </el-form-item>

          <el-form-item class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goRegister">立即注册</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const captchaEnabled = ref(false)
const captchaImg = ref('')

const loginForm = reactive({
  orgCode: '',
  username: '',
  password: '',
  captcha: '',
  uuid: '',
  rememberMe: false
})

const loginRules = {
  orgCode: [
    { required: true, message: '请输入组织编码', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码长度不能少于4位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 获取记住的登录信息
const getRememberedLoginInfo = () => {
  const remembered = localStorage.getItem('rb_rememberMe')
  if (remembered === 'true') {
    loginForm.orgCode = localStorage.getItem('rb_rememberOrgCode') || ''
    loginForm.username = localStorage.getItem('rb_rememberUsername') || ''
    loginForm.password = localStorage.getItem('rb_rememberPassword') || ''
    loginForm.rememberMe = true
  }
}

// 保存登录信息
const saveLoginInfo = () => {
  if (loginForm.rememberMe) {
    localStorage.setItem('rb_rememberMe', 'true')
    localStorage.setItem('rb_rememberOrgCode', loginForm.orgCode)
    localStorage.setItem('rb_rememberUsername', loginForm.username)
    localStorage.setItem('rb_rememberPassword', loginForm.password)
  } else {
    localStorage.removeItem('rb_rememberMe')
    localStorage.removeItem('rb_rememberOrgCode')
    localStorage.removeItem('rb_rememberUsername')
    localStorage.removeItem('rb_rememberPassword')
  }
}

// 获取验证码
const getCaptchaImage = async () => {
  try {
    const res = await getCaptcha()
    const { data } = res
    captchaImg.value = data.img
    loginForm.uuid = data.uuid
    captchaEnabled.value = true
  } catch (error) {
    captchaEnabled.value = false
  }
}

// 登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        saveLoginInfo()

        await userStore.loginAction(loginForm)
        ElMessage.success('登录成功')

        await userStore.getUserInfoAction()

        const redirect = route.query.redirect || '/home'
        router.replace(redirect)
      } catch (error) {
        console.error('登录失败', error)
        if (captchaEnabled.value) {
          getCaptchaImage()
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const goRegister = () => {
  router.push('/register')
}

onMounted(() => {
  getRememberedLoginInfo()
})
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  width: 100%;
  height: 100vh;
  background: #0a1628;
  overflow: hidden;
}

// 左侧背景区域 - 科技感深色
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  position: relative;
  overflow: hidden;

  // 网格背景
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image:
      linear-gradient(rgba(64, 158, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(64, 158, 255, 0.03) 1px, transparent 1px);
    background-size: 50px 50px;
    animation: gridMove 20s linear infinite;
  }

  @keyframes gridMove {
    0% { transform: translate(0, 0); }
    100% { transform: translate(50px, 50px); }
  }

  .login-bg-content {
    position: relative;
    z-index: 2;
    text-align: center;
    color: #fff;
    padding: 40px;

    .logo-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 15px;
      margin-bottom: 20px;

      .bg-logo {
        width: 50px;
        height: 50px;
        filter: drop-shadow(0 0 20px rgba(64, 158, 255, 0.5));
      }

      .logo-text {
        font-size: 28px;
        font-weight: 700;
        background: linear-gradient(90deg, #409EFF, #67C23A);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        letter-spacing: 2px;
      }
    }

    h2 {
      font-size: 26px;
      font-weight: 500;
      margin-bottom: 8px;
      color: #e0e6ed;
    }

    p {
      font-size: 13px;
      color: #8b9eb3;
      letter-spacing: 3px;
      margin-bottom: 30px;
    }

    .tech-lines {
      display: flex;
      justify-content: center;
      gap: 8px;
      margin-bottom: 40px;

      .line {
        width: 40px;
        height: 3px;
        background: linear-gradient(90deg, transparent, #409EFF, transparent);
        border-radius: 2px;
        animation: lineGlow 2s ease-in-out infinite;

        &:nth-child(2) { animation-delay: 0.3s; }
        &:nth-child(3) { animation-delay: 0.6s; }
      }
    }

    @keyframes lineGlow {
      0%, 100% { opacity: 0.3; transform: scaleX(0.8); }
      50% { opacity: 1; transform: scaleX(1.2); }
    }

    .features {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 15px;
      max-width: 360px;
      margin: 0 auto;

      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 15px;
        background: rgba(64, 158, 255, 0.08);
        border: 1px solid rgba(64, 158, 255, 0.15);
        border-radius: 8px;
        transition: all 0.3s;

        &:hover {
          background: rgba(64, 158, 255, 0.15);
          border-color: rgba(64, 158, 255, 0.3);
          transform: translateY(-2px);
        }

        .icon-box {
          width: 36px;
          height: 36px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.2));
          border-radius: 8px;

          .el-icon {
            font-size: 18px;
            color: #409EFF;
          }
        }

        .feature-text {
          text-align: left;

          .title {
            display: block;
            font-size: 13px;
            font-weight: 500;
            color: #e0e6ed;
          }

          .desc {
            display: block;
            font-size: 11px;
            color: #8b9eb3;
            margin-top: 2px;
          }
        }
      }
    }
  }

  // 科技感装饰
  .tech-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;

    .circle {
      position: absolute;
      border-radius: 50%;
      border: 1px solid rgba(64, 158, 255, 0.1);

      &.circle-1 {
        width: 400px;
        height: 400px;
        top: -100px;
        left: -100px;
        animation: pulse 4s ease-in-out infinite;
      }

      &.circle-2 {
        width: 300px;
        height: 300px;
        bottom: -50px;
        right: -50px;
        animation: pulse 5s ease-in-out infinite 1s;
      }

      &.circle-3 {
        width: 200px;
        height: 200px;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        animation: pulse 3s ease-in-out infinite 0.5s;
      }
    }

    .dot {
      position: absolute;
      width: 6px;
      height: 6px;
      background: #409EFF;
      border-radius: 50%;
      box-shadow: 0 0 10px #409EFF;

      &.dot-1 { top: 20%; left: 10%; animation: dotFloat 3s ease-in-out infinite; }
      &.dot-2 { top: 70%; left: 20%; animation: dotFloat 4s ease-in-out infinite 1s; }
      &.dot-3 { top: 40%; right: 15%; animation: dotFloat 3.5s ease-in-out infinite 0.5s; }
    }
  }

  @keyframes pulse {
    0%, 100% { opacity: 0.3; transform: scale(1); }
    50% { opacity: 0.6; transform: scale(1.05); }
  }

  @keyframes dotFloat {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-20px); }
  }
}

// 右侧登录区域
.login-right {
  width: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #0d1a2d 0%, #0a1628 100%);
  border-left: 1px solid rgba(64, 158, 255, 0.1);
  position: relative;

  // 右侧装饰线
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 2px;
    background: linear-gradient(180deg, transparent, #409EFF, transparent);
  }

  .login-box {
    width: 100%;
    max-width: 340px;
    padding: 30px;
  }

  .login-header {
    margin-bottom: 30px;
    text-align: center;

    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #e0e6ed;
      margin-bottom: 8px;
    }

    p {
      font-size: 13px;
      color: #8b9eb3;
    }
  }

  .login-form {
    :deep(.el-input__wrapper) {
      background: rgba(64, 158, 255, 0.05);
      border: 1px solid rgba(64, 158, 255, 0.15);
      box-shadow: none;
      transition: all 0.3s;

      &:hover {
        border-color: rgba(64, 158, 255, 0.3);
      }

      &.is-focus {
        border-color: #409EFF;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
      }
    }

    :deep(.el-input__inner) {
      color: #e0e6ed;

      &::placeholder {
        color: #5a6d82;
      }
    }

    :deep(.el-input__prefix) {
      color: #409EFF;
    }

    :deep(.el-checkbox__label) {
      color: #8b9eb3;
    }

    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: #409EFF;
      border-color: #409EFF;
    }

    .captcha-row {
      display: flex;
      width: 100%;
      gap: 10px;

      .el-input {
        flex: 1;
      }
    }

    .captcha-img {
      width: 110px;
      height: 40px;
      cursor: pointer;
      border-radius: 4px;
      border: 1px solid rgba(64, 158, 255, 0.15);
      background: rgba(64, 158, 255, 0.05);
    }

    .form-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      :deep(.el-link) {
        font-size: 13px;
      }
    }

    .login-btn {
      width: 100%;
      height: 44px;
      font-size: 15px;
      background: linear-gradient(90deg, #409EFF, #67C23A);
      border: none;
      letter-spacing: 5px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 20px rgba(64, 158, 255, 0.4);
      }
    }
  }

  .login-footer {
    text-align: center;
    margin-top: 25px;
    padding-top: 20px;
    border-top: 1px solid rgba(64, 158, 255, 0.1);
    font-size: 13px;
    color: #8b9eb3;

    .el-link {
      margin-left: 5px;
    }
  }
}

// 响应式布局
@media screen and (max-width: 992px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}

@media screen and (max-width: 480px) {
  .login-right .login-box {
    padding: 20px;
  }
}
</style>