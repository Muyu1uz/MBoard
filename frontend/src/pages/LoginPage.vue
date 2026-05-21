<template>
  <section class="auth-page">
    <div class="auth-card auth-card--single">
      <p class="eyebrow">Account Access</p>
      <h2>{{ isRegister ? '创建账号' : '账号登录' }}</h2>

      <div class="auth-single">
        <div class="panel auth-panel">
          <div class="panel__header">
            <h3 class="detail-section-title">{{ isRegister ? '注册' : '登录' }}</h3>
          </div>

          <div class="song-list">
            <input
              v-model="activeForm.username"
              class="input"
              placeholder="用户名"
              @keyup.enter="submit"
            />

            <input
              v-if="isRegister"
              v-model="registerForm.displayName"
              class="input"
              placeholder="显示名称"
              @keyup.enter="submit"
            />

            <input
              v-model="activeForm.password"
              class="input"
              placeholder="密码"
              type="password"
              @keyup.enter="submit"
            />

            <button class="button" @click="submit">
              {{ isRegister ? '创建账号' : '立即登录' }}
            </button>
          </div>
        </div>
      </div>

      <p class="auth-switch">
        <template v-if="isRegister">
          已有账号？
          <button class="text-button" type="button" @click="switchMode(false)">返回登录</button>
        </template>
        <template v-else>
          如果没有账号，先
          <button class="text-button" type="button" @click="switchMode(true)">注册</button>
        </template>
      </p>

      <p class="meta">默认管理员账号：admin / admin123</p>
      <p class="meta">默认演示账号：demo / demo123</p>
      <p v-if="message" class="notice">{{ message }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../lib/api'
import { useAuthStore } from '../lib/auth'

const router = useRouter()
const auth = useAuthStore()
const message = ref('')
const isRegister = ref(false)
const loginForm = reactive({ username: 'demo', password: 'demo123' })
const registerForm = reactive({ username: '', displayName: '', password: '' })

const activeForm = computed(() => (isRegister.value ? registerForm : loginForm))

function switchMode(next: boolean) {
  isRegister.value = next
  message.value = ''
}

async function submit() {
  if (isRegister.value) {
    await register()
    return
  }
  await login()
}

async function login() {
  try {
    const payload = await api.login(loginForm.username, loginForm.password)
    auth.set(payload)
    router.push('/')
  } catch (error) {
    message.value = (error as Error).message
  }
}

async function register() {
  try {
    const payload = await api.register(registerForm.username, registerForm.displayName, registerForm.password)
    auth.set(payload)
    router.push('/')
  } catch (error) {
    message.value = (error as Error).message
  }
}
</script>
