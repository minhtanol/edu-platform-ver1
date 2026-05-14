import { GraduationCap } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../services/api';
import { cn } from '../lib/utils';

type Branding = { appName: string; logoUrl?: string | null };

export function useBranding() {
  return useQuery({
    queryKey: ['branding'],
    queryFn: async () => (await api.get('/branding')).data.data as Branding,
    staleTime: 60_000
  });
}

export function BrandLogo({ className, iconSize = 22 }: { className?: string; iconSize?: number }) {
  const { data } = useBranding();
  if (data?.logoUrl) {
    return <span className={cn('grid place-items-center overflow-hidden rounded-lg bg-white shadow-lg shadow-cyan-950/30', className)}><img className="h-full w-full object-contain" src={data.logoUrl} alt={data.appName} /></span>;
  }
  return <span className={cn('grid place-items-center rounded-lg bg-primary text-slate-950 shadow-lg shadow-cyan-950/30', className)}><GraduationCap size={iconSize} /></span>;
}

export function BrandName() {
  const { data } = useBranding();
  return <>{data?.appName ?? 'Nền tảng học tập'}</>;
}
