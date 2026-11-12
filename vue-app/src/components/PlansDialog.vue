<!-- WHAT: Pop-up to pick a subscription plan. WHY: its own component so App.vue stays small.
     It only displays plans and reports which one was chosen; App.vue calls the API. -->
<script setup lang="ts">
import type { PlanInfo, User } from '../api';
defineProps<{ plans: PlanInfo[]; user: User | null }>();      // INPUT: plan list + current user (to highlight their plan)
defineEmits<{ close: []; choose: [plan: string] }>();         // OUTPUT: close, or choose with the plan name
</script>

<template>
  <div class="overlay" @click="$emit('close')">               <!-- click outside = close -->
    <div class="dialog" role="dialog" aria-label="Plans" @click.stop>
      <button class="close" @click="$emit('close')">Close</button>
      <h3>Choose a plan</h3>
      <p class="meta">Trailers stay free. Plans unlock full movies for 30 days.</p>
      <div class="plans">
        <!-- one card per plan; the "current" CSS class is added when it's the user's active plan -->
        <div v-for="p in plans" :key="p.plan" class="plan" :class="{ current: user?.plan === p.plan && user?.active }">
          <b>{{ p.plan }}</b><p>{{ p.description }}</p>
          <!-- first "$" is literal text; {{ }} prints the price -->
          <button class="primary" @click="$emit('choose', p.plan)">${{ p.priceMonthly }}/mo</button>
        </div>
      </div>
    </div>
  </div>
</template>