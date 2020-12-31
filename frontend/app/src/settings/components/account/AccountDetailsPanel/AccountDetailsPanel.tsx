import React from 'react';
import type { PhoneNumber, User, UserDTO } from '@rebrowse/types';
import { SHAPE, SIZE } from 'baseui/button';
import { FaExclamationTriangle } from 'react-icons/fa';
import {
  Button,
  Input,
  Panel,
  PhoneNumberInput,
  VerticalAligned,
} from '@rebrowse/elements';
import { PhoneNumberVerifyModal } from 'settings/components/account/PhoneNumberVerifyModal';
import { format } from 'date-fns';
import { MMM_D_YYYY_HH_MM } from 'shared/constants/date-format';
import { capitalize } from 'shared/utils/string';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import { useUpdateField } from 'shared/hooks/useUpdateField';
import type { UpdateUserPayload } from '@rebrowse/sdk';

type Props = {
  user: User;
  verifyPhoneNumber: (code: number) => Promise<UserDTO>;
  updateUser: (data: UpdateUserPayload) => Promise<UserDTO>;
  updatePhoneNumber: (
    phoneNumber: PhoneNumber | undefined | null
  ) => Promise<UserDTO>;
};

export const AccountDetailsPanel = ({
  user,
  verifyPhoneNumber,
  updateUser,
  updatePhoneNumber,
}: Props) => {
  const {
    value: fullName,
    setValue: setFullName,
    updating: updatingFullName,
    updateCurrent: maybeUpdateFullName,
  } = useUpdateField({
    fieldName: 'fullName',
    currentValue: user.fullName,
    resource: 'user',
    update: updateUser,
  });

  const {
    value: phoneNumber,
    updating: updatingPhoneNumber,
    setValue: setPhoneNumber,
    updateCurrent: maybeUpdatePhoneNumber,
  } = useUpdateField<'phoneNumber', UserDTO, { phoneNumber: PhoneNumber }>({
    fieldName: 'phoneNumber',
    currentValue: user.phoneNumber,
    resource: 'user',
    update: (payload) => updatePhoneNumber(payload.phoneNumber),
    displayValue: (maybePhoneNumber) =>
      `${maybePhoneNumber?.countryCode}${maybePhoneNumber?.digits}`,
  });

  return (
    <Panel>
      <Panel.Header>User Information</Panel.Header>
      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          for="fullName"
          explanation="Full name might help other users identity you easier in various places. If not present, email will be shown"
        >
          Full name
        </Panel.Label>
        <VerticalAligned width="50%">
          <Input
            value={fullName}
            onChange={(event) => setFullName(event.currentTarget.value)}
            id="fullName"
            name="fullName"
            placeholder="Full name"
            size={SIZE.compact}
            disabled={updatingFullName}
            onBlur={() => maybeUpdateFullName()}
          />
        </VerticalAligned>
      </Panel.Item>
      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          for="email"
          explanation="Used for login & email notifications"
        >
          Email
        </Panel.Label>
        <VerticalAligned width="50%">
          <Input
            value={user.email}
            id="email"
            name="email"
            placeholder="Email"
            size={SIZE.compact}
            disabled
          />
        </VerticalAligned>
      </Panel.Item>
      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          for="role"
          explanation="Role determines actions you're able to do in the organization"
        >
          Role
        </Panel.Label>
        <VerticalAligned width="50%">
          <Input
            value={capitalize(user.role)}
            id="role"
            name="role"
            placeholder="Role"
            size={SIZE.compact}
            disabled
          />
        </VerticalAligned>
      </Panel.Item>
      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          for="createdAt"
          explanation="Time at which you joined the family"
        >
          Member since
        </Panel.Label>
        <VerticalAligned width="50%">
          <Input
            value={format(user.createdAt, MMM_D_YYYY_HH_MM)}
            id="createdAt"
            name="createdAt"
            placeholder="Created at"
            size={SIZE.compact}
            disabled
          />
        </VerticalAligned>
      </Panel.Item>

      <Panel.Item display="flex" justifyContent="space-between">
        <Panel.Label
          width="50%"
          for="phoneNumber"
          explanation="Phone number might be used as a multi-factor authentication method"
        >
          Phone number
        </Panel.Label>

        <VerticalAligned width="50%">
          <PhoneNumberInput
            size={SIZE.compact}
            value={phoneNumber}
            onChange={setPhoneNumber}
            placeholder="51111222"
            onBlur={maybeUpdatePhoneNumber}
            onSelectBlur={() => {
              if (phoneNumber?.digits) {
                maybeUpdatePhoneNumber();
              }
            }}
            disabled={updatingPhoneNumber}
            endEnhancer={
              user.phoneNumberVerified || !user.phoneNumber ? null : (
                <PhoneNumberVerifyModal verifyPhoneNumber={verifyPhoneNumber}>
                  {(open) => (
                    <StatefulTooltip
                      content="Verify phone number"
                      placement={PLACEMENT.topRight}
                      showArrow
                    >
                      <Button
                        size={SIZE.mini}
                        shape={SHAPE.pill}
                        onClick={open}
                      >
                        <FaExclamationTriangle />
                      </Button>
                    </StatefulTooltip>
                  )}
                </PhoneNumberVerifyModal>
              )
            }
          />
        </VerticalAligned>
      </Panel.Item>
    </Panel>
  );
};
