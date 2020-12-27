import React, { useCallback } from 'react';
import { toaster } from 'baseui/toast';
import { DisableMfaModal } from 'settings/components/account/AccountMfaPanel/DisableMfaModal';
import { TotpMfaSetupModal } from 'settings/components/account/AccountMfaPanel/TotpMfaSetupModal';
import { useIsOpen } from 'shared/hooks/useIsOpen';

import type { Props } from './types';

export const TOTP_LABEL = 'Authy / Google Authenticator';

export const TotpMfaModal = ({
  isEnabled,
  disable,
  completeSetup,
  children,
}: Props) => {
  const { isOpen, open, close } = useIsOpen();

  const onDisable = useCallback(() => {
    disable().then(() => {
      close();
      toaster.positive(
        `${TOTP_LABEL} multi-factor authentication disabled`,
        {}
      );
    });
  }, [disable, close]);

  const onComplete = useCallback(
    (code: number) => {
      return completeSetup(code).then((setup) => {
        close();
        toaster.positive(
          `${TOTP_LABEL} multi-factor authentication enabled`,
          {}
        );
        return setup;
      });
    },
    [completeSetup, close]
  );

  return (
    <>
      {children(open)}
      {isEnabled ? (
        <DisableMfaModal
          header="Are you sure you want to disable multi-factor authentication method?"
          close={close}
          isOpen={isOpen}
          onConfirm={onDisable}
        />
      ) : (
        <TotpMfaSetupModal
          isOpen={isOpen}
          onClose={close}
          completeSetup={onComplete}
        />
      )}
    </>
  );
};
