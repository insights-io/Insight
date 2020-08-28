import React, { useState } from 'react';
import { Block } from 'baseui/block';
import { Table } from 'baseui/table';
import { User } from '@insight/types';
import { useStyletron } from 'baseui';
import FlexColumn from 'shared/components/FlexColumn';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { FaCogs } from 'react-icons/fa';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import VerticalAligned from 'shared/components/VerticalAligned';

import ChangePassword from '../ChangePassword';
import Security from '../Security';
import ConfigurePhoneNumberModal from '../ConfigurePhoneNumberModal';

type Props = {
  user: User;
};

const UserSettings = ({ user }: Props) => {
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
          {user.phoneNumber}
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
        />
      </>,
    ],
  ];

  return (
    <Block display="flex">
      <FlexColumn flex="1">
        <Block display="flex" flex="1">
          <Block width="100%" height="fit-content">
            <Table columns={['User Information']} data={data} />
          </Block>
        </Block>
        <Block>
          <Security user={user} />
        </Block>
      </FlexColumn>

      <ChangePassword
        overrides={{
          Root: {
            style: {
              maxWidth: '400px',
              width: '100%',
              marginLeft: theme.sizing.scale600,
            },
          },
        }}
      />
    </Block>
  );
};

export default React.memo(UserSettings);
