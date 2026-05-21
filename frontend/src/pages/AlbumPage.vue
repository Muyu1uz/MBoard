<template>
  <section v-if="album" class="album-detail">
    <div class="album-hero">
      <img :src="album.coverUrl" :alt="album.name" class="album-detail__cover" />

      <div class="album-hero__copy">
        <div>
          <p class="eyebrow">
            <RouterLink :to="`/artists/${album.artistId}`">{{ album.artistName }}</RouterLink>
          </p>
          <h2>{{ album.name }}</h2>
        </div>

        <div class="tag-row">
          <span
            v-for="label in album.genreLabels ?? []"
            :key="label"
            class="chip chip--genre"
          >
            {{ label }}
          </span>
        </div>

        <p class="lede">{{ album.summary }}</p>

        <div class="fact-row">
          <span>发行时间：{{ album.releaseDate ?? '待公布' }}</span>
          <span>{{ album.ratingCount }} 人评分</span>
          <strong>{{ album.ratingScore10 ?? '暂无' }}/10</strong>
        </div>

        <div class="panel">
          <div class="panel__header">
            <h3 class="detail-section-title">你的专辑评分</h3>
            <span class="meta">{{ album.currentUserScore10 ?? '未评分' }}</span>
          </div>
          <StarRating :model-value="album.currentUserStar" @select="rateAlbum" />
        </div>
      </div>
    </div>

    <section class="detail-grid">
      <div class="panel">
        <div class="panel__header">
          <h3 class="detail-section-title">歌曲列表</h3>
          <span class="meta">每首歌都支持单独评分</span>
        </div>
        <div class="song-list">
          <div v-for="song in album.songs" :key="song.id" class="song-row">
            <div>
              <p class="song-title">{{ song.trackNo }}. {{ song.name }}</p>
              <p class="meta">{{ song.durationSeconds }} 秒 · {{ song.ratingScore10 ?? '暂无' }}/10</p>
            </div>
            <StarRating :model-value="song.currentUserStar" @select="(star) => rateSong(song.id, star)" />
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel__header">
          <h3 class="detail-section-title">评论区</h3>
          <span class="meta">评论会附带当前专辑评分快照</span>
        </div>
        <textarea v-model="comment" class="input textarea" placeholder="写下你对这张专辑的看法"></textarea>
        <button class="button" @click="submitComment">发布评论</button>
        <div class="comment-list">
          <article v-for="entry in album.comments" :key="entry.id" class="comment-card">
            <div class="comment-card__top">
              <strong>{{ entry.username }}</strong>
              <span class="meta">{{ entry.ratingScoreSnapshot ?? '暂无' }}/10</span>
            </div>
            <p>{{ entry.content }}</p>
          </article>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import StarRating from '../components/StarRating.vue'
import { api } from '../lib/api'

const route = useRoute()
const album = ref<any>(null)
const comment = ref('')

async function load() {
  album.value = await api.getAlbum(route.params.id as string)
}

async function rateAlbum(star: number) {
  await api.rateAlbum(route.params.id as string, star)
  await load()
}

async function rateSong(songId: number, star: number) {
  await api.rateSong(songId, star)
  await load()
}

async function submitComment() {
  if (!comment.value.trim()) {
    return
  }
  await api.createComment(route.params.id as string, comment.value)
  comment.value = ''
  await load()
}

onMounted(load)
</script>
