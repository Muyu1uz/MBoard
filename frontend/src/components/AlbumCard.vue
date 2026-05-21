<template>
  <article class="album-card" @click="router.push(`/albums/${album.id}`)">
    <img :src="album.coverUrl" :alt="album.name" class="album-card__cover" />
    <div class="album-card__body">
      <p class="eyebrow">{{ album.artistName }}</p>
      <h3 class="album-card__title">{{ album.name }}</h3>
      <div class="tag-row">
        <span
          v-for="label in album.genreLabels ?? []"
          :key="label"
          class="chip chip--genre"
        >
          {{ label }}
        </span>
      </div>
      <p class="meta">{{ album.releaseDate ?? '待公布' }}</p>
      <div class="score-row">
        <strong>{{ album.ratingScore10 ?? '暂无' }}</strong>
        <span class="meta">{{ album.ratingCount }} 人评分</span>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

defineProps<{ album: Record<string, any> }>()

const router = useRouter()
</script>
