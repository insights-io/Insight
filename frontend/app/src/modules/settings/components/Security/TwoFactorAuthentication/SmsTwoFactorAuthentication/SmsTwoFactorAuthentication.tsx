import { AuthApi } from 'api';
import { Block } from 'baseui/block';
import { Button, SHAPE } from 'baseui/button';
import { Checkbox } from 'baseui/checkbox';
import { Modal, ModalBody, ModalFooter, ModalHeader } from 'baseui/modal';
import { StatefulTooltip } from 'baseui/tooltip';
import React, { useState, useRef, MutableRefObject, useEffect } from 'react';
import Flex from 'shared/components/Flex';
import CodeInput from 'shared/components/CodeInput';
import useCodeInput from 'shared/hooks/useCodeInput';
import { toaster } from 'baseui/toast';

import DisableTwoFactorAuthenticationModal from '../DisableTwoFactorAuthenticationModal';

import { Props } from './types';

const LABEL = 'Text message';

const SmsTwoFactorAuthentication = ({
  setupsMaps,
  setupDisabled,
  onMethodEnabled,
  onMethodDisabled,
  phoneNumber,
}: Props) => {
  const hasPhoneNumber = Boolean(phoneNumber);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const isEnabled = setupsMaps.sms?.createdAt !== undefined;
  const closeModal = () => setIsModalOpen(false);
  const openModal = () => setIsModalOpen(true);
  const [validitySeconds, setValiditySeconds] = useState(0);
  const countdownInterval = useRef(
    null
  ) as MutableRefObject<NodeJS.Timeout | null>;

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

  const sendCode = async (
    event: React.MouseEvent<HTMLButtonElement, MouseEvent>
  ) => {
    event.stopPropagation();
    event.preventDefault();
    if (validitySeconds !== 0) {
      return;
    }

    const response = await AuthApi.tfa.sms.setupSendCode();
    toaster.positive('Success', {});
    setValiditySeconds(response.validitySeconds);

    countdownInterval.current = setInterval(() => {
      setValiditySeconds((v) => v - 1);
    }, 1000);
  };

  useEffect(() => {
    if (countdownInterval.current !== null && validitySeconds === 0) {
      clearInterval(countdownInterval.current);
      countdownInterval.current = null;
    }
  }, [validitySeconds, countdownInterval]);

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
              <Block display="flex">
                <Block>
                  <CodeInput
                    label="Mobile verification code"
                    code={code}
                    handleChange={handleChange}
                    error={codeError}
                  />
                </Block>
                <Flex
                  flexDirection="column"
                  justifyContent={codeError ? 'center' : 'flex-end'}
                  width="100%"
                >
                  <Button onClick={sendCode}>
                    {validitySeconds ? `${validitySeconds}s` : 'Send Code'}
                  </Button>
                </Flex>
              </Block>
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
