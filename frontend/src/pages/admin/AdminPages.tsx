import { FormEvent, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit2, History, Plus, Save, Trash2, X } from 'lucide-react';
import { api } from '../../services/api';
import { Card } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { useAuthStore } from '../../stores/auth';
import { BrandLogo, BrandName } from '../../components/BrandLogo';

type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';
type Gender = 'MALE' | 'FEMALE' | 'OTHER';
type StudyStatus = 'ACTIVE' | 'INACTIVE';
type User = { id: string; email: string; fullName: string; enabled: boolean; roles: Role[]; teacherId?: string | null; teacherName?: string | null; address?: string | null; guardianName?: string | null; guardianPhone?: string | null; hometown?: string | null; allergies?: string | null; dateOfBirth?: string | null; age?: number | null; gender?: Gender | null; emergencyContactName?: string | null; emergencyContactPhone?: string | null; studyStatus?: StudyStatus | null };
type UserForm = { email: string; password: string; fullName: string; role: Role; teacherId: string; address: string; guardianName: string; guardianPhone: string; hometown: string; allergies: string; dateOfBirth: string; gender: '' | Gender; emergencyContactName: string; emergencyContactPhone: string; studyStatus: StudyStatus };
type Media = { id: string; ownerName: string; uploadedByName: string; title: string; description?: string; type: 'VIDEO' | 'IMAGE'; status: 'PENDING' | 'APPROVED' | 'REJECTED' };
type DevelopmentReport = { id: string; studentName: string; teacherName: string; weekStart: string; physicalChange?: string; cognitiveChange?: string; socialChange?: string; emotionalChange?: string; note: string };
type ProfileHistory = { id: string; studentName: string; actorName?: string | null; beforeValue?: string | null; afterValue: string; createdAt: string };
type DashboardData = { users: number; students: number; teachers: number; media: { uploaded: number; approved: number; rejected: number; pending: number }; evaluationsThisWeek: number; developmentReportsThisWeek: number; studentsByTeacher: { teacherId: string; teacherName: string; studentCount: number }[]; studentsMissingEvaluationThisWeek: { studentId: string; studentName: string; teacherId?: string | null; teacherName?: string | null }[]; weekStart: string; weekEnd: string };

const emptyForm: UserForm = { email: '', password: '', fullName: '', role: 'STUDENT', teacherId: '', address: '', guardianName: '', guardianPhone: '', hometown: '', allergies: '', dateOfBirth: '', gender: '', emergencyContactName: '', emergencyContactPhone: '', studyStatus: 'ACTIVE' };
const roleText: Record<Role, string> = { ADMIN: 'Quản trị', TEACHER: 'Giáo viên', STUDENT: 'Học sinh' };
const genderText: Record<Gender, string> = { MALE: 'Nam', FEMALE: 'Nữ', OTHER: 'Khác' };
const statusText: Record<StudyStatus, string> = { ACTIVE: 'Đang học', INACTIVE: 'Nghỉ học' };

function usersFromResponse(res: any) { return res.data.data.content as User[]; }
function mediaUrl(path: string) {
  const base = String(api.defaults.baseURL ?? '/api/v1').replace(/\/$/, '');
  const token = useAuthStore.getState().accessToken;
  return `${base}${path}?access_token=${encodeURIComponent(token ?? '')}`;
}
function studentPayload(form: UserForm) {
  return {
    address: form.address, guardianName: form.guardianName, guardianPhone: form.guardianPhone, hometown: form.hometown, allergies: form.allergies,
    dateOfBirth: form.dateOfBirth || null, gender: form.gender || null, emergencyContactName: form.emergencyContactName, emergencyContactPhone: form.emergencyContactPhone, studyStatus: form.studyStatus
  };
}

