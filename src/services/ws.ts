// STOMP + SockJS wrapper (clean, readable)
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';

type MessageHandler = (data: unknown) => void;

export class ReconnectingWebSocket {
  private url: string;
  private client: Client | null = null;
  private connected = false;
  private shouldReconnect = true;
  private reconnectDelay = 1000;
  private maxDelay = 30000;
  private handlers: MessageHandler[] = [];
  public onOpen: (() => void) | null = null;
  public onClose: ((ev?: CloseEvent) => void) | null = null;

  constructor(url = '/ws-chat') {
    this.url = url;
    this.connect();
  }

  private connect() {
    if (this.client) {
      try { this.client.deactivate(); } catch { }
      this.client = null;
    }

    const sockUrl = this.resolveUrl(this.url);
    const client = new Client({
      webSocketFactory: () => new SockJS(sockUrl),
      reconnectDelay: 0, // we'll handle our own reconnect
      debug: () => { },
    });

    client.onConnect = () => {
      this.connected = true;
      this.reconnectDelay = 1000;
      if (this.onOpen) this.onOpen();
      // subscribe to topic/messages
      client.subscribe('/topic/messages', (m: IMessage) => {
        let payload: unknown = m.body;
        try { payload = JSON.parse(m.body); } catch { /* leave string */ }
        this.handlers.forEach(h => h(payload));
      });
    };

    client.onStompError = (e) => {
      console.error('STOMP error', e);
    };

    client.onWebSocketClose = (ev: CloseEvent) => {
      this.connected = false;
      if (this.onClose) this.onClose(ev);
      if (!this.shouldReconnect) return;
      setTimeout(() => this.connect(), this.reconnectDelay);
      this.reconnectDelay = Math.min(this.reconnectDelay * 1.5, this.maxDelay);
    };

    client.activate();
    this.client = client;
  }

  private resolveUrl(u: string) {
    if (typeof window === 'undefined') return u;
    if (u.charAt(0) === '/') {
      // Vite proxies /ws-chat to backend; SockJS expects full path relative to origin
      return `${window.location.protocol}//${window.location.host}${u}`;
    }
    return u;
  }

  send(obj: unknown) {
    const payload = typeof obj === 'string' ? obj : JSON.stringify(obj);
    if (this.client && this.connected) {
      try {
        this.client.publish({ destination: '/app/chat.sendMessage', body: payload });
        return true;
      } catch { /* fallthrough */ }
    }
    return false;
  }

  addMessageListener(fn: MessageHandler) {
    this.handlers.push(fn);
    return () => { this.handlers = this.handlers.filter(h => h !== fn); };
  }

  close() {
    this.shouldReconnect = false;
    try { this.client?.deactivate(); } catch { }
  }
}
