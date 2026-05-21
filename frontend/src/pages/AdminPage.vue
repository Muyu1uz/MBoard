<template>
  <section class="admin-page">
    <div v-if="!auth.isAdmin" class="panel">
      <h3 class="detail-section-title">无权访问</h3>
      <p class="meta">只有管理员登录后，前端才会展示并请求管理接口。</p>
    </div>

    <template v-else>
      <div class="section-head">
        <div>
          <p class="eyebrow">Admin Console</p>
          <h3>内容管理</h3>
        </div>
        <button class="ghost" @click="loadDashboard">刷新</button>
      </div>

      <p v-if="message" class="notice">{{ message }}</p>

      <div class="detail-grid">
        <div class="panel">
          <div class="panel__header">
            <h3 class="detail-section-title">新建歌手</h3>
          </div>
          <div class="song-list">
            <input v-model="artistForm.name" class="input" placeholder="歌手名称" />
            <div class="upload-row">
              <input v-model="artistForm.avatarUrl" class="input" placeholder="头像链接" />
              <label class="ghost upload-button">
                上传本地图片
                <input type="file" accept="image/*" hidden @change="uploadArtistAvatar" />
              </label>
            </div>
            <textarea v-model="artistForm.bio" class="input textarea" placeholder="歌手简介"></textarea>
            <button class="button" @click="createArtist">保存歌手</button>
          </div>
        </div>

        <div class="panel">
          <div class="panel__header">
            <h3 class="detail-section-title">新建专辑</h3>
          </div>
          <div class="song-list">
            <input v-model="albumForm.artistId" class="input" placeholder="歌手 ID" />
            <input v-model="albumForm.name" class="input" placeholder="专辑名称" />

            <div class="genre-picker">
              <p class="meta">专辑类别，可多选</p>
              <label
                v-for="genre in dashboard?.genres ?? []"
                :key="genre.code"
                class="genre-check"
              >
                <input
                  :checked="albumForm.genres.includes(genre.code)"
                  type="checkbox"
                  @change="toggleGenre(genre.code)"
                />
                <span>{{ genre.label }}</span>
              </label>
            </div>

            <div class="upload-row">
              <input v-model="albumForm.coverUrl" class="input" placeholder="封面链接" />
              <label class="ghost upload-button">
                上传本地图片
                <input type="file" accept="image/*" hidden @change="uploadAlbumCover" />
              </label>
            </div>

            <input v-model="albumForm.releaseDate" class="input" placeholder="发行日期 YYYY-MM-DD" />
            <textarea v-model="albumForm.summary" class="input textarea" placeholder="专辑简介"></textarea>
            <label class="toggle"><input v-model="albumForm.trending" type="checkbox" /> 今日流行</label>
            <label class="toggle"><input v-model="albumForm.published" type="checkbox" /> 立即上架</label>
            <button class="button" @click="createAlbum">保存专辑</button>
          </div>
        </div>
      </div>

      <div class="detail-grid">
        <div class="panel">
          <div class="panel__header">
            <h3 class="detail-section-title">新建歌曲</h3>
          </div>
          <div class="song-list">
            <input v-model="songForm.albumId" class="input" placeholder="专辑 ID" />
            <input v-model="songForm.name" class="input" placeholder="歌曲名称" />
            <input v-model="songForm.trackNo" class="input" placeholder="曲序" />
            <input v-model="songForm.durationSeconds" class="input" placeholder="时长（秒）" />
            <button class="button" @click="createSong">保存歌曲</button>
          </div>
        </div>

        <div class="panel">
          <div class="panel__header">
            <h3 class="detail-section-title">已收录专辑</h3>
          </div>
          <div class="mini-list">
            <div v-for="album in dashboard?.albums ?? []" :key="album.id" class="mini-row">
              <strong>#{{ album.id }}</strong>
              <span>{{ album.name }} · {{ (album.genreLabels ?? []).join(' / ') }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from '../lib/api'
import { useAuthStore } from '../lib/auth'

const auth = useAuthStore()
const dashboard = ref<any>(null)
const message = ref('')
const artistForm = reactive({ name: '', avatarUrl: '', bio: '' })
const albumForm = reactive({
  artistId: '',
  name: '',
  genres: [] as string[],
  coverUrl: '',
  releaseDate: '',
  summary: '',
  trending: true,
  published: true,
})
const songForm = reactive({ albumId: '', name: '', trackNo: '', durationSeconds: '' })

async function loadDashboard() {
  try {
    dashboard.value = await api.getAdminDashboard()
    message.value = ''
  } catch (error) {
    message.value = (error as Error).message
  }
}

function toggleGenre(code: string) {
  if (albumForm.genres.includes(code)) {
    albumForm.genres = albumForm.genres.filter((item) => item !== code)
    return
  }
  albumForm.genres = [...albumForm.genres, code]
}

async function uploadArtistAvatar(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const result = await api.uploadImage(file)
  artistForm.avatarUrl = result.url
  input.value = ''
}

async function uploadAlbumCover(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  const result = await api.uploadImage(file)
  albumForm.coverUrl = result.url
  input.value = ''
}

async function createArtist() {
  try {
    await api.createArtist(artistForm)
    artistForm.name = ''
    artistForm.avatarUrl = ''
    artistForm.bio = ''
    await loadDashboard()
  } catch (error) {
    message.value = (error as Error).message
  }
}

async function createAlbum() {
  try {
    await api.createAlbum({
      ...albumForm,
      artistId: Number(albumForm.artistId),
    })
    albumForm.artistId = ''
    albumForm.name = ''
    albumForm.genres = []
    albumForm.coverUrl = ''
    albumForm.releaseDate = ''
    albumForm.summary = ''
    albumForm.trending = true
    albumForm.published = true
    await loadDashboard()
  } catch (error) {
    message.value = (error as Error).message
  }
}

async function createSong() {
  try {
    await api.createSong({
      ...songForm,
      albumId: Number(songForm.albumId),
      trackNo: Number(songForm.trackNo),
      durationSeconds: Number(songForm.durationSeconds),
    })
    songForm.albumId = ''
    songForm.name = ''
    songForm.trackNo = ''
    songForm.durationSeconds = ''
    await loadDashboard()
  } catch (error) {
    message.value = (error as Error).message
  }
}

onMounted(() => {
  if (auth.isAdmin) {
    loadDashboard()
  }
})
</script>
