// WHAT: Type declarations. WHY: TypeScript doesn't know what a ".vue" file is, so we describe it;
// otherwise `import App from './App.vue'` would be a type error.
/// <reference types="vite/client" />          // adds Vite's types (import.meta.env, asset imports)
declare module '*.vue' {                       // "any file ending in .vue..."
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<object, object, unknown>;  // ...is a Vue component
  export default component;
}