import type {
  ErrorOption,
  FieldName,
  FieldValues,
  UseFormMethods,
} from 'react-hook-form';

export const setFormErrors = <Values extends FieldValues>(
  setError: UseFormMethods<Values>['setError'],
  errors: Record<string, string> = {}
) => {
  return Object.keys(errors).reduce((acc, field) => {
    const errorOption: ErrorOption = { message: errors[field] };
    setError(field as FieldName<Values>, errorOption);
    return { ...acc, [field]: errorOption };
  }, {} as Record<string, ErrorOption>);
};
