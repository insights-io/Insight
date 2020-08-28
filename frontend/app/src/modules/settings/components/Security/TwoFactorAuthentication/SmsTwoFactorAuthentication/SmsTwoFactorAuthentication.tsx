import { AuthApi } from 'api';
import { Button, SHAPE } from 'baseui/button';
import { Checkbox } from 'baseui/checkbox';
import { Modal, ModalBody, ModalFooter, ModalHeader } from 'baseui/modal';
import { StatefulTooltip } from 'baseui/tooltip';
import React, { useState } from 'react';
import useCodeInput from 'shared/hooks/useCodeInput';
import { toaster } from 'baseui/toast';
import TfaSmsInputMethod from 'modules/auth/components/TfaSmsInputMethod';

import DisableTwoFactorAuthenticationModal from '../DisableTwoFactorAuthenticationModal';

import { Props } from './types';

const LABEL = 'Text message';

const SmsTwoFactorAuthentication = ({
  setupsMaps,
  setupDisabled,
  onMethodEnabled,
  onMethodDisabled,
  phoneNumber,
  phoneNumberVerified,
}: Props) => {
  const hasPhoneNumber = Boolean(phoneNumber);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const isEnabled =
    setupsMaps.sms?.createdAt !== undefined && phoneNumberVerified;
  const closeModal = () => setIsModalOpen(false);
  const openModal = () => setIsModalOpen(true);

  const {
    code,
    handleChange,
    submitButtonRef,
    codeError,
    isSubmitting,
    handleSubmit,
  } = useCodeInput({
    submitAction: (paramCode) => {
      return AuthApi.tfa.setupComplete('sms', paramCode).then((setup) => {
        toaster.positive(`${LABEL} two factor authentication enabled`, {});
        onMethodEnabled(setup);
        closeModal();
      });
    },
    handleError: (error, setError) => {
      setError(error.error);
    },
  });

  const disableSmsTwoFactorAuthentication = () => {
    AuthApi.tfa.disable('sms').then(() => {
      onMethodDisabled();
      closeModal();
      toaster.positive(`${LABEL} two factor authentication disabled`, {});
    });
  };

  return (
    <>
      <StatefulTooltip
        showArrow
        content={
          hasPhoneNumber
            ? undefined
            : 'Verify your phone number to enable text message two factor authentication'
        }
      >
        <li style={{ listStyleType: 'none' }}>
          <Checkbox
            onChange={openModal}
            checked={isEnabled}
            disabled={setupDisabled || !hasPhoneNumber}
            overrides={{ Label: { style: { fontSize: '0.8rem' } } }}
          >
            {LABEL}
          </Checkbox>
        </li>
      </StatefulTooltip>

      {isEnabled ? (
        <DisableTwoFactorAuthenticationModal
          header="Are you sure you want to disable text message two factor authentication?"
          onConfirm={disableSmsTwoFactorAuthentication}
          isOpen={isModalOpen}
          closeModal={closeModal}
        />
      ) : (
        <Modal isOpen={isModalOpen} onClose={closeModal}>
          <ModalHeader>
            Configure text message two factor authentication
          </ModalHeader>
          <form
            noValidate
            onSubmit={(event) => {
              event.preventDefault();
              handleSubmit(code);
            }}
          >
            <ModalBody>
              <TfaSmsInputMethod
                code={code}
                error={codeError}
                handleChange={handleChange}
                sendCode={AuthApi.tfa.sms.setupSendCode}
              />
            </ModalBody>
            <ModalFooter>
              <Button shape={SHAPE.pill} kind="tertiary" onClick={closeModal}>
                Cancel
              </Button>
              <Button
                shape={SHAPE.pill}
                ref={submitButtonRef}
                type="submit"
                isLoading={isSubmitting}
              >
                Submit
              </Button>
            </ModalFooter>
          </form>
        </Modal>
      )}
    </>
  );
};

export default React.memo(SmsTwoFactorAuthentication);
