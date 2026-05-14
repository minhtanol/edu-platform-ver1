import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Download, Send } from 'lucide-react';
import { api } from '../../services/api';
import { Card } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { usePullToRefresh } from '../../hooks/usePullToRefresh';
import { useAuthStore } from '../../stores/auth';

type Media = { id: string; title: string; description?: string; type: 'VIDEO' | 'IMAGE'; status: 'PENDING' | 'APPROVED' | 'REJECTED'; contentType?: string };

function useStudentId() {
  const id = useAuthStore(s => s.id);
  const me = useQuery({ queryKey: ['me'], enabled: !id, queryFn: async () => (await api.get('/users/me')).data.data as { id: string } });
  return id ?? me.data?.id ?? '';
}

function mediaUrl(path: string) {
  const base = String(api.defaults.baseURL ?? '/api/v1').replace(/\/$/, '');
  const token = useAuthStore.getState().accessToken;
  return `${base}${path}?access_token=${encodeURIComponent(token ?? '')}`;
}

export function StudentHomePage() {
  const studentId = useStudentId();
  const media = useQuery({ queryKey: ['media', studentId], enabled: Boolean(studentId), queryFn: async () => (await api.get(`/media/student/${studentId}`)).data.data.content as Media[] });
  const evals = useQuery({ queryKey: ['evals', studentId], enabled: Boolean(studentId), queryFn: async () => (await api.get(`/evaluations/student/${studentId}`)).data.data.content });
  return <div className="grid gap-4 sm:grid-cols-3"><Card><h2 className="text-xl font-semibold">Tư liệu đã duyệt</h2><p className="mt-2 text-3xl font-bold text-primary">{media.data?.filter(m => m.status === 'APPROVED').length ?? 0}</p></Card><Card><h2 className="text-xl font-semibold">Nhận xét</h2><p className="mt-2 text-3xl font-bold text-accent">{evals.data?.length ?? 0}</p></Card><Card><h2 className="text-xl font-semibold">Trợ lý AI</h2><p className="mt-2 text-muted">Sẵn sàng hỗ trợ câu hỏi học tập.</p></Card></div>;
}

export function VideosPage() {
  const studentId = useStudentId();
  const q = useQuery({ queryKey: ['media', studentId, 'video'], enabled: Boolean(studentId), queryFn: async () => (await api.get(`/media/student/${studentId}`)).data.data.content as Media[] });
  usePullToRefresh(q.refetch);
  return <MediaList title="Video bài học" items={q.data?.filter(m => m.type === 'VIDEO' && m.status === 'APPROVED')} loading={q.isLoading} />;
}

export function GalleryPage() {
  const studentId = useStudentId();
  const q = useQuery({ queryKey: ['media', studentId, 'image'], enabled: Boolean(studentId), queryFn: async () => (await api.get(`/media/student/${studentId}`)).data.data.content as Media[] });
  return <MediaList title="Thư viện hình ảnh" items={q.data?.filter(m => m.type === 'IMAGE' && m.status === 'APPROVED')} loading={q.isLoading} />;
}

function MediaPreview({ item }: { item: Media }) {
  const stream = mediaUrl(`/media/stream/${item.id}`);
  return item.type === 'VIDEO'
    ? <video className="aspect-video w-full rounded-md bg-slate-950 object-cover" src={stream} preload="metadata" controls playsInline />
    : <img className="aspect-video w-full rounded-md bg-slate-950 object-cover" src={stream} loading="lazy" alt={item.title} />;
}

function MediaList({ title, items = [], loading }: { title: string; items?: Media[]; loading?: boolean }) {
  return <div><h2 className="mb-4 text-xl font-semibold">{title}</h2><div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
    {loading && [0,1,2].map(i => <div key={i} className="skeleton h-40 rounded-lg" />)}
    {!loading && items.length === 0 && <Card>Chưa có tư liệu nào được duyệt.</Card>}
    {items.map(m => <Card key={m.id}><MediaPreview item={m} /><div className="mt-3 flex items-start justify-between gap-3"><div><b className="block">{m.title}</b>{m.description && <p className="text-sm text-muted">{m.description}</p>}</div><a className="touch-target rounded-md p-2 text-muted hover:bg-white/10 hover:text-foreground" title="Tải xuống" href={mediaUrl(`/media/download/${m.id}`)}><Download size={18} /></a></div></Card>)}
  </div></div>;
}

export function StudentEvaluationsPage() {
  const studentId = useStudentId();
  const { data = [] } = useQuery({ queryKey: ['evals', studentId], enabled: Boolean(studentId), queryFn: async () => (await api.get(`/evaluations/student/${studentId}`)).data.data.content });
  return <div><h2 className="mb-4 text-xl font-semibold">Nhận xét của giáo viên</h2><div className="space-y-2">{data.length === 0 && <Card>Chưa có nhận xét nào.</Card>}{data.map((e: any) => <Card key={e.id}><b>{e.subject}</b><p>{e.content}</p><span className="text-sm text-muted">Điểm {e.score}</span></Card>)}</div></div>;
}

export function ChatAIPage() {
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<{ role: 'Bạn' | 'AI'; text: string }[]>([]);
  const send = async () => {
    const text = input.trim();
    if (!text) return;
    setInput('');
    setMessages(m => [...m, { role: 'Bạn', text }]);
    const res = await api.post('/ai/chat', { message: text });
    setMessages(m => [...m, { role: 'AI', text: res.data.data.message }]);
  };
  return <Card className="mx-auto max-w-3xl"><h2 className="text-xl font-semibold">Trợ lý học tập AI</h2><div className="mt-4 h-96 overflow-auto rounded-md border border-border bg-slate-950/60 p-3">{messages.map((m,i)=><p key={i} className="mb-2"><b>{m.role}:</b> {m.text}</p>)}</div><div className="mt-3 flex gap-2"><Input value={input} onChange={e=>setInput(e.target.value)} placeholder="Nhập câu hỏi về bài học" onKeyDown={e => { if (e.key === 'Enter') send(); }} /><Button onClick={send}><Send size={16} />Gửi</Button></div></Card>;
}
