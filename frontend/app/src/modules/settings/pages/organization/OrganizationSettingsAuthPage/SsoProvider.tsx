import React, { useCallback, useState } from 'react';
import {
  Flex,
  FlexColumn,
  SpacedBetween,
  VerticalAligned,
  Toggle,
} from '@insight/elements';
import {
  SamlMethod,
  SamlSsoSetupDTO,
  SsoMethod,
  SsoSetup,
  SsoSetupDTO,
} from '@insight/types';
import { LabelSmall, LabelXSmall } from 'baseui/typography';

import { SsoProviderSetupModal } from './SsoProviderSetupModal';
import { SsoSetupDisableModal } from './SsoSetupDisableModal';

type Props = {
  label: string;
  method: SsoMethod;
  samlMethod?: SamlMethod;
  image: string;
  activeSetup: SsoSetup | undefined;
  setActiveSetup: (setup: SsoSetupDTO | undefined) => void;
};

export const SsoProvider = ({
  label,
  image,
  method,
  activeSetup,
  setActiveSetup,
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
          alt={label}
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
            setActiveSetup={setActiveSetup}
          />
        ) : (
          <SsoProviderSetupModal
            onClose={closeModal}
            isOpen={setupOpen}
            label={label}
            method={method}
            setActiveSetup={setActiveSetup}
            samlMethod={samlMethod}
          />
        )}
      </VerticalAligned>
    </SpacedBetween>
  );
};
