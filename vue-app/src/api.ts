// WHAT: The ONLY file that talks to the backend. WHY: keeping all GraphQL calls here means components
// never contain query strings, and if the API changes you edit one place.

// API address: from env var VITE_API_URL if set, otherwise the local Spring Boot server
const URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/graphql';

// ---- Types: describe the shape of data coming back, so TypeScript catches typos ----
export interface Movie { id: string; title: string; synopsis: string; year: number; genre: string; posterUrl: string; trailerUrl: string }
export interface Rail { id: string; title: string; movies: Movie[] }
export interface User { id: string; email: string; plan: string | null; subscribedUntil: string | null; active: boolean }
export interface PlanInfo { plan: string; priceMonthly: number; description: string }

// Generic helper that sends any GraphQL query/mutation. <T> = the type of data we expect back.
async function gql<T>(query: string, variables: Record<string, unknown> = {}, token?: string | null): Promise<T> {
  const res = await fetch(URL, {
    method: 'POST',                                            // GraphQL always POSTs
    headers: {
      'Content-Type': 'application/json',                      // we send JSON
      ...(token ? { Authorization: `Bearer ${token}` } : {}),  // add the login token only if we have one
    },
    body: JSON.stringify({ query, variables }),                // the query text + its variables
  });
  const json = await res.json();                               // parse the response
  if (json.errors?.length) throw new Error(json.errors[0].message); // GraphQL errors => normal JS error (message shown in UI)
  return json.data as T;                                       // hand back just the data
}

const USER = `id email plan subscribedUntil active`;           // fields we always want for a user (avoids repeating)

// Public: list all rails with their movies
export const rails = () =>
  gql<{ rails: Rail[] }>(`{ rails { id title movies { id title synopsis year genre posterUrl trailerUrl } } }`).then(d => d.rails);

// Public: list plans for the pricing dialog
export const plans = () =>
  gql<{ plans: PlanInfo[] }>(`{ plans { plan priceMonthly description } }`).then(d => d.plans);

// Who am I? Returns the user or null if the token is invalid
export const me = (token: string) =>
  gql<{ me: User | null }>(`{ me { ${USER} } }`, {}, token).then(d => d.me);

// Sign in (or sign up) with an email; returns a token to store
export const login = (email: string) =>
  gql<{ login: { token: string; user: User } }>(
    `mutation($email: String!) { login(email: $email) { token user { ${USER} } } }`, { email }).then(d => d.login);

// Buy a plan; returns the updated user
export const subscribe = (plan: string, token: string) =>
  gql<{ subscribe: User }>(`mutation($plan: Plan!) { subscribe(plan: $plan) { ${USER} } }`, { plan }, token).then(d => d.subscribe);

// End the plan; returns the updated user
export const cancel = (token: string) =>
  gql<{ cancelSubscription: User }>(`mutation { cancelSubscription { ${USER} } }`, {}, token).then(d => d.cancelSubscription);

// Ask for the full-movie URL. The server refuses unless the user has an active plan.
export const watch = (movieId: string, token: string) =>
  gql<{ watch: { url: string } }>(`query($id: ID!) { watch(movieId: $id) { url } }`, { id: movieId }, token).then(d => d.watch.url);