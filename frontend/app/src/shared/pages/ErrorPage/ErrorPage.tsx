import React from 'react';
import { Block } from 'baseui/block';
import { H1, Paragraph3 } from 'baseui/typography';
import { DeleteAlt } from 'baseui/icon';
import { VerticalAligned } from '@rebrowse/elements';

type Props = {
  statusCode: number;
};

export const ErrorPage = (_props: Props) => {
  return (
    <VerticalAligned
      width="100%"
      height="100%"
      $style={{ textAlign: 'center' }}
    >
      <Block display="flex" justifyContent="center">
        <Block maxWidth="400px">
          <DeleteAlt size={64} />
          <H1 $style={{ fontSize: '20px', lineHeight: '20px' }}>
            It looks like we have a problem
          </H1>
          <Paragraph3>
            We are tracking these errors automatically and are working on the
            fix. Please try again later or contact{' '}
            <a href="mailto:support@rebrowse.dev">support</a> if the error
            persists.
          </Paragraph3>
        </Block>
      </Block>
    </VerticalAligned>
  );
};
