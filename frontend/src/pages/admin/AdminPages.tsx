import { FormEvent, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit2, Plus, Save, Trash2, X } from 'lucide-react';
import { api } from '../../services/api';
import { Card } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { useAuthStore } from '../../stores/auth';

type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';
type User = { id: string; email: string; fullName: string; enabled: boolean; roles: Role[]; teacherId?: string | null; teacherName?: string | null; address?: string | null; guardianName?: string | null; guardianPhone?: string | null; hometown?: string | null; allergies?: string | null };
type UserForm = { email: string; password: string; fullName: string; role: Role; teacherId: string; address: string; guardianName: string; guardianPhone: string; hometown: string; allergies: string };
type Media = { id: string; ownerId: string; ownerName: string; uploadedById: string; uploadedByName: string; title: string; description?: string; type: 'VIDEO' | 'IMAGE'; status: 'PENDING' | 'APPROVED' | 'REJECTED'; contentType?: string; sizeBytes: number };
type DevelopmentReport = { id: string; studentName: string; teacherName: string; weekStart: string; physicalChange?: string; cognitiveChange?: string; socialChange?: string; emotionalChange?: string; note: string };

const emptyForm: UserForm = { email: '', password: '', fullName: '', role: 'STUDENT', teacherId: '', address: '', guardianName: '', guardianPhone: '', hometown: '', allergies: '' };
const roleText: Record<Role, string> = { ADMIN: 'Quản trị', TEACHER: 'Giáo viên', STUDENT: 'Học sinh' };

function usersFromResponse(res: any) {
  return res.data.data.content as User[];
}

function mediaUrl(path: string) {
  const base = String(api.defaults.baseURL ?? '/api/v1').replace(/\/$/, '');
  const token = useAuthStore.getState().accessToken;
  return `${base}${path}?access_token=${encodeURIComponent(token ?? '')}`;
}

export function DashboardStats() {
  const { data, isLoading } = useQuery({ queryKey: ['admin-stats'], queryFn: async () => (await api.get('/admin/statistics')).data.data });
  const labels: Record<string, string> = { users: 'Tài khoản', media: 'Tư liệu', pendingApprovals: 'Chờ duyệt' };
  return <div><h2 className="mb-4 text-xl font-semibold">Tổng quan hệ thống</h2><div className="grid gap-4 sm:grid-cols-3">{Object.keys(labels).map(k => <Card key={k}><p className="text-sm text-muted">{labels[k]}</p><p className="mt-1 text-3xl font-bold text-primary">{isLoading ? '...' : data?.[k] ?? 0}</p></Card>)}</div></div>;
}

