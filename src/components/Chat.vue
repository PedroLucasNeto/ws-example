<template>
  <div class="chat">
    <header class="chat-header">
      <div>
        <label>Username:</label>
        <input v-model="username" placeholder="Seu nome" />
      </div>
      <div class="status">Status: <strong>{{ status }}</strong></div>
    </header>

    <main class="messages" ref="messagesRef">
      <div v-for="(m, i) in messages" :key="i" class="message">
        <div class="meta">{{ m.from }} • <small>{{ formatTime(m.time) }}</small></div>
        <div class="body">{{ m.text }}</div>
      </div>
    </main>

    <footer class="composer">
      <input v-model="draft" @keyup.enter="send" placeholder="Digite uma mensagem" />
      <button @click="send">Enviar</button>
    </footer>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ChatWindow' });
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import { ReconnectingWebSocket } from '@/services/ws';
import endpointsJson from '../../endpoints.json';

interface EndpointsShape { ws: string; http: Record<string,string> }
const endpoints = endpointsJson as unknown as EndpointsShape;

type ChatMessage = { from: string; text: string; time: string };

const username = ref(localStorage.getItem('username') || '');
const draft = ref('');
const messages = ref<ChatMessage[]>([]);
const status = ref('disconnected');
const wsClient = ref<ReconnectingWebSocket | null>(null);
const messagesRef = ref<HTMLElement | null>(null);

watch(username, (v) => localStorage.setItem('username', v));

function formatTime(t: string) {
  try { return new Date(t).toLocaleTimeString(); } catch { return t; }
}

function scrollToBottom() {
  requestAnimationFrame(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  });
}

function connect() {
  status.value = 'connecting';
  const url = endpoints.ws;
  const client = new ReconnectingWebSocket(url);
  wsClient.value = client;

  client.onOpen = () => { status.value = 'connected'; };
  client.onClose = () => { status.value = 'disconnected'; };

  client.addMessageListener((data) => {
    // accept text or structured messages
    if (typeof data === 'string') {
      messages.value.push({ from: 'server', text: data, time: new Date().toISOString() });
    } else if (typeof data === 'object' && data !== null) {
      const anyd = data as Record<string, unknown>;
      const from = typeof anyd.from === 'string' ? anyd.from : 'anon';
      const text = typeof anyd.text === 'string' ? anyd.text : JSON.stringify(anyd);
      const time = typeof anyd.time === 'string' ? anyd.time : new Date().toISOString();
      messages.value.push({ from, text, time });
    }
    scrollToBottom();
  });
}

function send() {
  if (!draft.value) return;
  const payload = { from: username.value || 'guest', text: draft.value, time: new Date().toISOString() };
  wsClient.value?.send(payload);
  // show immediately
  messages.value.push(payload);
  draft.value = '';
  scrollToBottom();
}

onMounted(() => {
  connect();
});

onBeforeUnmount(() => {
  wsClient.value?.close();
});
</script>

<style scoped>
.chat { display:flex; flex-direction:column; height:100vh; max-height:100vh; }
.chat-header{ display:flex; justify-content:space-between; padding:8px; background:#f5f5f5; align-items:center }
.messages{ flex:1; overflow:auto; padding:12px; background:#fff }
.message{ margin-bottom:12px; }
.meta{ color:#666; font-size:12px }
.body{ background:#e9f3ff; padding:8px; border-radius:6px; display:inline-block }
.composer{ display:flex; padding:8px; gap:8px; background:#fafafa }
.composer input{ flex:1; padding:8px }
</style>
