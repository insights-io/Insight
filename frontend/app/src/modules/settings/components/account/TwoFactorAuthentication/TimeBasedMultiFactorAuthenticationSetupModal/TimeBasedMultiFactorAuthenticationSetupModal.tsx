import React from 'react';
import { Modal, ModalBody, ModalHeader } from 'baseui/modal';
import { TotpMfaSetupForm } from 'modules/auth/components/TotpMfaSetupForm';

import type { Props } from './types';

const TimeBasedMultiFactorAuthenticationSetupModal = ({
  isOpen,
  onClose,
  onTfaConfigured,
}: Props) => {
  return (
    <Modal onClose={onClose} isOpen={isOpen}>
      <ModalHeader>Setup multi-factor authentication</ModalHeader>
      <ModalBody>
        <TotpMfaSetupForm onCompleted={onTfaConfigured} />
      </ModalBody>
    </Modal>
  );
};

export default React.memo(TimeBasedMultiFactorAuthenticationSetupModal);
