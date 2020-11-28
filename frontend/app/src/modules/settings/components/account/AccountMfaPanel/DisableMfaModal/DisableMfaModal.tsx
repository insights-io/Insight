import React from 'react';
import { SHAPE } from 'baseui/button';
import { Modal, ModalFooter, ModalHeader } from 'baseui/modal';
import { Button } from '@rebrowse/elements';

type Props = {
  isOpen: boolean;
  close: () => void;
  onConfirm: () => void;
  header: string;
};

export const DisableMfaModal = ({
  isOpen,
  close,
  onConfirm,
  header,
}: Props) => {
  return (
    <Modal isOpen={isOpen} onClose={close}>
      <ModalHeader>{header}</ModalHeader>
      <ModalFooter>
        <Button shape={SHAPE.pill} kind="tertiary" onClick={close}>
          Mayber later
        </Button>
        <Button
          shape={SHAPE.pill}
          onClick={onConfirm}
          $style={{ marginLeft: '16px' }}
        >
          Disable
        </Button>
      </ModalFooter>
    </Modal>
  );
};