export function UsersPage() {
  const qc = useQueryClient();
  const [form, setForm] = useState<UserForm>(emptyForm);
  const [editing, setEditing] = useState<User | null>(null);
  const { data = [], isLoading } = useQuery({ queryKey: ['users'], queryFn: async () => usersFromResponse(await api.get('/users', { params: { size: 100 } })) });
  const { data: teachers = [] } = useQuery({ queryKey: ['teachers'], queryFn: async () => usersFromResponse(await api.get('/users/teachers', { params: { size: 100 } })) });
  const selectedRoleIsStudent = form.role === 'STUDENT';
  const canCreate = !selectedRoleIsStudent || Boolean(form.teacherId);
  const refresh = () => {
    qc.invalidateQueries({ queryKey: ['users'] });
    qc.invalidateQueries({ queryKey: ['teachers'] });
  };
  const createPayload = useMemo(() => ({
    email: form.email,
    password: form.password,
    fullName: form.fullName,
    roles: [form.role],
    teacherId: selectedRoleIsStudent ? form.teacherId : null,
    address: form.address,
    guardianName: form.guardianName,
    guardianPhone: form.guardianPhone,
    hometown: form.hometown,
    allergies: form.allergies
  }), [form, selectedRoleIsStudent]);
  const create = useMutation({ mutationFn: async () => api.post('/users', createPayload), onSuccess: () => { setForm(emptyForm); refresh(); } });
  const update = useMutation({
    mutationFn: async (user: User) => api.put(`/users/${user.id}`, {
      fullName: user.fullName,
      enabled: user.enabled,
      roles: user.roles,
      teacherId: user.teacherId ?? null,
      address: user.address ?? '',
      guardianName: user.guardianName ?? '',
      guardianPhone: user.guardianPhone ?? '',
      hometown: user.hometown ?? '',
      allergies: user.allergies ?? ''
    }),
    onSuccess: () => { setEditing(null); refresh(); }
  });
  const remove = useMutation({ mutationFn: async (id: string) => api.delete(`/users/${id}`), onSuccess: refresh });
  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (!canCreate) return;
    create.mutate();
  };

  return <div className="grid gap-4 lg:grid-cols-[400px_1fr]">
    <Card>
      <h2 className="text-xl font-semibold">Tạo tài khoản</h2>
      <form onSubmit={submit} className="mt-4 grid gap-3">
        <Input required placeholder="Họ và tên" value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />
        <Input required type="email" placeholder="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
        <Input required minLength={8} type="password" placeholder="Mật khẩu" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
        <select className="form-field" value={form.role} onChange={e => setForm({ ...emptyForm, role: e.target.value as Role, fullName: form.fullName, email: form.email, password: form.password })}>
          <option value="STUDENT">Học sinh</option>
          <option value="TEACHER">Giáo viên</option>
          <option value="ADMIN">Quản trị</option>
        </select>
        {selectedRoleIsStudent && <>
          <select className="form-field" required value={form.teacherId} onChange={e => setForm({ ...form, teacherId: e.target.value })}>
            <option value="">Chọn giáo viên phụ trách</option>
            {teachers.map(teacher => <option key={teacher.id} value={teacher.id}>{teacher.fullName} - {teacher.email}</option>)}
          </select>
          <Input placeholder="Địa chỉ" value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} />
          <Input placeholder="Tên phụ huynh" value={form.guardianName} onChange={e => setForm({ ...form, guardianName: e.target.value })} />
          <Input placeholder="Số điện thoại phụ huynh" value={form.guardianPhone} onChange={e => setForm({ ...form, guardianPhone: e.target.value })} />
          <Input placeholder="Quê quán" value={form.hometown} onChange={e => setForm({ ...form, hometown: e.target.value })} />
          <textarea className="form-field min-h-24" placeholder="Dị ứng / lưu ý sức khỏe" value={form.allergies} onChange={e => setForm({ ...form, allergies: e.target.value })} />
          {teachers.length === 0 && <p className="text-sm text-danger">Cần tạo ít nhất một tài khoản giáo viên trước khi tạo học sinh.</p>}
        </>}
        <Button disabled={create.isPending || !canCreate}><Plus size={16} />Tạo mới</Button>
      </form>
    </Card>
    <div>
      <h2 className="mb-4 text-xl font-semibold">Danh sách tài khoản</h2>
      <div className="space-y-2">
        {isLoading && <div className="skeleton h-24 rounded-lg" />}
        {data.map(user => editing?.id === user.id ? <Card key={user.id} className="grid gap-3">
          <Input value={editing.fullName} onChange={e => setEditing({ ...editing, fullName: e.target.value })} />
          <select className="form-field" value={editing.roles[0]} onChange={e => setEditing({ ...editing, roles: [e.target.value as Role] })}>
            <option value="STUDENT">Học sinh</option><option value="TEACHER">Giáo viên</option><option value="ADMIN">Quản trị</option>
          </select>
          {editing.roles[0] === 'STUDENT' && <>
            <select className="form-field" value={editing.teacherId ?? ''} onChange={e => setEditing({ ...editing, teacherId: e.target.value })}>
              <option value="">Chọn giáo viên phụ trách</option>
              {teachers.map(teacher => <option key={teacher.id} value={teacher.id}>{teacher.fullName} - {teacher.email}</option>)}
            </select>
            <div className="grid gap-3 md:grid-cols-2">
              <Input placeholder="Địa chỉ" value={editing.address ?? ''} onChange={e => setEditing({ ...editing, address: e.target.value })} />
              <Input placeholder="Tên phụ huynh" value={editing.guardianName ?? ''} onChange={e => setEditing({ ...editing, guardianName: e.target.value })} />
              <Input placeholder="Số điện thoại phụ huynh" value={editing.guardianPhone ?? ''} onChange={e => setEditing({ ...editing, guardianPhone: e.target.value })} />
              <Input placeholder="Quê quán" value={editing.hometown ?? ''} onChange={e => setEditing({ ...editing, hometown: e.target.value })} />
            </div>
            <textarea className="form-field min-h-24" placeholder="Dị ứng / lưu ý sức khỏe" value={editing.allergies ?? ''} onChange={e => setEditing({ ...editing, allergies: e.target.value })} />
          </>}
          <div className="flex gap-2">
            <Button title="Lưu" onClick={() => update.mutate(editing)}><Save size={16} /></Button>
            <Button title="Hủy" type="button" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setEditing(null)}><X size={16} /></Button>
          </div>
        </Card> : <Card key={user.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div><b>{user.fullName}</b><p className="text-sm text-muted">{user.email} - {user.roles.map(role => roleText[role]).join(', ')}{user.teacherName ? ` - GV phụ trách: ${user.teacherName}` : ''} - {user.enabled ? 'Đang hoạt động' : 'Tạm khóa'}</p>{user.roles.includes('STUDENT') && <p className="text-sm text-muted">PH: {user.guardianName || 'Chưa cập nhật'} - SĐT: {user.guardianPhone || 'Chưa cập nhật'} - Dị ứng: {user.allergies || 'Không ghi nhận'}</p>}</div>
          <div className="flex gap-2">
            <Button title="Sửa" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setEditing(user)}><Edit2 size={16} /></Button>
            <Button title="Xóa" className="bg-danger" onClick={() => remove.mutate(user.id)}><Trash2 size={16} /></Button>
          </div>
        </Card>)}
      </div>
    </div>
  </div>;
}