export function DashboardStats() {
  const { data, isLoading } = useQuery({ queryKey: ['admin-stats'], queryFn: async () => (await api.get('/admin/statistics')).data.data });
  const labels: Record<string, string> = { users: 'Tài khoản', media: 'Tư liệu', pendingApprovals: 'Chờ duyệt' };
  return <div><h2 className="mb-4 text-xl font-semibold">Tổng quan hệ thống</h2><div className="grid gap-4 sm:grid-cols-3">{Object.keys(labels).map(k => <Card key={k}><p className="text-sm text-muted">{labels[k]}</p><p className="mt-1 text-3xl font-bold text-primary">{isLoading ? '...' : data?.[k] ?? 0}</p></Card>)}</div></div>;
}

export function RealDashboardStats() {
  const { data, isLoading } = useQuery({ queryKey: ['admin-stats'], queryFn: async () => (await api.get('/admin/statistics')).data.data as DashboardData });
  const cards = [
    ['Tài khoản', data?.users],
    ['Học sinh', data?.students],
    ['Giáo viên', data?.teachers],
    ['Media đã upload', data?.media.uploaded],
    ['Đã duyệt', data?.media.approved],
    ['Bị từ chối', data?.media.rejected],
    ['Chờ duyệt', data?.media.pending],
    ['Đánh giá tuần này', data?.evaluationsThisWeek],
    ['Lộ trình tuần này', data?.developmentReportsThisWeek]
  ];
  return <div>
    <div className="mb-4 flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
      <h2 className="text-xl font-semibold">Tổng quan hệ thống</h2>
      {data && <p className="text-sm text-muted">Tuần {data.weekStart} đến {data.weekEnd}</p>}
    </div>
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{cards.map(([label, value]) => <Card key={String(label)}><p className="text-sm text-muted">{label}</p><p className="mt-1 text-3xl font-bold text-primary">{isLoading ? '...' : value ?? 0}</p></Card>)}</div>
    <div className="mt-4 grid gap-4 xl:grid-cols-2">
      <Card><h3 className="font-semibold">Số học sinh theo giáo viên</h3><div className="mt-3 space-y-2">{data?.studentsByTeacher.length === 0 && <p className="text-sm text-muted">Chưa có giáo viên hoặc học sinh liên kết.</p>}{data?.studentsByTeacher.map(item => <div key={item.teacherId} className="flex items-center justify-between rounded-md border border-border p-3"><span>{item.teacherName}</span><b className="text-primary">{item.studentCount}</b></div>)}</div></Card>
      <Card><h3 className="font-semibold">Học sinh chưa có đánh giá tuần này</h3><div className="mt-3 space-y-2">{data?.studentsMissingEvaluationThisWeek.length === 0 && <p className="text-sm text-muted">Tất cả học sinh đã có đánh giá trong tuần.</p>}{data?.studentsMissingEvaluationThisWeek.map(item => <div key={item.studentId} className="rounded-md border border-border p-3"><b>{item.studentName}</b><p className="text-sm text-muted">Giáo viên: {item.teacherName ?? 'Chưa liên kết'}</p></div>)}</div></Card>
    </div>
  </div>;
}

