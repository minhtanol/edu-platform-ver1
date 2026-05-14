import { forwardRef, InputHTMLAttributes } from 'react';
import { cn } from '../../lib/utils';

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(({ className, ...props }, ref) => {
  return <input ref={ref} {...props} className={cn('touch-target w-full rounded-md border border-border bg-slate-950/60 px-3 py-2 text-sm text-foreground shadow-sm outline-none transition placeholder:text-slate-500 focus:border-primary focus:ring-2 focus:ring-primary/20', className)} />;
});
Input.displayName = 'Input';
