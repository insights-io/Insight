import React from 'react';
import { Block } from 'baseui/block';
import { Panel, Toggle, VerticalAligned } from '@rebrowse/elements';
import { useMfaSetups } from 'settings/hooks/useMfaSetups';

import { TotpMfaModal, TOTP_LABEL } from './TotpMfaModal';
import { SmsMfaModal, SMS_LABEL } from './SmsMfaModal';
import type { Props } from './types';

export const AccountMfaPanel = ({
  user,
  mfaSetups: initialMfaSetups,
}: Props) => {
  const {
    totpMethodEnabled,
    smsMethodEnabled,
    error,
    disableTotpMethod,
    completeTotpSetup,
    disableSmsMethod,
    completeSmsSetup,
  } = useMfaSetups(initialMfaSetups);

  return (
    <Panel>
      <Panel.Header>Multi-factor authentication</Panel.Header>
      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          explanation="Use your favorite app to generate a time-dependent six-digit code"
        >
          {TOTP_LABEL}
        </Panel.Label>

        <VerticalAligned width="50%">
          <Block width="fit-content">
            <TotpMfaModal
              isEnabled={totpMethodEnabled}
              disable={disableTotpMethod}
              completeSetup={completeTotpSetup}
            >
              {(open) => (
                <Toggle
                  onChange={open}
                  disabled={Boolean(error)}
                  checked={totpMethodEnabled}
                />
              )}
            </TotpMfaModal>
          </Block>
        </VerticalAligned>
      </Panel.Item>

      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          explanation="Use your phone & receive a text message with a short lived code"
        >
          {SMS_LABEL}
        </Panel.Label>

        <VerticalAligned width="50%">
          <Block width="fit-content">
            <SmsMfaModal
              isEnabled={smsMethodEnabled}
              disable={disableSmsMethod}
              completeSetup={completeSmsSetup}
              phoneNumber={user.phoneNumber}
            >
              {(open) => (
                <Toggle
                  checked={smsMethodEnabled}
                  onChange={open}
                  disabled={Boolean(error)}
                />
              )}
            </SmsMfaModal>
          </Block>
        </VerticalAligned>
      </Panel.Item>
    </Panel>
  );
};
