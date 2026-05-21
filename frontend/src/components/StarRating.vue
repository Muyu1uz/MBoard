<template>
  <div class="stars">
    <button
      v-for="star in [1, 2, 3, 4, 5]"
      :key="star"
      class="star"
      :class="{ active: star <= modelValue }"
      @click="$emit('select', star)"
      type="button"
    >
      ★
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{ modelValue?: number | null }>()
defineEmits<{ select: [value: number] }>()
</script>
