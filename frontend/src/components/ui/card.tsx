import { HTMLAttributes } from 'react';
import { cn } from '../../lib/utils';
export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) { return <div className={cn('rounded-lg border border-border/80 bg-panel/78 p-4 text-foreground shadow-soft backdrop-blur-xl', className)} {...props} />; }
