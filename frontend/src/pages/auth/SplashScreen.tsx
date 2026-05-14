import { BrandLogo, BrandName } from '../../components/BrandLogo';

export function SplashScreen() {
  return <div className="grid min-h-screen place-items-center bg-background text-foreground"><div className="text-center"><BrandLogo className="mx-auto mb-4 h-14 w-14" iconSize={28} /><h1 className="text-3xl font-bold"><BrandName /></h1></div></div>;
}
