import { useState } from 'react';
import { api } from '../../services/api';
import { Button } from '../../components/ui/button';
import { Input } from '../../components/ui/input';
import { Card } from '../../components/ui/card';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState(''); const [message, setMessage] = useState('');
  const submit = async () => { await api.post('/auth/forgot-password', { email }); setMessage('Mã OTP demo đã được tạo. Kiểm tra backend logs hoặc Redis key otp:' + email); };
  return <main className="grid min-h-screen place-items-center p-4"><Card className="w-full max-w-md border-cyan-300/20 p-6"><h1 className="text-xl font-semibold text-foreground">Đặt lại mật khẩu</h1><p className="mt-1 text-sm text-muted">Nhập email đã đăng ký tài khoản.</p><Input className="mt-4" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} /><Button className="mt-4 w-full" onClick={submit}>Gửi mã OTP</Button>{message && <p className="mt-3 rounded-md border border-border bg-slate-950/50 p-3 text-sm text-muted">{message}</p>}</Card></main>;
}
