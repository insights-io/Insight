import { Button, SHAPE } from 'baseui/button';
import { Modal, ModalFooter, ModalHeader } from 'baseui/modal';
import React from 'react';

type Props = {
  isOpen: boolean;
  closeModal: () => void;
  onConfirm: () => void;
  header: string;
};

const DisableTwoFactorAuthenticationModal = ({
  isOpen,
  closeModal,
  onConfirm,
  header,
}: Props) => {
  return (
    <Modal isOpen={isOpen} onClose={closeModal}>
      <ModalHeader>{header}</ModalHeader>
      <ModalFooter>
        <Button shape={SHAPE.pill} kind="tertiary" onClick={closeModal}>
          Cancel
        </Button>
        <Button shape={SHAPE.pill} onClick={onConfirm}>
          Yes
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default React.memo(DisableTwoFactorAuthenticationModal);
