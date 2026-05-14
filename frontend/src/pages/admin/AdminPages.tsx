import { FormEvent, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit2, Plus, Save, Trash2, X } from 'lucide-react';
import { api } from '../../services/api';
import { Card } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';

type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';
type User = { id: string; email: string; fullName: string; enabled: boolean; roles: Role[] };
type UserForm = { email: string; password: string; fullName: string; role: Role; enabled: boolean };

const emptyForm: UserForm = { email: '', password: '', fullName: '', role: 'STUDENT', enabled: true };
const roleText: Record<Role, string> = { ADMIN: 'Quản trị', TEACHER: 'Giáo viên', STUDENT: 'Học sinh' };

export function DashboardStats() {
  const { data, isLoading } = useQuery({ queryKey: ['admin-stats'], queryFn: async () => (await api.get('/admin/statistics')).data.data });
  const labels: Record<string, string> = { users: 'Tài khoản', media: 'Tư liệu', pendingApprovals: 'Chờ duyệt' };
  return <div><h2 className="mb-4 text-xl font-semibold">Tổng quan hệ thống</h2><div className="grid gap-4 sm:grid-cols-3">{Object.keys(labels).map(k => <Card key={k}><p className="text-sm text-muted">{labels[k]}</p><p className="mt-1 text-3xl font-bold text-primary">{isLoading ? '...' : data?.[k] ?? 0}</p></Card>)}</div></div>;
}

export function UsersPage() {
  const qc = useQueryClient();
  const [form, setForm] = useState<UserForm>(emptyForm);
  const [editing, setEditing] = useState<User | null>(null);
  const { data = [], isLoading } = useQuery({ queryKey: ['users'], queryFn: async () => (await api.get('/users', { params: { size: 100 } })).data.data.content as User[] });
  const refresh = () => qc.invalidateQueries({ queryKey: ['users'] });
  const create = useMutation({ mutationFn: async () => api.post('/users', { email: form.email, password: form.password, fullName: form.fullName, roles: [form.role] }), onSuccess: () => { setForm(emptyForm); refresh(); } });
  const update = useMutation({ mutationFn: async (user: User) => api.put(`/users/${user.id}`, { fullName: user.fullName, enabled: user.enabled, roles: user.roles }), onSuccess: () => { setEditing(null); refresh(); } });
  const remove = useMutation({ mutationFn: async (id: string) => api.delete(`/users/${id}`), onSuccess: refresh });
  const submit = (event: FormEvent) => { event.preventDefault(); create.mutate(); };

  return <div className="grid gap-4 lg:grid-cols-[360px_1fr]">
    <Card>
      <h2 className="text-xl font-semibold">Tạo tài khoản</h2>
      <form onSubmit={submit} className="mt-4 grid gap-3">
        <Input required placeholder="Họ và tên" value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />
        <Input required type="email" placeholder="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
        <Input required minLength={8} type="password" placeholder="Mật khẩu" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
        <select className="form-field" value={form.role} onChange={e => setForm({ ...form, role: e.target.value as Role })}>
          <option value="STUDENT">Học sinh</option><option value="TEACHER">Giáo viên</option><option value="ADMIN">Quản trị</option>
        </select>
        <Button disabled={create.isPending}><Plus size={16} />Tạo mới</Button>
      </form>
    </Card>
    <div>
      <h2 className="mb-4 text-xl font-semibold">Danh sách tài khoản</h2>
      <div className="space-y-2">
        {isLoading && <div className="skeleton h-24 rounded-lg" />}
        {data.map(user => editing?.id === user.id ? <Card key={user.id} className="grid gap-3 md:grid-cols-[1fr_170px_140px]">
          <Input value={editing.fullName} onChange={e => setEditing({ ...editing, fullName: e.target.value })} />
          <select className="form-field" value={editing.roles[0]} onChange={e => setEditing({ ...editing, roles: [e.target.value as Role] })}>
            <option value="STUDENT">Học sinh</option><option value="TEACHER">Giáo viên</option><option value="ADMIN">Quản trị</option>
          </select>
          <div className="flex gap-2">
            <Button title="Lưu" onClick={() => update.mutate(editing)}><Save size={16} /></Button>
            <Button title="Hủy" type="button" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setEditing(null)}><X size={16} /></Button>
          </div>
        </Card> : <Card key={user.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div><b>{user.fullName}</b><p className="text-sm text-muted">{user.email} - {user.roles.map(role => roleText[role]).join(', ')} - {user.enabled ? 'Đang hoạt động' : 'Tạm khóa'}</p></div>
          <div className="flex gap-2">
            <Button title="Sửa" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setEditing(user)}><Edit2 size={16} /></Button>
            <Button title="Xóa" className="bg-danger" onClick={() => remove.mutate(user.id)}><Trash2 size={16} /></Button>
          </div>
        </Card>)}
      </div>
    </div>
  </div>;
}

export function ApprovalsPage() {
  const { data = [], refetch } = useQuery({ queryKey: ['pending'], queryFn: async () => (await api.get('/admin/pending-approvals')).data.data.content });
  const approve = async (id: string, status: string) => { await api.post(`/admin/pending-approvals/${id}`, { status }); refetch(); };
  return <div><h2 className="mb-4 text-xl font-semibold">Tư liệu chờ duyệt</h2><div className="space-y-2">{data.length === 0 && <Card>Không có tư liệu nào đang chờ duyệt.</Card>}{data.map((m: any) => <Card key={m.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><b>{m.title}</b><p className="text-sm text-muted">{m.type === 'VIDEO' ? 'Video' : 'Hình ảnh'} - {m.status === 'PENDING' ? 'Chờ duyệt' : m.status}</p></div><div className="flex gap-2"><Button onClick={() => approve(m.id,'APPROVED')}>Duyệt</Button><Button className="bg-danger text-white hover:bg-rose-400" onClick={() => approve(m.id,'REJECTED')}>Từ chối</Button></div></Card>)}</div></div>;
}

export function ReportsPage() { return <Card><h2 className="text-xl font-semibold">Báo cáo</h2><p className="mt-2 text-muted">Nhật ký hoạt động và báo cáo hệ thống đã sẵn sàng để tích hợp công cụ phân tích.</p></Card>; }
