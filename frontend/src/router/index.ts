import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import AlbumPage from '../pages/AlbumPage.vue'
import ArtistPage from '../pages/ArtistPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import AdminPage from '../pages/AdminPage.vue'
import { useAuthStore } from '../lib/auth'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage },
    { path: '/albums/:id', component: AlbumPage, props: true },
    { path: '/artists/:id', component: ArtistPage, props: true },
    { path: '/login', component: LoginPage },
    { path: '/admin', component: AdminPage },
  ],
})

router.beforeEach((to) => {
  if (to.path !== '/admin') {
    return true
  }
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    return '/login'
  }
  if (!auth.isAdmin) {
    return '/'
  }
  return true
})
