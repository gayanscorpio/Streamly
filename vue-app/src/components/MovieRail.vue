<!-- WHAT: One horizontal row of posters. WHY: a reusable "dumb" component: it shows data it is given
     and reports clicks upward. It never fetches anything itself. -->
<script setup lang="ts">
import type { Movie, Rail } from '../api';           // types only (erased at build time)
defineProps<{ rail: Rail }>();                       // INPUT: parent passes the rail to display
defineEmits<{ select: [movie: Movie] }>();           // OUTPUT: event sent to parent with the clicked movie
</script>

<template>
  <section class="rail">
    <h2>{{ rail.title }}</h2>                        <!-- {{ }} prints a value -->
    <div class="track">
      <!-- v-for = loop; :key helps Vue track each item; $emit tells the parent which movie was chosen -->
      <button v-for="m in rail.movies" :key="m.id" class="card" @click="$emit('select', m)">
        <img :src="m.posterUrl" alt="" loading="lazy" /><span>{{ m.title }}</span> <!-- lazy = load image only when near the screen -->
      </button>
    </div>
  </section>
</template>