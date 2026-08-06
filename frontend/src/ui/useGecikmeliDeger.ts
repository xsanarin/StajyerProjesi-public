import { useEffect, useState } from "react";

/**
 * Degeri gecikmeli yansitir.
 *
 * Arama artik sunucuda: her tus vurusunda istek atmak hem gereksiz hem de
 * yanitlar sirasiz donebilir. Kullanici yazmayi birakana kadar bekliyoruz.
 */
export function useGecikmeliDeger<T>(deger: T, gecikmeMs = 350): T {
  const [gecikmeli, setGecikmeli] = useState(deger);

  useEffect(() => {
    const zamanlayici = setTimeout(() => setGecikmeli(deger), gecikmeMs);
    return () => clearTimeout(zamanlayici);
  }, [deger, gecikmeMs]);

  return gecikmeli;
}