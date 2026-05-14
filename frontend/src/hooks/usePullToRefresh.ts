import { useEffect } from 'react';
export function usePullToRefresh(onRefresh: () => void) {
  useEffect(() => {
    let start = 0;
    const down = (e: TouchEvent) => { if (scrollY === 0) start = e.touches[0].clientY; };
    const up = (e: TouchEvent) => { if (start && e.changedTouches[0].clientY - start > 80) onRefresh(); start = 0; };
    addEventListener('touchstart', down); addEventListener('touchend', up);
    return () => { removeEventListener('touchstart', down); removeEventListener('touchend', up); };
  }, [onRefresh]);
}
