<template>
  <div class="shell">
    <header class="topbar">
      <div class="brand" @click="router.push('/')">
        <span class="brand__glyph">M</span>
        <div>
          <p class="eyebrow">Album Rating Board</p>
          <h1>MBoard</h1>
        </div>
      </div>

      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink v-if="auth.isAdmin" to="/admin">管理</RouterLink>
      </nav>

      <div class="topbar__search">
        <div class="search-shell">
          <input
            v-model="searchKeyword"
            class="input search-shell__input"
            placeholder="搜索专辑名称"
            @keyup.enter="submitSearch"
          />
          <button class="search-shell__submit" @click="submitSearch">搜索</button>
        </div>
      </div>

      <div class="topbar__actions">
        <div v-if="auth.token" class="session">
          <span>{{ auth.displayName }} · {{ auth.role === 'ADMIN' ? '管理员' : '用户' }}</span>
          <button class="ghost" @click="logout">退出</button>
        </div>
        <RouterLink v-else class="button" to="/login">登录</RouterLink>
      </div>
    </header>

    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './lib/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const searchKeyword = ref('')

watch(
  () => route.query.keyword,
  (value) => {
    searchKeyword.value = typeof value === 'string' ? value : ''
  },
  { immediate: true },
)

function submitSearch() {
  const keyword = searchKeyword.value.trim()
  router.push({
    path: '/',
    query: keyword ? { keyword } : {},
  })
}

function logout() {
  auth.clear()
  router.push('/')
}
</script>
