# messenger

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VS Code](https://code.visualstudio.com/) + [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Recommended Browser Setup

- Chromium-based browsers (Chrome, Edge, Brave, etc.):
  - [Vue.js devtools](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd) 
  - [Turn on Custom Object Formatter in Chrome DevTools](http://bit.ly/object-formatters)
- Firefox:
  - [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)
  - [Turn on Custom Object Formatter in Firefox DevTools](https://fxdx.dev/firefox-devtools-custom-object-formatters/)

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
pnpm install
```

### Compile and Hot-Reload for Development

```sh
pnpm dev
```

### Type-Check, Compile and Minify for Production

```sh
pnpm build
```

### Lint with [ESLint](https://eslint.org/)

```sh
pnpm lint
```

**Docker / Running (local development)**

- **Build & Run (detached):** `make up` — builds images and starts services:
  - `frontend` (Vite dev server at port `5173`)
  - `backend` (Spring Boot at port `8080`)
  - `db` (Postgres at port `5432`)
- **Build only:** `make build` — rebuild images with no cache.
- **Stop & remove:** `make down` — stops the compose application and removes containers.
- **Follow logs:** `make logs` — tails compose logs for all services.

Run backend tests in a disposable Maven container (no build artifacts left on host):

```sh
make test-backend
```

Run frontend tests (if configured in `package.json`):

```sh
make test-frontend
```

Notes:
- Frontend runs `npm run dev` inside a Node container and exposes port `5173`.
- Backend is built from `chat/` using the multi-stage `chat/Dockerfile` and connects to Postgres at `jdbc:postgresql://db:5432/chatdb` (credentials `postgres`/`postgres`).
- For local runs without Docker you can still execute the Maven wrapper: `./chat/mvnw test`.
