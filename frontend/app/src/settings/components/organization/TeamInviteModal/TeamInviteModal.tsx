import React, { useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter } from 'baseui/modal';
import { useForm, Controller } from 'react-hook-form';
import { FormControl } from 'baseui/form-control';
import { useStyletron } from 'baseui';
import { EMAIL_PLACEHOLDER } from 'shared/constants/form-placeholders';
import {
  EMAIL_VALIDATION,
  REQUIRED_VALIDATION,
} from 'shared/constants/form-validation';
import { toaster } from 'baseui/toast';
import { FormError } from 'shared/components/FormError';
import { RadioGroup, Radio } from 'baseui/radio';
import type {
  APIError,
  TeamInviteCreateDTO,
  APIErrorDataResponse,
  UserRole,
  TeamInviteDTO,
  DataResponse,
} from '@rebrowse/types';
import { Button, EmailInput, Label } from '@rebrowse/elements';
import { applyApiFormErrors } from 'shared/utils/form';
import { SIZE } from 'baseui/button';
import { useIsOpen } from 'shared/hooks/useIsOpen';
import type { HttpResponse } from '@rebrowse/sdk';

type Props = {
  createTeamInvite: (
    formData: TeamInviteCreateDTO
  ) => Promise<HttpResponse<DataResponse<TeamInviteDTO>>>;
  children: (open: () => void) => void;
};

const ADMIN: UserRole = 'admin';
const MEMBER: UserRole = 'member';

const TeamInviteModal = ({ createTeamInvite, children }: Props) => {
  const [_css, theme] = useStyletron();
  const { isOpen, open, close: closeModal } = useIsOpen();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const {
    register,
    handleSubmit,
    errors,
    control,
    setError,
    reset,
  } = useForm<TeamInviteCreateDTO>();

  const close = () => {
    closeModal();
    reset({ email: undefined, role: undefined });
  };

  const onSubmit = handleSubmit((data) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    createTeamInvite(data)
      .then(() => {
        toaster.positive('Member invited', {});
        setFormError(undefined);
        close();
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
        applyApiFormErrors(
          setError,
          errorDTO.error.errors as Record<string, string>
        );
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <>
      {children(open)}
      <Modal onClose={close} isOpen={isOpen}>
        <form onSubmit={onSubmit} noValidate>
          <ModalHeader>Invite new member</ModalHeader>
          <ModalBody>
            <FormControl
              label={<Label as="span">Email</Label>}
              error={errors.email?.message}
            >
              <EmailInput
                placeholder={EMAIL_PLACEHOLDER}
                required
                inputRef={register(EMAIL_VALIDATION)}
                error={Boolean(errors.email)}
              />
            </FormControl>

            <FormControl label="Role" error={errors.role?.message}>
              <Controller
                name="role"
                control={control}
                rules={REQUIRED_VALIDATION}
                as={
                  <RadioGroup>
                    <Radio value={ADMIN}>Admin</Radio>
                    <Radio value={MEMBER}>Member</Radio>
                  </RadioGroup>
                }
              />
            </FormControl>
          </ModalBody>
          <ModalFooter>
            <Button kind="tertiary" onClick={close} size={SIZE.compact}>
              Cancel
            </Button>
            <Button
              size={SIZE.compact}
              type="submit"
              isLoading={isSubmitting}
              $style={{ marginLeft: theme.sizing.scale400 }}
            >
              Invite
            </Button>
            {formError && <FormError error={formError} />}
          </ModalFooter>
        </form>
      </Modal>
    </>
  );
};

export default React.memo(TeamInviteModal);
