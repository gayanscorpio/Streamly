<!-- WHAT: Pop-up with movie details, the two watch buttons and the video player.
     WHY: separates the "movie details" UI from App.vue. It does NOT decide whether you may watch;
     it just reports button clicks and lets App.vue decide. -->
<script setup lang="ts">
import type { Movie } from '../api';
defineProps<{ movie: Movie; video: string | null }>();        // INPUT: the movie, and the video URL to play (null = none)
defineEmits<{ close: []; trailer: []; full: [] }>();          // OUTPUTS: three events with no data
</script>

<template>
  <!-- clicking the dark background closes the dialog -->
  <div class="overlay" @click="$emit('close')">
    <!-- .stop prevents clicks inside the box from reaching the background (which would close it) -->
    <div class="dialog" role="dialog" :aria-label="movie.title" @click.stop>
      <button class="close" @click="$emit('close')">Close</button>
      <div class="detail">
        <img :src="movie.posterUrl" alt="" />
        <div style="flex: 1; min-width: 220px">
          <h3>{{ movie.title }}</h3>
          <p class="meta">{{ movie.year }} · {{ movie.genre }}</p>
          <p>{{ movie.synopsis }}</p>
          <div class="actions">
            <button @click="$emit('trailer')">Watch trailer (free)</button>        <!-- always allowed -->
            <button class="primary" @click="$emit('full')">Watch full movie</button> <!-- parent checks the plan -->
          </div>
        </div>
      </div>
      <!-- v-if: render the player only when there is a URL; :key forces a reload when the URL changes -->
      <video v-if="video" :key="video" :src="video" controls autoplay />
    </div>
  </div>
</template>