import { useAuthStore } from './auth'
import type {
  AdminDashboardView,
  AlbumCardView,
  AlbumDetailView,
  AlbumQueryParams,
  AlbumSaveRequest,
  ApiResponse,
  ArtistDetailView,
  ArtistSaveRequest,
  AuthView,
  CommentView,
  HomeView,
  PagedView,
  RatingView,
  SongAdminView,
  SongSaveRequest,
  UploadView,
  AlbumAdminView,
  ArtistAdminView,
} from '../types/api'

const API_BASE = 'http://localhost:8080/api'

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
  getHome: (): Promise<HomeView> => request('/home'),
  getAlbums: (params: AlbumQueryParams = {}): Promise<PagedView<AlbumCardView>> => {
    const query = new URLSearchParams()
    query.set('page', String(params.page ?? 1))
    query.set('size', String(params.size ?? 12))
    if (params.genre) query.set('genre', params.genre)
    if (params.keyword) query.set('keyword', params.keyword)
    return request(`/albums?${query.toString()}`)
  },
  getAlbum: (id: number | string): Promise<AlbumDetailView> => request(`/albums/${id}`),
  rateAlbum: (id: number | string, star: number) =>
    request<RatingView>(`/albums/${id}/ratings`, {
      method: 'POST',
      body: JSON.stringify({ star }),
    }),
  rateSong: (id: number | string, star: number) =>
    request<RatingView>(`/songs/${id}/ratings`, {
      method: 'POST',
      body: JSON.stringify({ star }),
    }),
  createComment: (id: number | string, content: string) =>
    request<CommentView>(`/albums/${id}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
  login: (username: string, password: string) =>
    request<AuthView>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  register: (username: string, displayName: string, password: string) =>
    request<AuthView>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, displayName, password }),
    }),
  getArtist: (id: number | string): Promise<ArtistDetailView> => request(`/artists/${id}`),
  getAdminDashboard: (): Promise<AdminDashboardView> => request('/admin/dashboard'),
  createArtist: (payload: ArtistSaveRequest) =>
    request<ArtistAdminView>('/admin/artists', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  createAlbum: (payload: AlbumSaveRequest) =>
    request<AlbumAdminView>('/admin/albums', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  createSong: (payload: SongSaveRequest) =>
    request<SongAdminView>('/admin/songs', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request<UploadView>('/admin/uploads/images', {
      method: 'POST',
      body: formData,
    })
  },
}