export function UsersPage() {
  const qc = useQueryClient();
  const [form, setForm] = useState<UserForm>(emptyForm);
  const [editing, setEditing] = useState<User | null>(null);
  const [historyUser, setHistoryUser] = useState<User | null>(null);
  const { data = [], isLoading } = useQuery({ queryKey: ['users'], queryFn: async () => usersFromResponse(await api.get('/users', { params: { size: 100 } })) });
  const { data: teachers = [] } = useQuery({ queryKey: ['teachers'], queryFn: async () => usersFromResponse(await api.get('/users/teachers', { params: { size: 100 } })) });
  const { data: history = [] } = useQuery({ queryKey: ['profile-history', historyUser?.id], enabled: Boolean(historyUser), queryFn: async () => (await api.get(`/users/${historyUser!.id}/profile-history`)).data.data.content as ProfileHistory[] });
  const selectedRoleIsStudent = form.role === 'STUDENT';
  const canCreate = !selectedRoleIsStudent || Boolean(form.teacherId);
  const refresh = () => { qc.invalidateQueries({ queryKey: ['users'] }); qc.invalidateQueries({ queryKey: ['teachers'] }); };
  const createPayload = useMemo(() => ({ email: form.email, password: form.password, fullName: form.fullName, roles: [form.role], teacherId: selectedRoleIsStudent ? form.teacherId : null, ...studentPayload(form) }), [form, selectedRoleIsStudent]);
  const create = useMutation({ mutationFn: async () => api.post('/users', createPayload), onSuccess: () => { setForm(emptyForm); refresh(); } });
  const update = useMutation({
    mutationFn: async (user: User) => api.put(`/users/${user.id}`, { fullName: user.fullName, enabled: user.enabled, roles: user.roles, teacherId: user.teacherId ?? null, address: user.address ?? '', guardianName: user.guardianName ?? '', guardianPhone: user.guardianPhone ?? '', hometown: user.hometown ?? '', allergies: user.allergies ?? '', dateOfBirth: user.dateOfBirth || null, gender: user.gender || null, emergencyContactName: user.emergencyContactName ?? '', emergencyContactPhone: user.emergencyContactPhone ?? '', studyStatus: user.studyStatus ?? 'ACTIVE' }),
    onSuccess: () => { setEditing(null); refresh(); if (historyUser) qc.invalidateQueries({ queryKey: ['profile-history', historyUser.id] }); }
  });
  const remove = useMutation({ mutationFn: async (id: string) => api.delete(`/users/${id}`), onSuccess: refresh });
  const submit = (event: FormEvent) => { event.preventDefault(); if (canCreate) create.mutate(); };

  return <div className="grid gap-4 lg:grid-cols-[420px_1fr]">
    <Card>
      <h2 className="text-xl font-semibold">Tạo tài khoản</h2>
      <form onSubmit={submit} className="mt-4 grid gap-3">
        <Input required placeholder="Họ và tên" value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />
        <Input required type="email" placeholder="Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
        <Input required minLength={8} type="password" placeholder="Mật khẩu" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
        <select className="form-field" value={form.role} onChange={e => setForm({ ...emptyForm, role: e.target.value as Role, fullName: form.fullName, email: form.email, password: form.password })}>
          <option value="STUDENT">Học sinh</option><option value="TEACHER">Giáo viên</option><option value="ADMIN">Quản trị</option>
        </select>
        {selectedRoleIsStudent && <StudentFormFields form={form} setForm={setForm} teachers={teachers} />}
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
          {editing.roles[0] === 'STUDENT' && <StudentEditFields editing={editing} setEditing={setEditing} teachers={teachers} />}
          <div className="flex gap-2">
            <Button title="Lưu" onClick={() => update.mutate(editing)}><Save size={16} /></Button>
            <Button title="Hủy" type="button" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setEditing(null)}><X size={16} /></Button>
          </div>
        </Card> : <Card key={user.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div><b>{user.fullName}</b><p className="text-sm text-muted">{user.email} - {user.roles.map(role => roleText[role]).join(', ')}{user.teacherName ? ` - GV phụ trách: ${user.teacherName}` : ''} - {user.enabled ? 'Đang hoạt động' : 'Tạm khóa'}</p>{user.roles.includes('STUDENT') && <p className="text-sm text-muted">Tuổi: {user.age ?? 'Chưa cập nhật'} - {user.gender ? genderText[user.gender] : 'Chưa cập nhật'} - {user.studyStatus ? statusText[user.studyStatus] : 'Chưa cập nhật'} - Liên hệ khẩn cấp: {user.emergencyContactName || 'Chưa cập nhật'} {user.emergencyContactPhone || ''}</p>}</div>
          <div className="flex gap-2">
            {user.roles.includes('STUDENT') && <Button title="Lịch sử hồ sơ" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setHistoryUser(user)}><History size={16} /></Button>}
            <Button title="Sửa" className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setEditing(user)}><Edit2 size={16} /></Button>
            <Button title="Xóa" className="bg-danger" onClick={() => remove.mutate(user.id)}><Trash2 size={16} /></Button>
          </div>
        </Card>)}
      </div>
      {historyUser && <Card className="mt-4">
        <div className="mb-3 flex items-center justify-between"><h3 className="font-semibold">Lịch sử hồ sơ: {historyUser.fullName}</h3><Button className="bg-slate-700 text-white hover:bg-slate-600" onClick={() => setHistoryUser(null)}><X size={16} /></Button></div>
        <div className="space-y-2">{history.length === 0 && <p className="text-sm text-muted">Chưa có lịch sử thay đổi.</p>}{history.map(item => <div key={item.id} className="rounded-md border border-border p-3 text-sm"><p className="text-muted">{new Date(item.createdAt).toLocaleString()} - {item.actorName ?? 'Hệ thống'}</p><p className="mt-2 break-all"><b>Sau:</b> {item.afterValue}</p></div>)}</div>
      </Card>}
    </div>
  </div>;
}

