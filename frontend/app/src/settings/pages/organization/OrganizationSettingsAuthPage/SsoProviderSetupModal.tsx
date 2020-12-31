import React, { useState } from 'react';
import { Modal, ModalHeader, ModalFooter, ModalBody } from 'baseui/modal';
import { Button, Input } from '@rebrowse/elements';
import type {
  APIError,
  APIErrorDataResponse,
  SamlConfigurationDTO,
  SamlMethod,
  SsoMethod,
  SsoSetupDTO,
} from '@rebrowse/types';
import { FormError } from 'shared/components/FormError';
import { SIZE } from 'baseui/button';
import { toaster } from 'baseui/toast';
import { useForm } from 'react-hook-form';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { REQUIRED_VALIDATION } from 'shared/constants/form-validation';
import { applyApiFormErrors } from 'shared/utils/form';

type Props = {
  method: SsoMethod;
  samlMethod?: SamlMethod;
  label: string;
  isOpen: boolean;
  onClose: () => void;
  createSsoSetup: (params: {
    method: SsoMethod;
    saml?: SamlConfigurationDTO;
  }) => Promise<SsoSetupDTO>;
};

type FormValues = {
  metadataEndpoint: string;
};

export const SsoProviderSetupModal = ({
  label,
  method,
  samlMethod,
  isOpen,
  onClose,
  createSsoSetup,
}: Props) => {
  const [formError, setFormError] = useState<APIError | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { handleSubmit, register, errors, setError } = useForm<FormValues>({
    defaultValues: { metadataEndpoint: '' },
  });

  const onSubmit = handleSubmit(({ metadataEndpoint }) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);
    setFormError(undefined);

    createSsoSetup({
      method,
      saml: samlMethod ? { metadataEndpoint, method: samlMethod } : undefined,
    })
      .then(() => {
        toaster.positive(`${label} SSO setup enabled`, {});
        onClose();
      })
      .catch(async (setupError) => {
        const errorDTO: APIErrorDataResponse = await setupError.response.json();
        setFormError(errorDTO.error);
        applyApiFormErrors(
          setError,
          errorDTO.error.errors?.saml as Record<string, string>
        );
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <Modal isOpen={isOpen} onClose={onClose} unstable_ModalBackdropScroll>
      <form onSubmit={onSubmit}>
        <ModalHeader>Setup {label} authentication</ModalHeader>
        <ModalBody>
          <Block>
            Single Sign-On (or SSO) allows you to manage your organization’s
            entire membership via a third-party provider.
          </Block>

          {method === 'saml' && (
            <Block marginTop="16px">
              <FormControl
                label="Metadata endpoint"
                error={errors.metadataEndpoint?.message}
              >
                <Input
                  placeholder="https://snuderlstest.okta.com/app/exkligrqDovHJsGmk5d5/sso/saml/metadata"
                  ref={register(REQUIRED_VALIDATION)}
                  name="metadataEndpoint"
                />
              </FormControl>
            </Block>
          )}
        </ModalBody>
        <ModalFooter>
          <Button
            kind="tertiary"
            type="button"
            onClick={onClose}
            size={SIZE.compact}
          >
            Maybe later
          </Button>
          <Button
            type="submit"
            size={SIZE.compact}
            isLoading={isSubmitting}
            $style={{ marginLeft: '8px' }}
          >
            Enable
          </Button>
        </ModalFooter>
        {formError && <FormError error={formError} />}
      </form>
    </Modal>
  );
};
