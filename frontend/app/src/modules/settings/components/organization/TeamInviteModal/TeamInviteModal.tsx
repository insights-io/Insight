import React, { useState } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter } from 'baseui/modal';
import { useForm, Controller } from 'react-hook-form';
import { FormControl } from 'baseui/form-control';
import { useStyletron } from 'baseui';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import { toaster } from 'baseui/toast';
import FormError from 'shared/components/FormError';
import { RadioGroup, Radio } from 'baseui/radio';
import { REQUIRED_VALIDATION } from 'modules/auth/validation/base';
import type {
  APIError,
  TeamInviteCreateDTO,
  APIErrorDataResponse,
  UserRole,
  TeamInviteDTO,
} from '@rebrowse/types';
import { Input, Button } from '@rebrowse/elements';
import { applyApiFormErrors } from 'shared/utils/form';
import { SIZE } from 'baseui/button';

type Props = {
  createTeamInvite: (formData: TeamInviteCreateDTO) => Promise<TeamInviteDTO>;
  children: (open: () => void) => void;
};

const ADMIN: UserRole = 'admin';
const MEMBER: UserRole = 'member';

const TeamInviteModal = ({ createTeamInvite, children }: Props) => {
  const [_css, theme] = useStyletron();
  const [isOpen, setIsOpen] = useState(false);
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
    setIsOpen(false);
    reset({ email: undefined, role: undefined });
  };

  const open = () => {
    setIsOpen(true);
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
            <FormControl label="Email" error={errors.email?.message}>
              <Input
                name="email"
                type="email"
                placeholder="Email"
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
