export type User = { id: number; name: string; online: boolean; lastSeen?: string };
export type Message = { id?: number; sender: string; recipient?: string | null; content: string; timestamp: string };

const base = '/api';

export async function listUsers(): Promise<User[]> {
  const res = await fetch(`${base}/users`);
  if (!res.ok) throw new Error('Falha ao listar usuários');
  return res.json();
}

export async function listOnline(): Promise<User[]> {
  const res = await fetch(`${base}/users/online`);
  if (!res.ok) throw new Error('Falha ao listar online');
  return res.json();
}

export async function register(name: string): Promise<User> {
  const res = await fetch(`${base}/users/register?name=${encodeURIComponent(name)}`, { method: 'POST' });
  if (!res.ok) throw new Error('Falha ao registrar usuário');
  return res.json();
}

export async function logout(name: string): Promise<void> {
  const res = await fetch(`${base}/users/logout?name=${encodeURIComponent(name)}`, { method: 'POST' });
  if (!res.ok) throw new Error('Falha ao deslogar');
}

// Fetch messages. dateFrom/dateTo are ISO date-time strings (optional)
export async function getMessages(withUser?: string, dateFrom?: string, dateTo?: string): Promise<Message[]> {
  const params = new URLSearchParams();
  if (withUser) params.set('with', withUser);
  if (dateFrom) params.set('dateFrom', dateFrom);
  if (dateTo) params.set('dateTo', dateTo);
  const res = await fetch(`${base}/messages?${params.toString()}`);
  if (!res.ok) throw new Error('Falha ao buscar mensagens');
  return res.json();
}
