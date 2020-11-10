import React from 'react';
import { Modal, ModalBody, ModalHeader } from 'baseui/modal';
import { TimeBasedTwoFactorAuthenticationForm } from 'modules/auth/components/TimeBasedTwoFactorAuthenticationForm';

import { Props } from './types';

const TimeBasedTwoFactorAuthenticationSetupModal = ({
  isOpen,
  onClose,
  onTfaConfigured,
}: Props) => {
  return (
    <Modal onClose={onClose} isOpen={isOpen}>
      <ModalHeader>Setup two factor authentication</ModalHeader>
      <ModalBody>
        <TimeBasedTwoFactorAuthenticationForm
          onTfaConfigured={onTfaConfigured}
        />
      </ModalBody>
    </Modal>
  );
};

export default React.memo(TimeBasedTwoFactorAuthenticationSetupModal);
