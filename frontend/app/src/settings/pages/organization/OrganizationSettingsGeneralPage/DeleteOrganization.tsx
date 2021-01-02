import React, { useState } from 'react';
import { Button } from '@rebrowse/elements';
import { useStyletron } from 'baseui';
import { Modal, ModalBody, ModalFooter, ModalHeader } from 'baseui/modal';
import { toaster } from 'baseui/toast';
import { useRouter } from 'next/router';
import { LOGIN_PAGE } from 'shared/constants/routes';
import { SIZE } from 'baseui/button';
import { client } from 'sdk';

export const DeleteOrganization = () => {
  const router = useRouter();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [_css, theme] = useStyletron();

  const closeModal = () => setIsModalOpen(false);

  const handleDelete = () => {
    setIsDeleting(true);
    client.auth.organizations
      .delete()
      .then(() => {
        router.replace(LOGIN_PAGE);
        toaster.positive('Organization deleted', {});
      })
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
        size={SIZE.compact}
        $style={{ backgroundColor: theme.colors.negative400 }}
        onClick={() => setIsModalOpen(true)}
      >
        Delete Organization
      </Button>
      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        unstable_ModalBackdropScroll
      >
        <ModalHeader>Are you sure?</ModalHeader>
        <ModalBody>
          Deleting the organization is permanent and cannot be undone! Are you
          sure you want to continue?
        </ModalBody>
        <ModalFooter>
          <Button kind="tertiary" onClick={closeModal} size={SIZE.compact}>
            Cancel
          </Button>
          <Button
            onClick={handleDelete}
            isLoading={isDeleting}
            $style={{
              background: theme.colors.negative400,
              marginLeft: theme.sizing.scale400,
            }}
            size={SIZE.compact}
          >
            Confirm
          </Button>
        </ModalFooter>
      </Modal>
    </>
  );
};
