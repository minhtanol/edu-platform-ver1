import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../../services/api';
import { useAuthStore } from '../../stores/auth';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Card } from '../../components/ui/card';
import { BrandLogo, BrandName } from '../../components/BrandLogo';

const schema = z.object({ email: z.string().email(), password: z.string().min(8) });
type Form = z.infer<typeof schema>;

export function LoginPage() {
  const nav = useNavigate(); const setSession = useAuthStore(s => s.setSession);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<Form>({ resolver: zodResolver(schema), defaultValues: { email: 'admin@education.com', password: 'Admin@123' } });
  const onSubmit = async (data: Form) => { const res = await api.post('/auth/login', data); setSession(res.data.data); const role = res.data.data.roles[0]?.toLowerCase() ?? 'student'; nav(`/${role}`); };
  return <main className="grid min-h-screen place-items-center p-4">
    <Card className="w-full max-w-md border-cyan-300/20 p-6">
      <div className="mb-6">
        <BrandLogo className="mb-4 h-12 w-12" />
        <h1 className="text-2xl font-bold text-foreground"><BrandName /></h1>
        <p className="mt-1 text-sm text-muted">Quản lý lớp học, đánh giá và tư liệu học tập trong một không gian bảo mật.</p>
      </div>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div><Input placeholder="Email" {...register('email')} />{errors.email && <p className="mt-1 text-sm text-danger">Email không hợp lệ</p>}</div>
        <div><Input placeholder="Mật khẩu" type="password" {...register('password')} />{errors.password && <p className="mt-1 text-sm text-danger">Mật khẩu cần tối thiểu 8 ký tự</p>}</div>
        <Button disabled={isSubmitting} className="w-full">Đăng nhập</Button>
      </form>
      <Link className="mt-4 block text-sm font-medium text-primary hover:text-cyan-200" to="/forgot-password">Quên mật khẩu?</Link>
    </Card>
  </main>;
}
