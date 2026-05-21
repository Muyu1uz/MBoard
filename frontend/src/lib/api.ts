import { useAuthStore } from './auth'

const API_BASE = 'http://localhost:8080/api'

type ApiResponse<T> = {
  success: boolean
  message: string
  data: T
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const auth = useAuthStore()
  const headers = new Headers(init?.headers ?? {})
  if (!headers.has('Content-Type') && !(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  if (auth.token) {
    headers.set('Authorization', `Bearer ${auth.token}`)
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
  })
  const body = (await response.json()) as ApiResponse<T>
  if (!response.ok || !body.success) {
    throw new Error(body.message || 'Request failed')
  }
  return body.data
}

export const api = {
  getHome: () => request('/home'),
  getAlbums: (params: { page?: number; size?: number; genre?: string; keyword?: string } = {}) => {
    const query = new URLSearchParams()
    query.set('page', String(params.page ?? 1))
    query.set('size', String(params.size ?? 12))
    if (params.genre) query.set('genre', params.genre)
    if (params.keyword) query.set('keyword', params.keyword)
    return request(`/albums?${query.toString()}`)
  },
  getAlbum: (id: number | string) => request(`/albums/${id}`),
  rateAlbum: (id: number | string, star: number) =>
    request(`/albums/${id}/ratings`, {
      method: 'POST',
      body: JSON.stringify({ star }),
    }),
  rateSong: (id: number | string, star: number) =>
    request(`/songs/${id}/ratings`, {
      method: 'POST',
      body: JSON.stringify({ star }),
    }),
  createComment: (id: number | string, content: string) =>
    request(`/albums/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
  login: (username: string, password: string) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  register: (username: string, displayName: string, password: string) =>
    request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, displayName, password }),
    }),
  getArtist: (id: number | string) => request(`/artists/${id}`),
  getAdminDashboard: () => request('/admin/dashboard'),
  createArtist: (payload: Record<string, unknown>) =>
    request('/admin/artists', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  createAlbum: (payload: Record<string, unknown>) =>
    request('/admin/albums', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  createSong: (payload: Record<string, unknown>) =>
    request('/admin/songs', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request<{ url: string }>('/admin/uploads/images', {
      method: 'POST',
      body: formData,
    })
  },
}
