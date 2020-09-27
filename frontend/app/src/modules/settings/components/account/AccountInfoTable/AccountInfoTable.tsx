import React, { useState } from 'react';
import { Block } from 'baseui/block';
import { Table } from 'baseui/table';
import type { User, UserDTO } from '@insight/types';
import { useStyletron } from 'baseui';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { FaCogs } from 'react-icons/fa';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import VerticalAligned from 'shared/components/VerticalAligned';
import { UpdateUserPayload } from '@insight/sdk/dist/auth';
import ConfigurePhoneNumberModal from 'modules/settings/components/account/ConfigurePhoneNumberModal';

type Props = {
  user: User;
  updateUser: (updateUserPayload: UpdateUserPayload) => Promise<UserDTO>;
  setUser: (user: UserDTO) => void;
};

export const AccountInfoTable = ({ user, updateUser, setUser }: Props) => {
  const [_css, theme] = useStyletron();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const data = [
    ['Full name', user.fullName],
    ['Email', user.email],
    ['Organization ID', user.organizationId],
    ['Member since', user.createdAt.toLocaleDateString()],
    [
      'Phone number',

      <>
        <VerticalAligned marginRight={theme.sizing.scale400}>
          {user.phoneNumber
            ? `${user.phoneNumber.countryCode}${user.phoneNumber.digits}`
            : ''}
        </VerticalAligned>
        <StatefulTooltip
          content="Configure"
          showArrow
          placement={PLACEMENT.auto}
        >
          <Button
            size={SIZE.mini}
            shape={SHAPE.pill}
            onClick={() => setIsModalOpen(true)}
          >
            <FaCogs />
          </Button>
        </StatefulTooltip>
        <ConfigurePhoneNumberModal
          isOpen={isModalOpen}
          setIsModalOpen={setIsModalOpen}
          phoneNumber={user.phoneNumber}
          updateUser={updateUser}
          setUser={setUser}
        />
      </>,
    ],
  ];

  return (
    <Block width="100%" height="fit-content">
      <Table columns={['User Information']} data={data} />
    </Block>
  );
};
