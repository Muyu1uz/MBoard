import { reactive } from 'vue'

const STORAGE_KEY = 'mboard-auth'

type AuthState = {
  token: string
  userId: number | null
  username: string
  displayName: string
  role: string
}

const state = reactive<AuthState>(load())

function load(): AuthState {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return { token: '', userId: null, username: '', displayName: '', role: '' }
  }
  return JSON.parse(raw) as AuthState
}

function persist() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

export function useAuthStore() {
  return {
    get token() {
      return state.token
    },
    get userId() {
      return state.userId
    },
    get username() {
      return state.username
    },
    get displayName() {
      return state.displayName
    },
    get role() {
      return state.role
    },
    get isLoggedIn() {
      return Boolean(state.token)
    },
    get isAdmin() {
      return state.role === 'ADMIN'
    },
    set(payload: AuthState) {
      state.token = payload.token
      state.userId = payload.userId
      state.username = payload.username
      state.displayName = payload.displayName
      state.role = payload.role
      persist()
    },
    clear() {
      state.token = ''
      state.userId = null
      state.username = ''
      state.displayName = ''
      state.role = ''
      persist()
    },
  }
}
