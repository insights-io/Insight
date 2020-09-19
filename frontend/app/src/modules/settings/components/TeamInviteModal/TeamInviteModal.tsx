import React, { useState } from 'react';
import { Button, SIZE } from 'baseui/button';
import {
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  ModalButton,
} from 'baseui/modal';
import {
  APIError,
  TeamInviteCreateDTO,
  APIErrorDataResponse,
  TeamInvite,
  UserRole,
} from '@insight/types';
import { useForm, Controller } from 'react-hook-form';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { useStyletron } from 'baseui';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import { createInputOverrides } from 'shared/styles/input';
import { toaster } from 'baseui/toast';
import FormError from 'shared/components/FormError';
import { RadioGroup, Radio } from 'baseui/radio';
import { REQUIRED_VALIDATION } from 'modules/auth/validation/base';

type Props = {
  createInvite: (formData: TeamInviteCreateDTO) => Promise<TeamInvite>;
};

const ADMIN: UserRole = 'admin';
const STANDARD: UserRole = 'standard';

const TeamInviteModal = ({ createInvite }: Props) => {
  const [_css, theme] = useStyletron();
  const [isOpen, setIsOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const { register, handleSubmit, errors, control } = useForm<
    TeamInviteCreateDTO
  >();
  const inputOverrides = createInputOverrides(theme);

  const close = () => {
    setIsOpen(false);
  };

  const open = () => {
    setIsOpen(true);
  };

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    createInvite(formData)
      .then((_resp) => {
        toaster.positive('Member invited', {});
        setFormError(undefined);
        close();
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <>
      <Button onClick={open} size={SIZE.mini}>
        Invite new member
      </Button>
      <Modal onClose={close} isOpen={isOpen}>
        <form onSubmit={onSubmit} noValidate>
          <ModalHeader>Invite new member</ModalHeader>
          <ModalBody>
            <FormControl label="Email" error={errors.email?.message}>
              <Input
                overrides={inputOverrides}
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
                    <Radio value={STANDARD}>Regular</Radio>
                  </RadioGroup>
                }
              />
            </FormControl>
          </ModalBody>
          <ModalFooter>
            <ModalButton kind="tertiary" onClick={close}>
              Cancel
            </ModalButton>
            <ModalButton
              type="submit"
              isLoading={isSubmitting}
              $style={{ width: '100%' }}
            >
              Invite
            </ModalButton>
            {formError && <FormError error={formError} />}
          </ModalFooter>
        </form>
      </Modal>
    </>
  );
};

export default React.memo(TeamInviteModal);
