import React, { useState, useCallback } from 'react';
import {
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_AUTH_TOKENS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import { AccountSettingsPageLayout } from 'modules/settings/components/account/AccountSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import { AuthTokenDTO } from '@insight/types';
import { useAuthTokens } from 'modules/settings/hooks/useAuthTokens';
import { Block } from 'baseui/block';
import { Delete, Plus } from 'baseui/icon';
import {
  StyledTable,
  StyledHead,
  StyledHeadCell,
  StyledBody,
  StyledRow,
  StyledCell,
} from 'baseui/table';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { StatefulTooltip } from 'baseui/tooltip';
import { Modal, ModalBody, ModalFooter, ModalHeader } from 'baseui/modal';
import { AuthApi } from 'api';
import { SpacedBetween } from '@insight/elements';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_AUTH_TOKENS_PATH_PART,
];

type Props = {
  authTokens: AuthTokenDTO[];
};

export const AccountSettingsAuthTokensPage = ({
  authTokens: initialAuthTokens,
}: Props) => {
  const [selectedAuthToken, setSelectedAuthToken] = useState<string>();
  const { authTokens, removeAuthToken, addAuthToken } = useAuthTokens(
    initialAuthTokens
  );
  const [deletingAuthToken, setDeletingAuthToken] = useState(false);
  const [creatingAuthToken, setCreatingAuthToken] = useState(false);

  const createAuthToken = () => {
    if (creatingAuthToken) {
      return;
    }
    setCreatingAuthToken(true);
    AuthApi.sso.token
      .create()
      .then(addAuthToken)
      .finally(() => setCreatingAuthToken(false));
  };

  const deleteAuthToken = useCallback(() => {
    if (deletingAuthToken || !selectedAuthToken) {
      return;
    }

    setDeletingAuthToken(true);

    AuthApi.sso.token
      .delete(selectedAuthToken)
      .then(() => {
        setSelectedAuthToken(undefined);
        removeAuthToken(selectedAuthToken);
      })
      .finally(() => {
        setDeletingAuthToken(false);
      });
  }, [selectedAuthToken, deletingAuthToken, removeAuthToken]);

  return (
    <AccountSettingsPageLayout path={PATH} header="Auth Tokens">
      <SpacedBetween>
        <div />
        <Button
          size={SIZE.compact}
          shape={SHAPE.pill}
          isLoading={creatingAuthToken}
          onClick={createAuthToken}
        >
          <Plus />
          Create new
        </Button>
      </SpacedBetween>

      <Block width="100%" height="fit-content" marginTop="24px">
        <StyledTable className="auth-tokens">
          <StyledHead>
            <StyledHeadCell>Token</StyledHeadCell>
            <StyledHeadCell>Created at</StyledHeadCell>
            <StyledHeadCell>Actions</StyledHeadCell>
          </StyledHead>
          <StyledBody>
            {authTokens.map(({ token, createdAt }) => {
              return (
                <StyledRow key={token}>
                  <StyledCell>{token}</StyledCell>
                  <StyledCell>{createdAt.toLocaleDateString()}</StyledCell>
                  <StyledCell>
                    <StatefulTooltip
                      content="Revoke"
                      placement="right"
                      showArrow
                    >
                      <Button
                        size="mini"
                        shape={SHAPE.pill}
                        onClick={() => setSelectedAuthToken(token)}
                      >
                        <Delete />
                      </Button>
                    </StatefulTooltip>
                  </StyledCell>
                </StyledRow>
              );
            })}
          </StyledBody>
        </StyledTable>
      </Block>

      <Modal
        isOpen={selectedAuthToken !== undefined}
        onClose={() => setSelectedAuthToken(undefined)}
      >
        <ModalHeader>Are you sure you want to revoke Auth Token?</ModalHeader>
        <ModalBody>
          Please make sure to remove all of its usages before revoking access.
          This is an instant operation and cannot be reverted.
        </ModalBody>

        <ModalFooter>
          <Button
            shape={SHAPE.pill}
            kind="tertiary"
            onClick={() => setSelectedAuthToken(undefined)}
          >
            Cancel
          </Button>
          <Button
            shape={SHAPE.pill}
            onClick={deleteAuthToken}
            isLoading={deletingAuthToken}
          >
            Yes
          </Button>
        </ModalFooter>
      </Modal>
    </AccountSettingsPageLayout>
  );
};
