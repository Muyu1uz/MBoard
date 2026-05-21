<template>
  <section class="hero">
    <div class="hero__copy">
      <h2 class="hero-title">
        <span class="hero-title__brand">MBoard</span>
        <span class="hero-title__sub">专业音乐评分社区</span>
      </h2>
    </div>

    <aside class="chart-panel">
      <p class="eyebrow">Top 5 热门专辑</p>
      <ol>
        <li v-for="album in topFiveAlbums" :key="album.id">
          <RouterLink :to="`/albums/${album.id}`">{{ album.name }}</RouterLink>
          <strong>{{ album.ratingScore10 ?? '暂无' }}</strong>
        </li>
      </ol>
    </aside>
  </section>

  <section class="grid-section">
    <div class="section-head">
      <h3>为你推荐</h3>
    </div>
    <div class="album-grid">
      <AlbumCard v-for="album in home?.recommendedAlbums ?? []" :key="album.id" :album="album" />
    </div>
  </section>

  <section class="grid-section">
    <div class="section-head">
      <h3>今日流行</h3>
    </div>
    <div class="album-grid">
      <AlbumCard v-for="album in home?.trendingAlbums ?? []" :key="album.id" :album="album" />
    </div>
  </section>

  <section class="grid-section">
    <div class="section-head">
      <h3>按类型浏览</h3>
      <span class="meta">共 {{ albums?.total ?? 0 }} 张专辑</span>
    </div>
    <div class="genre-tabs">
      <button
        class="chip chip--filter"
        :class="{ 'chip--active': currentGenre === '' }"
        @click="changeGenre('')"
      >
        全部类型
      </button>
      <button
        v-for="genre in genreOptions"
        :key="genre.code"
        class="chip chip--filter"
        :class="{ 'chip--active': currentGenre === genre.code }"
        @click="changeGenre(genre.code)"
      >
        {{ genre.label }}
      </button>
    </div>
    <div class="album-grid">
      <AlbumCard v-for="album in albums?.records ?? []" :key="album.id" :album="album" />
    </div>
    <div class="pager" v-if="albums">
      <button class="ghost" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
      <span class="meta">第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button class="ghost" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AlbumCard from '../components/AlbumCard.vue'
import { api } from '../lib/api'

const LOCAL_GENRES = [
  { code: 'pop', label: '流行' },
  { code: 'hiphop', label: '嘻哈' },
  { code: 'electronic', label: '电子' },
  { code: 'jazz', label: '爵士' },
  { code: 'rock', label: '摇滚' },
  { code: 'rnb', label: 'R&B' },
  { code: 'folk', label: '民谣' },
  { code: 'classical', label: '古典' },
]

const route = useRoute()
const home = ref<any>(null)
const albums = ref<any>(null)
const currentGenre = ref('')
const keyword = ref('')
const page = ref(1)
const size = ref(8)

const genreOptions = computed(() => {
  const genres = home.value?.genres
  return Array.isArray(genres) && genres.length > 0 ? genres : LOCAL_GENRES
})

const topFiveAlbums = computed(() => (home.value?.topAlbums ?? []).slice(0, 5))

const totalPages = computed(() => {
  if (!albums.value) return 1
  return Math.max(1, Math.ceil((albums.value.total ?? 0) / (albums.value.size ?? size.value)))
})

async function loadHome() {
  home.value = await api.getHome()
}

async function loadAlbums() {
  albums.value = await api.getAlbums({
    page: page.value,
    size: size.value,
    genre: currentGenre.value || undefined,
    keyword: keyword.value || undefined,
  })
}

async function changeGenre(genreCode: string) {
  currentGenre.value = genreCode
  page.value = 1
  await loadAlbums()
}

async function changePage(next: number) {
  page.value = next
  await loadAlbums()
}

watch(
  () => route.query.keyword,
  async (value) => {
    keyword.value = typeof value === 'string' ? value.trim() : ''
    page.value = 1
    if (home.value !== null) {
      await loadAlbums()
    }
  },
  { immediate: true },
)

onMounted(async () => {
  await loadHome()
  await loadAlbums()
})
</script>
