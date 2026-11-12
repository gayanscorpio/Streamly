<!-- WHAT: The root component = the "brain" of the app. WHY: it owns all state (user, movies, dialogs),
     talks to the API, and passes data down to the small components. Children report back with events. -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';                          // ref = reactive value; onMounted = run code when the page loads
import * as api from './api';                                  // all backend calls
import type { Movie, Rail, User, PlanInfo } from './api';
import MovieRail from './components/MovieRail.vue';
import MovieDialog from './components/MovieDialog.vue';
import PlansDialog from './components/PlansDialog.vue';

// ---- State: when a ref changes, Vue re-draws whatever uses it (read/write with .value in script) ----
const rails = ref<Rail[]>([]);                                 // catalog rows
const plans = ref<PlanInfo[]>([]);                             // pricing cards
const token = ref<string | null>(localStorage.getItem('token')); // restore login after page refresh
const user = ref<User | null>(null);                           // signed-in user (null = guest)
const email = ref('');                                         // text in the sign-in box
const movie = ref<Movie | null>(null);                         // movie whose dialog is open
const video = ref<string | null>(null);                        // URL currently playing
const showPlans = ref(false);                                  // is the plans dialog open?
const msg = ref('');                                           // error message shown at the top

// Wrapper: run any async action, clear the old error first, and show a new error if it throws
async function run(fn: () => Promise<void>) {
  try { msg.value = ''; await fn(); } catch (e) { msg.value = (e as Error).message; }
}

// Load the current user from the saved token (called on startup)
async function loadUser() {
  if (!token.value) { user.value = null; return; }             // no token = guest
  try {
    user.value = await api.me(token.value);                    // ask the server who this is
    if (!user.value) token.value = null;                       // server says unknown = forget the token
  } catch { token.value = null; }
}

// Runs once when the page first appears: load public data + try to restore the session
onMounted(() => {
  run(async () => { rails.value = await api.rails(); plans.value = await api.plans(); });
  loadUser();
});

// Sign in: call API, store token so refresh keeps you signed in
const signIn = () => run(async () => {
  const r = await api.login(email.value);
  localStorage.setItem('token', r.token); token.value = r.token; user.value = r.user; email.value = '';
});

// Sign out: forget everything locally
function signOut() { localStorage.removeItem('token'); token.value = null; user.value = null; }

// "Watch full movie" decision tree: guest -> ask to sign in, no plan -> show plans, plan -> play
const watchFull = () => run(async () => {
  if (!movie.value) return;
  if (!token.value || !user.value) { msg.value = 'Sign in to watch the full movie.'; movie.value = null; return; }
  if (!user.value.active) { showPlans.value = true; return; }
  video.value = await api.watch(movie.value.id, token.value);  // server double-checks the plan (never trust the UI alone)
});

// User picked a plan in the dialog
const choose = (plan: string) => run(async () => {
  if (!token.value) { msg.value = 'Sign in before choosing a plan.'; showPlans.value = false; return; }
  user.value = await api.subscribe(plan, token.value); showPlans.value = false;
});

// Cancel the current plan
const cancel = () => run(async () => { if (token.value) user.value = await api.cancel(token.value); });

// Closing the movie dialog also stops the video (removing it from the page)
function closeMovie() { movie.value = null; video.value = null; }
</script>

<template>
  <header>
    <div class="brand">Streamly</div>
    <!-- v-if / v-else: show account info when signed in, otherwise the sign-in form -->
    <div v-if="user" class="who">
      <span>{{ user.email }}</span>
      <span class="badge">{{ user.active ? user.plan : 'No plan' }}</span>
      <button @click="showPlans = true">Plans</button>
      <button v-if="user.active" @click="cancel">Cancel plan</button>  <!-- only if there is a plan to cancel -->
      <button @click="signOut">Sign out</button>
    </div>
    <div v-else class="who">
      <!-- v-model = two-way binding: typing updates `email`; Enter key submits -->
      <input v-model="email" type="email" placeholder="you@example.com" @keydown.enter="signIn" />
      <button class="primary" :disabled="!email" @click="signIn">Sign in</button>
    </div>
  </header>

  <main>
    <p v-if="msg" class="msg" role="alert">{{ msg }}</p>             <!-- error line, only when there is an error -->
    <!-- one MovieRail per rail; when it emits "select", store that movie ($event) to open the dialog -->
    <MovieRail v-for="r in rails" :key="r.id" :rail="r" @select="movie = $event" />
  </main>

  <!-- dialog exists only while a movie is selected; "trailer" plays the free URL immediately -->
  <MovieDialog
    v-if="movie"
    :movie="movie"
    :video="video"
    @close="closeMovie"
    @trailer="video = movie.trailerUrl"
    @full="watchFull"
  />

  <PlansDialog v-if="showPlans" :plans="plans" :user="user" @close="showPlans = false" @choose="choose" />
</template>