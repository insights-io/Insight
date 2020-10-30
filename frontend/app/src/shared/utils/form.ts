import type { APIErrorDataResponse } from '@insight/types';
import type {
  ErrorOption,
  FieldName,
  FieldValues,
  UseFormMethods,
} from 'react-hook-form';

export const applyApiFormErrors = <Values extends FieldValues>(
  setError: UseFormMethods<Values>['setError'],
  errors: APIErrorDataResponse['error']['errors'] = {}
) => {
  return Object.keys(errors).reduce((acc, field) => {
    const errorOption = { message: errors[field] };
    setError(field as FieldName<Values>, errorOption);
    return { ...acc, [field]: errorOption };
  }, {} as Record<string, ErrorOption>);
};
