import React, { useState } from 'react';
import { Button } from '@insight/elements';
import { useStyletron } from 'baseui';
import { Modal, ModalBody, ModalFooter, ModalHeader } from 'baseui/modal';
import { AuthApi } from 'api';
import { toaster } from 'baseui/toast';
import { useRouter } from 'next/router';
import { LOGIN_PAGE } from 'shared/constants/routes';

export const DeleteOrganization = () => {
  const router = useRouter();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [_css, theme] = useStyletron();

  const closeModal = () => setIsModalOpen(false);

  const handleDelete = () => {
    setIsDeleting(true);
    AuthApi.organization
      .delete()
      .then(() => router.replace(LOGIN_PAGE))
      .then(() => toaster.positive('Organization deleted', {}))
      .catch(() => {
        setIsDeleting(false);
        toaster.negative(
          'Something went wrong while deleting organization',
          {}
        );
      });
  };

  return (
    <>
      <Button
        $style={{ backgroundColor: theme.colors.negative400 }}
        onClick={() => setIsModalOpen(true)}
      >
        Delete Organization
      </Button>
      <Modal isOpen={isModalOpen} onClose={closeModal}>
        <ModalHeader>Are you sure?</ModalHeader>
        <ModalBody>
          Deleting the organization is permanent and cannot be undone! Are you
          sure you want to continue?
        </ModalBody>
        <ModalFooter>
          <Button kind="tertiary" onClick={closeModal}>
            Cancel
          </Button>
          <Button
            onClick={handleDelete}
            isLoading={isDeleting}
            $style={{
              background: theme.colors.negative400,
              marginLeft: theme.sizing.scale400,
            }}
          >
            Confirm
          </Button>
        </ModalFooter>
      </Modal>
    </>
  );
};