function StudentFormFields({ form, setForm, teachers }: { form: UserForm; setForm: (f: UserForm) => void; teachers: User[] }) {
  return <>
    <select className="form-field" required value={form.teacherId} onChange={e => setForm({ ...form, teacherId: e.target.value })}><option value="">Chọn giáo viên phụ trách</option>{teachers.map(t => <option key={t.id} value={t.id}>{t.fullName} - {t.email}</option>)}</select>
    <div className="grid gap-3 md:grid-cols-2"><Input type="date" value={form.dateOfBirth} onChange={e => setForm({ ...form, dateOfBirth: e.target.value })} /><select className="form-field" value={form.gender} onChange={e => setForm({ ...form, gender: e.target.value as '' | Gender })}><option value="">Giới tính</option><option value="MALE">Nam</option><option value="FEMALE">Nữ</option><option value="OTHER">Khác</option></select></div>
    <select className="form-field" value={form.studyStatus} onChange={e => setForm({ ...form, studyStatus: e.target.value as StudyStatus })}><option value="ACTIVE">Đang học</option><option value="INACTIVE">Nghỉ học</option></select>
    <Input placeholder="Địa chỉ" value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} />
    <div className="grid gap-3 md:grid-cols-2"><Input placeholder="Tên phụ huynh" value={form.guardianName} onChange={e => setForm({ ...form, guardianName: e.target.value })} /><Input placeholder="Số điện thoại phụ huynh" value={form.guardianPhone} onChange={e => setForm({ ...form, guardianPhone: e.target.value })} /></div>
    <div className="grid gap-3 md:grid-cols-2"><Input placeholder="Người liên hệ khẩn cấp" value={form.emergencyContactName} onChange={e => setForm({ ...form, emergencyContactName: e.target.value })} /><Input placeholder="SĐT khẩn cấp" value={form.emergencyContactPhone} onChange={e => setForm({ ...form, emergencyContactPhone: e.target.value })} /></div>
    <Input placeholder="Quê quán" value={form.hometown} onChange={e => setForm({ ...form, hometown: e.target.value })} />
    <textarea className="form-field min-h-24" placeholder="Dị ứng / lưu ý sức khỏe" value={form.allergies} onChange={e => setForm({ ...form, allergies: e.target.value })} />
  </>;
}

