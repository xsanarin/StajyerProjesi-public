import { Button, CircularProgress, type ButtonProps } from "@mui/material";

/**
 * Mutasyon tetikleyen butonlar icin: islem surerken kilitlenir ve doner.
 * Butonu sadece disable etmek "tikladim mi?" hissi biraktigi icin spinner sart.
 */
export function IslemButonu({
  calisiyor = false,
  disabled,
  startIcon,
  children,
  ...rest
}: ButtonProps & { calisiyor?: boolean }) {
  return (
    <Button
      {...rest}
      disabled={disabled || calisiyor}
      startIcon={calisiyor ? <CircularProgress size={16} color="inherit" /> : startIcon}
    >
      {children}
    </Button>
  );
}
