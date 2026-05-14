import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { BarChart3, Bot, GalleryHorizontal, GraduationCap, Home, Images, LineChart, LogOut, Menu, Upload, Users, Video, X } from 'lucide-react';
import { useState } from 'react';
import { useAuthStore, Role } from '../../stores/auth';
import { cn } from '../../lib/utils';

const nav = {
  ADMIN: [['/admin','Tổng quan',BarChart3], ['/admin/users','Tài khoản',Users], ['/admin/media','Media',Images], ['/admin/development','Lộ trình',LineChart], ['/admin/approvals','Duyệt tư liệu',Upload], ['/admin/reports','Báo cáo',BarChart3]],
  TEACHER: [['/teacher','Tổng quan',Home], ['/teacher/upload','Tải tư liệu',Upload], ['/teacher/evaluations','Đánh giá',BarChart3], ['/teacher/development','Theo dõi',LineChart], ['/teacher/students','Học sinh',Users]],
  STUDENT: [['/student','Trang chủ',Home], ['/student/videos','Video',Video], ['/student/gallery','Thư viện',GalleryHorizontal], ['/student/evaluations','Nhận xét',BarChart3], ['/student/chat','Trợ lý AI',Bot]]
} as const;

const roleLabel: Record<Role, string> = {
  ADMIN: 'Quản trị hệ thống',
  TEACHER: 'Không gian giáo viên',
  STUDENT: 'Không gian học sinh'
};

export function DashboardLayout() {
  const [open, setOpen] = useState(false);
  const { roles, fullName, logout } = useAuthStore();
  const navigate = useNavigate();
  const role = (roles?.[0] ?? 'STUDENT') as Role;
  const links = nav[role] ?? nav.STUDENT;
  const signOut = () => { logout(); navigate('/login'); };
  return <div className="min-h-screen text-foreground lg:grid lg:grid-cols-[272px_1fr]">
    <aside className={cn('fixed inset-y-0 left-0 z-40 w-72 border-r border-cyan-400/10 bg-slate-950/86 p-4 text-foreground shadow-2xl backdrop-blur-2xl transition lg:static lg:w-auto lg:shadow-none', open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0')}>
      <div className="mb-7 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="grid h-10 w-10 place-items-center rounded-lg bg-primary text-slate-950 shadow-lg shadow-cyan-950/30"><GraduationCap size={22} /></span>
          <div><strong className="block leading-tight">Nền tảng học tập</strong><span className="text-xs text-muted">{roleLabel[role]}</span></div>
        </div>
        <button className="touch-target rounded-md p-2 text-muted hover:bg-white/10 lg:hidden" onClick={() => setOpen(false)}><X size={18} /></button>
      </div>
      <nav className="space-y-1">{links.map(([to,label,Icon]) => <NavLink key={to} to={to} end className={({isActive}) => cn('flex touch-target items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition', isActive ? 'bg-primary text-slate-950 shadow-[0_14px_34px_rgba(34,211,238,0.22)]' : 'text-muted hover:bg-white/10 hover:text-foreground')}><Icon size={18}/>{label}</NavLink>)}</nav>
    </aside>
    <main className="pb-20 lg:pb-0">
      <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border/70 bg-background/70 px-4 shadow-sm backdrop-blur-xl">
        <button className="touch-target rounded-md p-2 text-muted hover:bg-white/10 lg:hidden" onClick={() => setOpen(true)}><Menu /></button>
        <div><p className="text-xs font-medium uppercase tracking-wide text-muted">Đang đăng nhập</p><h1 className="font-semibold text-foreground">{fullName}</h1></div>
        <button className="touch-target rounded-md p-2 text-muted transition hover:bg-white/10 hover:text-foreground" onClick={signOut}><LogOut size={20}/></button>
      </header>
      <section className="mx-auto max-w-7xl p-4 sm:p-6"><Outlet /></section>
    </main>
    <nav className="fixed inset-x-0 bottom-0 z-40 grid grid-cols-5 border-t border-border/80 bg-slate-950/90 px-2 py-1 shadow-[0_-10px_30px_rgba(0,0,0,0.26)] backdrop-blur-xl lg:hidden">
      {links.slice(0,5).map(([to,label,Icon]) => <NavLink key={to} to={to} className={({isActive}) => cn('flex flex-col items-center justify-center gap-1 rounded-md py-1 text-xs transition', isActive ? 'bg-primary text-slate-950' : 'text-muted')}><Icon size={19}/><span>{label}</span></NavLink>)}
    </nav>
  </div>;
}
