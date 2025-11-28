<template>
  <div class="chat min-h-screen flex flex-col bg-base-100 text-base-content">
    <header class="chat-header p-4 border-b bg-white shadow-sm flex items-start justify-between gap-4">
      <div v-if="!loggedIn">
        <div class="flex flex-col gap-2">
          <label class="font-semibold">Entrar (digite seu nome):</label>
          <div class="flex gap-2">
            <input class="input input-bordered" v-model="loginName" @input="onLoginInput" @keyup.enter="tryLogin" placeholder="Seu nome" />
            <button class="btn btn-primary" @click="tryLogin">Entrar</button>
            <button class="btn btn-ghost" @click="createUser">Criar usuário</button>
          </div>
          <div class="suggestions mt-2">
            <small class="text-sm text-muted">Usuários (clique para preencher):</small>
            <ul class="mt-1 max-h-36 overflow-auto divide-y rounded border bg-white">
              <li class="px-3 py-2 hover:bg-base-200 cursor-pointer flex justify-between items-center" v-for="u in filteredUsers" :key="u.id" @click="selectUserForLogin(u.name)">
                <span class="font-medium">{{ u.name }}</span>
                <span class="text-sm text-success" v-if="u.online">online</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div v-else>
        <div class="flex flex-col gap-3 w-full">
          <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
              <label class="font-semibold">Username:</label>
              <input class="input input-sm input-bordered" v-model="username" disabled />
            </div>
            <div class="ml-auto flex items-center gap-2">
              <div class="status">Status: <strong>{{ status }}</strong></div>
              <button class="btn btn-sm btn-outline" @click="doLogout">Logout</button>
            </div>
          </div>
          <div class="flex gap-2 items-center">
            <input class="input input-sm input-bordered" v-model="withUser" placeholder="Filtrar conversa com..." />
            <input class="input input-sm" type="datetime-local" v-model="filterFrom" />
            <input class="input input-sm" type="datetime-local" v-model="filterTo" />
            <button class="btn btn-sm btn-primary" @click="applyFilter">Filtrar</button>
            <button class="btn btn-sm btn-ghost" @click="clearFilter">Limpar</button>
          </div>
        </div>
      </div>
    </header>

    <!-- Users panel (visible always) -->
    <div class="users-panel p-4 border-b bg-white">
      <h4 class="text-sm font-semibold mb-2">Usuários</h4>
      <ul class="flex gap-2 overflow-auto">
        <li v-for="u in users" :key="u.id" class="px-3 py-1 rounded hover:bg-base-200 cursor-pointer flex items-center gap-2">
          <span class="font-medium">{{ u.name }}</span>
          <span class="text-sm" :class="u.online ? 'text-success' : 'text-muted'">{{ u.online ? 'online' : 'offline' }}</span>
        </li>
      </ul>
    </div>

    <main class="messages flex-1 overflow-auto p-4 bg-base-200" ref="messagesRef">
      <div class="flex flex-col gap-3">
        <div v-for="(m, i) in messages" :key="i" :class="['flex flex-col max-w-xl', m.from === username ? 'self-end items-end' : 'self-start items-start']">
          <div class="meta text-sm text-muted">{{ m.from }} • <small>{{ formatTime(m.time) }}</small></div>
          <div :class="['body mt-1 px-3 py-2 rounded-lg', m.from === username ? 'bg-green-200' : 'bg-white shadow']">{{ m.text }}</div>
        </div>
      </div>
    </main>

    <footer class="composer p-4 bg-white border-t" v-if="loggedIn">
      <div class="flex gap-2">
        <input class="input input-bordered flex-1" v-model="draft" @keyup.enter="send" placeholder="Digite uma mensagem" />
        <button class="btn btn-primary" @click="send">Enviar</button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ChatWindow' });
import { ref, onBeforeUnmount, watch, computed, onMounted } from 'vue';
import { ReconnectingWebSocket } from '@/services/ws';
import * as api from '@/services/api';

interface EndpointsShape { ws: string; http: Record<string,string> }
// fallback endpoints (matches project endpoints.json)
const endpoints: EndpointsShape = { ws: '/ws-chat', http: { getMessages: '/api/messages', postMessage: '/api/messages', getUsers: '/api/users', health: '/actuator/health' } };

type ChatMessage = { from: string; text: string; time: string };

const username = ref(localStorage.getItem('username') || '');
const loginName = ref('');
const loggedIn = ref(!!username.value);
const withUser = ref('');
const draft = ref('');
const messages = ref<ChatMessage[]>([]);
const status = ref('disconnected');
const wsClient = ref<ReconnectingWebSocket | null>(null);
const messagesRef = ref<HTMLElement | null>(null);
const users = ref<api.User[]>([]);
let usersPollId: number | null = null;
const filterFrom = ref('');
const filterTo = ref('');

watch(username, (v) => localStorage.setItem('username', v));

const filteredUsers = computed(() => {
  const q = loginName.value.trim().toLowerCase();
  if (!q) return users.value;
  return users.value.filter(u => u.name.toLowerCase().includes(q));
});

