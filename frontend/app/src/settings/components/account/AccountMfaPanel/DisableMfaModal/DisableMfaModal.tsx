import React from 'react';
import { Modal, ModalFooter, ModalHeader } from 'baseui/modal';
import { Button } from '@rebrowse/elements';
import { SIZE } from 'baseui/button';

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
    <Modal isOpen={isOpen} onClose={close} unstable_ModalBackdropScroll>
      <ModalHeader>{header}</ModalHeader>
      <ModalFooter>
        <Button kind="tertiary" onClick={close} size={SIZE.compact}>
          Maybe later
        </Button>
        <Button
          onClick={onConfirm}
          size={SIZE.compact}
          $style={{ marginLeft: '16px' }}
        >
          Disable
        </Button>
      </ModalFooter>
    </Modal>
  );
};
