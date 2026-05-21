<template>
  <section v-if="artist" class="artist-page">
    <div class="artist-head">
      <img :src="artist.avatarUrl" :alt="artist.name" class="artist-avatar" />
      <div>
        <p class="eyebrow">Artist Page</p>
        <h2>{{ artist.name }}</h2>
        <p class="lede">{{ artist.bio }}</p>
      </div>
    </div>

    <div class="grid-section">
      <div class="section-head">
        <h3>专辑目录</h3>
      </div>
      <div class="album-grid">
        <AlbumCard v-for="album in artist.albums" :key="album.id" :album="album" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AlbumCard from '../components/AlbumCard.vue'
import { api } from '../lib/api'

const route = useRoute()
const artist = ref<any>(null)

onMounted(async () => {
  artist.value = await api.getArtist(route.params.id as string)
})
</script>