function StudentEditFields({ editing, setEditing, teachers }: { editing: User; setEditing: (u: User) => void; teachers: User[] }) {
  return <>
    <select className="form-field" value={editing.teacherId ?? ''} onChange={e => setEditing({ ...editing, teacherId: e.target.value })}><option value="">Chọn giáo viên phụ trách</option>{teachers.map(t => <option key={t.id} value={t.id}>{t.fullName} - {t.email}</option>)}</select>
    <div className="grid gap-3 md:grid-cols-2"><Input type="date" value={editing.dateOfBirth ?? ''} onChange={e => setEditing({ ...editing, dateOfBirth: e.target.value })} /><select className="form-field" value={editing.gender ?? ''} onChange={e => setEditing({ ...editing, gender: e.target.value as Gender })}><option value="">Giới tính</option><option value="MALE">Nam</option><option value="FEMALE">Nữ</option><option value="OTHER">Khác</option></select></div>
    <select className="form-field" value={editing.studyStatus ?? 'ACTIVE'} onChange={e => setEditing({ ...editing, studyStatus: e.target.value as StudyStatus })}><option value="ACTIVE">Đang học</option><option value="INACTIVE">Nghỉ học</option></select>
    <div className="grid gap-3 md:grid-cols-2"><Input placeholder="Địa chỉ" value={editing.address ?? ''} onChange={e => setEditing({ ...editing, address: e.target.value })} /><Input placeholder="Quê quán" value={editing.hometown ?? ''} onChange={e => setEditing({ ...editing, hometown: e.target.value })} /><Input placeholder="Tên phụ huynh" value={editing.guardianName ?? ''} onChange={e => setEditing({ ...editing, guardianName: e.target.value })} /><Input placeholder="Số điện thoại phụ huynh" value={editing.guardianPhone ?? ''} onChange={e => setEditing({ ...editing, guardianPhone: e.target.value })} /><Input placeholder="Người liên hệ khẩn cấp" value={editing.emergencyContactName ?? ''} onChange={e => setEditing({ ...editing, emergencyContactName: e.target.value })} /><Input placeholder="SĐT khẩn cấp" value={editing.emergencyContactPhone ?? ''} onChange={e => setEditing({ ...editing, emergencyContactPhone: e.target.value })} /></div>
    <textarea className="form-field min-h-24" placeholder="Dị ứng / lưu ý sức khỏe" value={editing.allergies ?? ''} onChange={e => setEditing({ ...editing, allergies: e.target.value })} />
  </>;
}

function MediaPreview({ item }: { item: Media }) {
  const src = mediaUrl(`/media/stream/${item.id}`);
  return item.type === 'VIDEO' ? <video className="aspect-video w-full rounded-md bg-slate-950 object-cover" src={src} preload="metadata" controls playsInline /> : <img className="aspect-video w-full rounded-md bg-slate-950 object-cover" src={src} loading="lazy" alt={item.title} />;
}

