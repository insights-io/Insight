import React, { useState, useCallback } from 'react';
import { Checkbox } from 'baseui/checkbox';
import { AuthApi } from 'api/auth';
import { toaster } from 'baseui/toast';
import { TfaSetupDTO } from '@rebrowse/types';

import TimeBasedMultiFactorAuthenticationSetupModal from '../TimeBasedMultiFactorAuthenticationSetupModal';
import DisableMultiFactorAuthenticationModal from '../DisableMultiFactorAuthenticationModal';

import { Props } from './types';

const LABEL = 'Authy / Google Authenticator';

const TimeBasedTwoFactorAuthentication = ({
  setupsMaps,
  setupDisabled,
  onMethodDisabled,
  onMethodEnabled,
}: Props) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const closeModal = useCallback(() => {
    setIsModalOpen(false);
  }, []);

  const onTotpDisable = useCallback(() => {
    AuthApi.tfa.setup.disable('totp').then(() => {
      onMethodDisabled();
      closeModal();
      toaster.positive(`${LABEL} multi-factor authentication disabled`, {});
    });
  }, [onMethodDisabled, closeModal]);

  const onTfaConfigured = (newTfaSetup: TfaSetupDTO) => {
    onMethodEnabled(newTfaSetup);
    closeModal();
    toaster.positive(`${LABEL} multi-factor authentication enabled`, {});
  };

  const isEnabled = setupsMaps.totp?.createdAt !== undefined;

  return (
    <li style={{ listStyleType: 'none' }}>
      <Checkbox
        checked={isEnabled}
        disabled={setupDisabled}
        onChange={() => setIsModalOpen(true)}
        overrides={{ Label: { style: { fontSize: '0.8rem' } } }}
      >
        {LABEL}
      </Checkbox>

      {isEnabled ? (
        <DisableMultiFactorAuthenticationModal
          closeModal={closeModal}
          isOpen={isModalOpen}
          header="Are you sure you want to disable multi-factor authentication method?"
          onConfirm={onTotpDisable}
        />
      ) : (
        <TimeBasedMultiFactorAuthenticationSetupModal
          isOpen={isModalOpen}
          onClose={closeModal}
          onTfaConfigured={onTfaConfigured}
        />
      )}
    </li>
  );
};

export default React.memo(TimeBasedTwoFactorAuthentication);
