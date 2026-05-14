import { useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { Bot, Save, Upload } from 'lucide-react';
import { api } from '../../services/api';
import { Card } from '../../components/ui/card';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';

type Student = { id: string; email: string; fullName: string };

function useStudents() {
  return useQuery({ queryKey: ['students'], queryFn: async () => (await api.get('/users/students', { params: { size: 100 } })).data.data.content as Student[] });
}

export function TeacherDashboard() {
  const { data = [] } = useStudents();
  return <div className="grid gap-4 md:grid-cols-3"><Card><b>Học sinh</b><p className="mt-2 text-3xl font-bold text-primary">{data.length}</p></Card><Card><b>Quy trình tải tư liệu</b><p className="mt-2 text-3xl font-bold text-accent">Sẵn sàng</p></Card><Card><b>Gợi ý AI</b><p className="mt-2 text-3xl font-bold text-primary">Sẵn sàng</p></Card></div>;
}

export function UploadMediaPage() {
  const qc = useQueryClient();
  const { data: students = [] } = useStudents();
  const [ownerId, setOwnerId] = useState('');
  const [file, setFile] = useState<File>();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const selectedOwnerId = ownerId || students[0]?.id || '';
  const upload = async () => {
    if (!file || !selectedOwnerId) return;
    const fd = new FormData();
    fd.append('file', file); fd.append('ownerId', selectedOwnerId); fd.append('title', title || file.name); fd.append('description', description);
    fd.append('type', file.type.startsWith('video') ? 'VIDEO' : 'IMAGE');
    await api.post('/media/upload', fd);
    setFile(undefined); setTitle(''); setDescription(''); qc.invalidateQueries({ queryKey: ['media'] });
    alert('Đã tải lên, tư liệu đang chờ quản trị duyệt.');
  };
  return <Card><h2 className="text-xl font-semibold">Tải tư liệu học tập</h2><div className="mt-4 grid gap-3">
    <select className="form-field" value={selectedOwnerId} onChange={e => setOwnerId(e.target.value)}>
      {students.map(s => <option key={s.id} value={s.id}>{s.fullName} - {s.email}</option>)}
    </select>
    <Input placeholder="Tiêu đề" value={title} onChange={e=>setTitle(e.target.value)} />
    <textarea className="form-field min-h-28" placeholder="Mô tả ngắn" value={description} onChange={e=>setDescription(e.target.value)} />
    <Input type="file" accept="image/*,video/*" onChange={e=>setFile(e.target.files?.[0])} />
    <Button onClick={upload} disabled={!file || !selectedOwnerId}><Upload size={16} />Tải lên</Button>
  </div></Card>;
}

export function EvaluationsPage() {
  const { data: students = [] } = useStudents();
  const [studentId, setStudentId] = useState('');
  const [subject, setSubject] = useState('Tiến bộ học tập');
  const [score, setScore] = useState(85);
  const [content, setContent] = useState('');
  const [ai, setAi] = useState('');
  const selectedStudentId = studentId || students[0]?.id || '';
  const selectedStudent = useMemo(() => students.find(s => s.id === selectedStudentId), [students, selectedStudentId]);
  const save = async () => { if (!selectedStudentId || !content.trim()) return; await api.post('/evaluations', { studentId: selectedStudentId, subject, content, score }); alert('Đã lưu đánh giá.'); };
  const suggest = async () => { const res = await api.post('/ai/suggest-evaluation', { studentName: selectedStudent?.fullName ?? 'Học sinh', evidence: content || 'Tham gia học tập tích cực' }); setAi(res.data.data.message); };
  return <div className="grid gap-4 lg:grid-cols-2"><Card><h2 className="text-xl font-semibold">Đánh giá học sinh</h2><div className="mt-4 grid gap-3">
    <select className="form-field" value={selectedStudentId} onChange={e=>setStudentId(e.target.value)}>{students.map(s => <option key={s.id} value={s.id}>{s.fullName}</option>)}</select>
    <Input placeholder="Chủ đề đánh giá" value={subject} onChange={e=>setSubject(e.target.value)} />
    <Input type="number" min={0} max={100} value={score} onChange={e=>setScore(Number(e.target.value))} />
    <textarea className="form-field min-h-40" value={content} onChange={e=>setContent(e.target.value)} />
    <div className="flex gap-2"><Button onClick={save}><Save size={16} />Lưu</Button><Button className="bg-accent text-slate-950 hover:bg-lime-300" onClick={suggest}><Bot size={16} />Gợi ý AI</Button></div>
  </div></Card><Card><h3 className="font-semibold">Gợi ý từ AI</h3><p className="mt-3 whitespace-pre-wrap text-muted">{ai || 'Nội dung gợi ý sẽ hiển thị tại đây.'}</p></Card></div>;
}

export function StudentsListPage() {
  const { data = [], isLoading } = useStudents();
  return <div><h2 className="mb-4 text-xl font-semibold">Danh sách học sinh</h2><div className="space-y-2">{isLoading && <div className="skeleton h-24 rounded-lg" />}{data.map(s => <Link key={s.id} to={`/teacher/students/${s.id}`}><Card className="transition hover:border-primary"><b>{s.fullName}</b><p className="text-sm text-muted">{s.email}</p></Card></Link>)}</div></div>;
}

export function StudentDetailPage() {
  const { id } = useParams();
  const { data: students = [] } = useStudents();
  const student = students.find(s => s.id === id);
  const { data = [] } = useQuery({ queryKey: ['student-evals', id], enabled: Boolean(id), queryFn: async () => (await api.get(`/evaluations/student/${id}`)).data.data.content });
  return <div><h2 className="mb-4 text-xl font-semibold">{student?.fullName ?? 'Chi tiết học sinh'}</h2><div className="space-y-2">{data.length === 0 && <Card>Chưa có đánh giá nào.</Card>}{data.map((e: any) => <Card key={e.id}><b>{e.subject}</b><p>{e.content}</p><span className="text-sm text-muted">Điểm {e.score}</span></Card>)}</div></div>;
}
