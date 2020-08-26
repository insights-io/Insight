export type TfaInputMethodProps = {
  code: string[];
  handleChange: (code: string[]) => void;
  error: React.ReactNode | undefined;
};
