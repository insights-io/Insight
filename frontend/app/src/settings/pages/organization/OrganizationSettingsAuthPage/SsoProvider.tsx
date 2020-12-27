import React, { useCallback, useState } from 'react';
import {
  Flex,
  FlexColumn,
  SpacedBetween,
  VerticalAligned,
  Toggle,
} from '@rebrowse/elements';
import {
  SamlConfigurationDTO,
  SamlMethod,
  SamlSsoSetupDTO,
  SsoMethod,
  SsoSetup,
  SsoSetupDTO,
} from '@rebrowse/types';
import { LabelSmall, LabelXSmall } from 'baseui/typography';

import { SsoProviderSetupModal } from './SsoProviderSetupModal';
import { SsoSetupDisableModal } from './SsoSetupDisableModal';

type Props = {
  label: string;
  method: SsoMethod;
  samlMethod?: SamlMethod;
  image: string;
  activeSetup: SsoSetup | undefined;
  deleteSsoSetup: () => Promise<Response>;
  createSsoSetup: (params: {
    method: SsoMethod;
    saml: SamlConfigurationDTO;
  }) => Promise<SsoSetupDTO>;
};

export const SsoProvider = ({
  label,
  image,
  method,
  activeSetup,
  deleteSsoSetup,
  createSsoSetup,
  samlMethod,
}: Props) => {
  const [setupOpen, setSetupOpen] = useState(false);
  const openModel = useCallback(() => setSetupOpen(true), []);
  const closeModal = useCallback(() => setSetupOpen(false), []);

  let isActive = false;
  if (activeSetup) {
    isActive = method === activeSetup.method;
    if (activeSetup.method === 'saml') {
      isActive =
        samlMethod ===
        ((activeSetup as unknown) as Pick<SamlSsoSetupDTO, 'saml'>).saml.method;
    }
  }

  return (
    <SpacedBetween>
      <Flex>
        <img
          src={image}
          alt={`${label} logo`}
          style={{ width: '32px', height: '32px' }}
        />
        <FlexColumn marginLeft="16px">
          <LabelSmall>{label}</LabelSmall>
          <LabelXSmall marginTop="4px">
            Enable your organization to sign in with {label}.
          </LabelXSmall>
        </FlexColumn>
      </Flex>

      <VerticalAligned>
        <Toggle onChange={openModel} checked={isActive} />

        {isActive ? (
          <SsoSetupDisableModal
            label={label}
            onClose={closeModal}
            isOpen={setupOpen}
            disable={deleteSsoSetup}
          />
        ) : (
          <SsoProviderSetupModal
            onClose={closeModal}
            isOpen={setupOpen}
            label={label}
            method={method}
            createSsoSetup={createSsoSetup}
            samlMethod={samlMethod}
          />
        )}
      </VerticalAligned>
    </SpacedBetween>
  );
};
