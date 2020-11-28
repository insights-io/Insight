export type MfaInputMethodProps = {
  code: string[];
  handleChange: (code: string[]) => void;
  error: React.ReactNode | undefined;
};
