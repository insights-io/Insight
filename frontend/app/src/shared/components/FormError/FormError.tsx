import React from 'react';
import { Paragraph3 } from 'baseui/typography';
import { useStyletron } from 'baseui';
import { Flex } from '@rebrowse/elements';

type HasMessage = {
  message: string;
};

type Props = {
  error: HasMessage;
};

export const FormError = React.forwardRef<HTMLElement, Props>(
  ({ error }, ref) => {
    const [_css, theme] = useStyletron();
    return (
      <Flex ref={ref} justifyContent="center" marginTop={theme.sizing.scale600}>
        <Paragraph3 color={theme.colors.borderError}>
          {error.message}
        </Paragraph3>
      </Flex>
    );
  }
);