function formatTime(t: string) {
  const d = new Date(t);
  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${day}/${month}/${year} ${hours}:${minutes}`;
}

function scrollToBottom() {
  requestAnimationFrame(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  });
}

async function fetchUsers() {
  try {
    users.value = await api.listUsers();
  } catch (e) {
    console.warn('Erro ao buscar usuários', e);
  }
}

function onLoginInput() {
  // live suggestions: ensure we have latest users
  fetchUsers();
}

function selectUserForLogin(name: string) {
  loginName.value = name;
}

async function tryLogin() {
  const name = loginName.value.trim();
  if (!name) return alert('Digite um nome');
  // ensure we have users list
  await fetchUsers();
  const exists = users.value.some(u => u.name === name);
  if (!exists) {
    return alert('Nome não encontrado no banco. Use "Criar usuário" para cadastrar.');
  }
  try {
    await api.register(name);
    username.value = name;
    loggedIn.value = true;
    // connect websocket and load messages
    connect();
    await loadMessages();
  } catch (e) {
    console.error(e);
    alert('Falha ao registrar usuário');
  }
}

async function createUser() {
  const name = loginName.value.trim();
  if (!name) return alert('Digite um nome');
  try {
    await api.register(name);
    // refresh users list and login
    await fetchUsers();
    username.value = name;
    loggedIn.value = true;
    connect();
    await loadMessages();
  } catch (err) {
    console.error(err);
    alert('Falha ao criar usuário');
  }
}

async function doLogout() {
  try {
    await api.logout(username.value);
  } catch (e) { console.warn(e); }
  username.value = '';
  loggedIn.value = false;
  wsClient.value?.close();
  messages.value = [];
}

function connect() {
  status.value = 'connecting';
  const url = endpoints.ws;
  const client = new ReconnectingWebSocket(url);
  wsClient.value = client;

  client.onOpen = () => { status.value = 'connected'; };
  client.onClose = () => { status.value = 'disconnected'; };

  client.addMessageListener((data) => {
    // server may send different shapes (Message or ChatMessage)
    if (typeof data === 'string') {
      messages.value.push({ from: 'server', text: data, time: new Date().toISOString() });
    } else if (typeof data === 'object' && data !== null) {
      const anyd = data as Record<string, unknown>;
      // try common fields
      const from = (typeof anyd.sender === 'string') ? anyd.sender : (typeof anyd.from === 'string' ? anyd.from : 'anon');
      const text = (typeof anyd.content === 'string') ? anyd.content : (typeof anyd.text === 'string' ? anyd.text : JSON.stringify(anyd));
      const time = (typeof anyd.timestamp === 'string') ? anyd.timestamp : (typeof anyd.time === 'string' ? anyd.time : new Date().toISOString());
      messages.value.push({ from, text, time });
    }
    scrollToBottom();
  });
}

async function loadMessages(dateFrom?: string, dateTo?: string) {
  try {
    // if `withUser` is set, filter by that; otherwise request all messages
    const withUserVal = withUser.value ? withUser.value : undefined;
    const msgs = await api.getMessages(withUserVal, dateFrom, dateTo);
    messages.value = msgs.map(m => ({ from: m.sender, text: m.content, time: m.timestamp }));
    scrollToBottom();
  } catch (e) {
    console.warn('Erro ao carregar mensagens', e);
  }
}

function applyFilter() {
  const from = filterFrom.value ? new Date(filterFrom.value).toISOString() : undefined;
  const to = filterTo.value ? new Date(filterTo.value).toISOString() : undefined;
  loadMessages(from, to);
}

function clearFilter() {
  filterFrom.value = '';
  filterTo.value = '';
  loadMessages();
}

function send() {
  if (!draft.value) return;
  const iso = new Date().toISOString();
  const payload = {
    // both shapes so server controller accepts either
    sender: username.value || 'guest',
    content: draft.value,
    timestamp: iso,
    from: username.value || 'guest',
    text: draft.value,
    time: iso
  };
  const sent = wsClient.value?.send(payload);
  if (!sent) {
    messages.value.push({ from: username.value || 'guest', text: draft.value, time: iso });
  }
  draft.value = '';
  scrollToBottom();
}

onMounted(() => {
  // initial load and start polling online users
  fetchUsers();
  usersPollId = window.setInterval(fetchUsers, 10000);
});

onBeforeUnmount(() => {
  wsClient.value?.close();
  if (usersPollId) {
    clearInterval(usersPollId);
    usersPollId = null;
  }
});
</script>

<style scoped>
.chat { display:flex; flex-direction:column; height:100vh; max-height:100vh; }
.chat-header{ display:flex; justify-content:space-between; padding:8px; background:#f5f5f5; align-items:center }
.messages{ flex:1; overflow:auto; padding:12px; background:#fff }
.message{ margin-bottom:12px; display:flex; flex-direction:column; align-items:flex-start; }
.message-own{ align-items:flex-end; }
.meta{ color:#666; font-size:12px }
.body{ background:#e9f3ff; padding:8px; border-radius:6px; display:inline-block; max-width:70%; }
.message-own .body{ background:#dcf8c6; }
.composer{ display:flex; padding:8px; gap:8px; background:#fafafa }
.composer input{ flex:1; padding:8px }
</style>
