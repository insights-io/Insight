import React, { useState } from 'react';
import { Button, Flex, Input, VerticalAligned } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import { Checkbox } from 'baseui/checkbox';
import { SIZE } from 'baseui/input';
import { Controller, useForm } from 'react-hook-form';
import type {
  APIError,
  APIErrorDataResponse,
  OrganizationPasswordPolicyDTO,
  PasswordPolicy,
} from '@rebrowse/types';
import { useOrganizationPasswordPolicy } from 'shared/hooks/useOrganizationPasswordPolicy';
import { REQUIRED_VALIDATION } from 'shared/constants/form-validation';
import Divider from 'shared/components/Divider';
import { toaster } from 'baseui/toast';
import { FormControl } from 'baseui/form-control';
import { FormError } from 'shared/components/FormError';

type Props = {
  initialPasswordPolicy: OrganizationPasswordPolicyDTO | undefined;
};

export const PasswordPolicyForm = ({ initialPasswordPolicy }: Props) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<APIError>();
  const {
    passwordPolicy,
    updatePasswordPolicy,
    createPasswordPolicy,
  } = useOrganizationPasswordPolicy(initialPasswordPolicy);

  const {
    register,
    errors,
    handleSubmit,
    control,
    setError,
  } = useForm<PasswordPolicy>({
    reValidateMode: 'onChange',
    defaultValues: {
      minCharacters: passwordPolicy?.minCharacters || 8,
      preventPasswordReuse: passwordPolicy?.preventPasswordReuse || true,
      requireLowercaseCharacter:
        passwordPolicy?.requireLowercaseCharacter || false,
      requireUppercaseCharacter:
        passwordPolicy?.requireUppercaseCharacter || false,
      requireNumber: passwordPolicy?.requireNumber || false,
      requireNonAlphanumericCharacter:
        passwordPolicy?.requireNonAlphanumericCharacter || false,
    },
  });

  const onSubmit = handleSubmit((values) => {
    const apply = passwordPolicy ? updatePasswordPolicy : createPasswordPolicy;

    setIsSubmitting(true);
    apply({ ...values, minCharacters: Number(values.minCharacters) })
      .then(() => toaster.positive('Password policy configured', {}))
      .catch(async (error) => {
        const errorResponse: APIErrorDataResponse = await error.response.json();
        const {
          error: { errors: apiErrors },
        } = errorResponse;

        if (apiErrors) {
          Object.keys(apiErrors).forEach((field) => {
            setError(field as keyof PasswordPolicy, {
              message: apiErrors[field] as string,
            });
          });
        } else {
          setApiError(errorResponse.error);
        }
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <form onSubmit={onSubmit}>
      <Checkbox
        disabled
        checked
        overrides={{ Label: { style: { fontSize: '12px' } } }}
      >
        Enforce minimum length
      </Checkbox>
      <Flex marginTop="8px" marginLeft="32px">
        <FormControl error={errors.minCharacters?.message}>
          <Input
            type="number"
            size={SIZE.mini}
            name="minCharacters"
            min={8}
            max={256}
            ref={register(REQUIRED_VALIDATION)}
            error={Boolean(errors.minCharacters?.message)}
            placeholder="Min characters"
          />
        </FormControl>
        <VerticalAligned marginLeft="8px">characters</VerticalAligned>
      </Flex>

      <Block marginTop="8px">
        <Controller
          name="preventPasswordReuse"
          control={control}
          render={(props) => (
            <Checkbox
              name="preventPasswordReuse"
              checked={props.value}
              onChange={(e) => props.onChange(e.currentTarget.checked)}
              overrides={{ Label: { style: { fontSize: '12px' } } }}
            >
              Prevent password reuse
            </Checkbox>
          )}
        />
      </Block>

      <Block marginTop="8px">
        <Controller
          name="requireUppercaseCharacter"
          control={control}
          render={(props) => (
            <Checkbox
              overrides={{ Label: { style: { fontSize: '12px' } } }}
              name="requireUppercaseCharacter"
              checked={props.value}
              onChange={(e) => props.onChange(e.currentTarget.checked)}
            >
              Require at least one uppercase letter from Latin alphabet (A-Z)
            </Checkbox>
          )}
        />
      </Block>

      <Block marginTop="8px">
        <Controller
          name="requireLowercaseCharacter"
          control={control}
          render={(props) => (
            <Checkbox
              overrides={{ Label: { style: { fontSize: '12px' } } }}
              name="requireLowercaseCharacter"
              checked={props.value}
              onChange={(e) => props.onChange(e.currentTarget.checked)}
            >
              Require at least one lowercase letter from Latin alphabet (a-z)
            </Checkbox>
          )}
        />
      </Block>

      <Block marginTop="8px">
        <Controller
          name="requireNumber"
          control={control}
          render={(props) => (
            <Checkbox
              overrides={{ Label: { style: { fontSize: '12px' } } }}
              name="requireNumber"
              checked={props.value}
              onChange={(e) => props.onChange(e.currentTarget.checked)}
            >
              Require at least one number
            </Checkbox>
          )}
        />
      </Block>

      <Block marginTop="8px">
        <Controller
          name="requireNonAlphanumericCharacter"
          control={control}
          render={(props) => (
            <Checkbox
              overrides={{ Label: { style: { fontSize: '12px' } } }}
              name="requireNonAlphanumericCharacter"
              checked={props.value}
              onChange={(e) => props.onChange(e.currentTarget.checked)}
            >
              {`Require at least one non-alphanumeric character (! @ # $ % ^ & * () _ + - = [ ] {} | ')`}
            </Checkbox>
          )}
        />
      </Block>

      <Divider />
      {apiError && <FormError error={apiError} />}
      <Button type="submit" isLoading={isSubmitting} size={SIZE.compact}>
        Save
      </Button>
    </form>
  );
};
