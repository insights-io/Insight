import type { HttpResponse, RequestOptions } from '@rebrowse/sdk';
import type { CodeValidityDTO } from '@rebrowse/types';

export type MfaInputProps = {
  code: string[];
  handleChange: (code: string[]) => void;
  error: React.ReactNode | undefined;
  sendCode: (
    options?: RequestOptions
  ) => Promise<HttpResponse<CodeValidityDTO>>;
};
