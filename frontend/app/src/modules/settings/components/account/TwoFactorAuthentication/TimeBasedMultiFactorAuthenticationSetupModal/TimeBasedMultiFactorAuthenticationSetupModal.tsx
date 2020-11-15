import React from 'react';
import { Modal, ModalBody, ModalHeader } from 'baseui/modal';
import { TimeBasedMultiFactorAuthenticationForm } from 'modules/auth/components/TimeBasedMultiFactorAuthenticationForm';

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
        <TimeBasedMultiFactorAuthenticationForm onCompleted={onTfaConfigured} />
      </ModalBody>
    </Modal>
  );
};

export default React.memo(TimeBasedMultiFactorAuthenticationSetupModal);
