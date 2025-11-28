// Small reconnecting WebSocket wrapper
type MessageHandler = (data: unknown) => void;

export class ReconnectingWebSocket {
  private url: string;
  private ws: WebSocket | null = null;
  private shouldReconnect = true;
  private reconnectDelay = 1000; // initial
  private maxDelay = 30000;
  private onMessageHandlers: MessageHandler[] = [];
  public onOpen: (() => void) | null = null;
  public onClose: ((ev?: CloseEvent) => void) | null = null;

  constructor(url: string) {
    this.url = url;
    this.connect();
  }

  private connect() {
    this.ws = new WebSocket(this.url);
    this.ws.addEventListener('open', () => {
      this.reconnectDelay = 1000;
      if (this.onOpen) this.onOpen();
    });
    this.ws.addEventListener('message', (ev) => {
      let data: unknown = ev.data;
  try { data = JSON.parse(ev.data as string) as unknown; } catch { /* text message */ }
      this.onMessageHandlers.forEach(h => h(data));
    });
    this.ws.addEventListener('close', (ev) => {
      if (this.onClose) this.onClose(ev);
      if (!this.shouldReconnect) return;
      setTimeout(() => this.connect(), this.reconnectDelay);
      this.reconnectDelay = Math.min(this.reconnectDelay * 1.5, this.maxDelay);
    });
    this.ws.addEventListener('error', () => {
      // ensure socket is closed to trigger reconnect behavior
  try { this.ws?.close(); } catch { /* ignore */ }
    });
  }

  send(obj: unknown) {
    const payload = typeof obj === 'string' ? obj : JSON.stringify(obj);
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(payload);
      return true;
    }
    return false;
  }

  addMessageListener(fn: MessageHandler) {
    this.onMessageHandlers.push(fn);
    return () => {
      this.onMessageHandlers = this.onMessageHandlers.filter(h => h !== fn);
    };
  }

  close() {
    this.shouldReconnect = false;
    try { this.ws?.close(); } catch { }
  }
}
