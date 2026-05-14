import { ButtonHTMLAttributes } from 'react';
import { cn } from '../../lib/utils';

export function Button({ className, ...props }: ButtonHTMLAttributes<HTMLButtonElement>) {
  return <button className={cn('touch-target inline-flex items-center justify-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-semibold text-slate-950 shadow-[0_12px_30px_rgba(34,211,238,0.18)] ring-1 ring-cyan-200/20 transition hover:bg-cyan-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/40 disabled:cursor-not-allowed disabled:opacity-50', className)} {...props} />;
}