export function AdminMediaPage() {
  const { data = [], isLoading } = useQuery({ queryKey: ['admin-media'], queryFn: async () => (await api.get('/media', { params: { size: 100 } })).data.data.content as Media[] });
  const statusLabel: Record<Media['status'], string> = { PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', REJECTED: 'Từ chối' };
  const typeText: Record<Media['type'], string> = { VIDEO: 'Video', IMAGE: 'Hình ảnh' };
  return <div><h2 className="mb-4 text-xl font-semibold">Media giáo viên đã đăng</h2><div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{isLoading && [0, 1, 2].map(i => <div key={i} className="skeleton h-64 rounded-lg" />)}{!isLoading && data.length === 0 && <Card>Chưa có ảnh hoặc video nào được đăng.</Card>}{data.map(item => <Card key={item.id}><MediaPreview item={item} /><div className="mt-3 space-y-1"><div className="flex items-start justify-between gap-3"><b>{item.title}</b><span className="rounded-md border border-border px-2 py-1 text-xs text-muted">{statusLabel[item.status]}</span></div><p className="text-sm text-muted">{typeText[item.type]} - Học sinh: {item.ownerName}</p><p className="text-sm text-muted">Người đăng: {item.uploadedByName}</p>{item.description && <p className="text-sm">{item.description}</p>}</div></Card>)}</div></div>;
}

export function AdminDevelopmentPage() {
  const { data = [], isLoading } = useQuery({ queryKey: ['admin-development'], queryFn: async () => (await api.get('/development-reports', { params: { size: 100 } })).data.data.content as DevelopmentReport[] });
  return <div><h2 className="mb-4 text-xl font-semibold">Lộ trình phát triển học sinh</h2><div className="space-y-3">{isLoading && <div className="skeleton h-28 rounded-lg" />}{!isLoading && data.length === 0 && <Card>Chưa có báo cáo phát triển nào.</Card>}{data.map(r => <Card key={r.id}><div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between"><div><b>{r.studentName}</b><p className="text-sm text-muted">Giáo viên: {r.teacherName}</p></div><span className="rounded-md border border-border px-2 py-1 text-xs text-muted">Tuần {r.weekStart}</span></div><div className="mt-3 grid gap-2 md:grid-cols-2"><p><b>Thể chất:</b> {r.physicalChange || 'Không ghi nhận'}</p><p><b>Nhận thức:</b> {r.cognitiveChange || 'Không ghi nhận'}</p><p><b>Xã hội:</b> {r.socialChange || 'Không ghi nhận'}</p><p><b>Cảm xúc:</b> {r.emotionalChange || 'Không ghi nhận'}</p></div><p className="mt-3"><b>Ghi chú:</b> {r.note}</p></Card>)}</div></div>;
}

export function AdminSettingsPage() {
  const qc = useQueryClient();
  const [file, setFile] = useState<File>();
  const upload = useMutation({ mutationFn: async () => { if (!file) return; const fd = new FormData(); fd.append('file', file); await api.post('/branding/logo', fd); }, onSuccess: () => { setFile(undefined); qc.invalidateQueries({ queryKey: ['branding'] }); alert('Đã cập nhật logo.'); } });
  return <Card className="max-w-2xl"><h2 className="text-xl font-semibold">Cài đặt thương hiệu</h2><div className="mt-4 flex items-center gap-4"><BrandLogo className="h-16 w-16" iconSize={30} /><div><b><BrandName /></b><p className="text-sm text-muted">Logo này hiển thị ở màn đăng nhập, sidebar và splash screen.</p></div></div><div className="mt-5 grid gap-3"><Input type="file" accept="image/*" onChange={e => setFile(e.target.files?.[0])} /><Button disabled={!file || upload.isPending} onClick={() => upload.mutate()}>Cập nhật logo</Button></div></Card>;
}

export function ApprovalsPage() {
  const { data = [], refetch } = useQuery({ queryKey: ['pending'], queryFn: async () => (await api.get('/admin/pending-approvals')).data.data.content });
  const approve = async (id: string, status: string) => { await api.post(`/admin/pending-approvals/${id}`, { status }); refetch(); };
  return <div><h2 className="mb-4 text-xl font-semibold">Tư liệu chờ duyệt</h2><div className="space-y-2">{data.length === 0 && <Card>Không có tư liệu nào đang chờ duyệt.</Card>}{data.map((m: any) => <Card key={m.id} className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><b>{m.title}</b><p className="text-sm text-muted">{m.type === 'VIDEO' ? 'Video' : 'Hình ảnh'} - {m.status === 'PENDING' ? 'Chờ duyệt' : m.status}</p></div><div className="flex gap-2"><Button onClick={() => approve(m.id,'APPROVED')}>Duyệt</Button><Button className="bg-danger text-white hover:bg-rose-400" onClick={() => approve(m.id,'REJECTED')}>Từ chối</Button></div></Card>)}</div></div>;
}

export function ReportsPage() { return <Card><h2 className="text-xl font-semibold">Báo cáo</h2><p className="mt-2 text-muted">Nhật ký hoạt động và báo cáo hệ thống đã sẵn sàng để tích hợp công cụ phân tích.</p></Card>; }