function MediaPreview({ item }: { item: Media }) {
  const src = mediaUrl(`/media/stream/${item.id}`);
  return item.type === 'VIDEO'
    ? <video className="aspect-video w-full rounded-md bg-slate-950 object-cover" src={src} preload="metadata" controls playsInline />
    : <img className="aspect-video w-full rounded-md bg-slate-950 object-cover" src={src} loading="lazy" alt={item.title} />;
}

export function AdminMediaPage() {
  const { data = [], isLoading } = useQuery({ queryKey: ['admin-media'], queryFn: async () => (await api.get('/media', { params: { size: 100 } })).data.data.content as Media[] });
  const statusText: Record<Media['status'], string> = { PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' };
  const typeText: Record<Media['type'], string> = { VIDEO: 'Video', IMAGE: 'Hình ảnh' };

  return <div>
    <h2 className="mb-4 text-xl font-semibold">Media giáo viên đã đăng</h2>
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {isLoading && [0, 1, 2].map(i => <div key={i} className="skeleton h-64 rounded-lg" />)}
      {!isLoading && data.length === 0 && <Card>Chưa có ảnh hoặc video nào được đăng.</Card>}
      {data.map(item => <Card key={item.id}>
        <MediaPreview item={item} />
        <div className="mt-3 space-y-1">
          <div className="flex items-start justify-between gap-3">
            <b>{item.title}</b>
            <span className="rounded-md border border-border px-2 py-1 text-xs text-muted">{statusText[item.status]}</span>
          </div>
          <p className="text-sm text-muted">{typeText[item.type]} - Học sinh: {item.ownerName}</p>
          <p className="text-sm text-muted">Người đăng: {item.uploadedByName}</p>
          {item.description && <p className="text-sm">{item.description}</p>}
        </div>
      </Card>)}
    </div>
  </div>;
}

export function AdminDevelopmentPage() {
  const { data = [], isLoading } = useQuery({ queryKey: ['admin-development'], queryFn: async () => (await api.get('/development-reports', { params: { size: 100 } })).data.data.content as DevelopmentReport[] });
  return <div>
    <h2 className="mb-4 text-xl font-semibold">Lộ trình phát triển học sinh</h2>
    <div className="space-y-3">
      {isLoading && <div className="skeleton h-28 rounded-lg" />}
      {!isLoading && data.length === 0 && <Card>Chưa có báo cáo phát triển nào.</Card>}
      {data.map(report => <Card key={report.id}>
        <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
          <div><b>{report.studentName}</b><p className="text-sm text-muted">Giáo viên: {report.teacherName}</p></div>
          <span className="rounded-md border border-border px-2 py-1 text-xs text-muted">Tuần {report.weekStart}</span>
        </div>
        <div className="mt-3 grid gap-2 md:grid-cols-2">
          <p><b>Thể chất:</b> {report.physicalChange || 'Không ghi nhận'}</p>
          <p><b>Nhận thức:</b> {report.cognitiveChange || 'Không ghi nhận'}</p>
          <p><b>Xã hội:</b> {report.socialChange || 'Không ghi nhận'}</p>
          <p><b>Cảm xúc:</b> {report.emotionalChange || 'Không ghi nhận'}</p>
        </div>
        <p className="mt-3"><b>Ghi chú:</b> {report.note}</p>
      </Card>)}
    </div>
  </div>;
}

export function ApprovalsPage() {
  const { data = [], refetch } = useQuery({ queryKey: ['pending'], queryFn: async () => (await api.get('/admin/pending-approvals')).data.data.content });
  const approve = async (id: string, status: string) => { await api.post(`/admin/pending-approvals/${id}`, { status }); refetch(); };
  return <div><h2 className="mb-4 text-xl font-semibold">Tư liệu chờ duyệt</h2><div className="space-y-2">{data.length === 0 && <Card>Không có tư liệu nào đang chờ duyệt.</Card>}{data.map((m: any) => <Card key={m.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><b>{m.title}</b><p className="text-sm text-muted">{m.type === 'VIDEO' ? 'Video' : 'Hình ảnh'} - {m.status === 'PENDING' ? 'Chờ duyệt' : m.status}</p></div><div className="flex gap-2"><Button onClick={() => approve(m.id,'APPROVED')}>Duyệt</Button><Button className="bg-danger text-white hover:bg-rose-400" onClick={() => approve(m.id,'REJECTED')}>Từ chối</Button></div></Card>)}</div></div>;
}

export function ReportsPage() { return <Card><h2 className="text-xl font-semibold">Báo cáo</h2><p className="mt-2 text-muted">Nhật ký hoạt động và báo cáo hệ thống đã sẵn sàng để tích hợp công cụ phân tích.</p></Card>; }
