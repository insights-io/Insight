import React from 'react';
import { Paragraph3 } from 'baseui/typography';
import { APIError } from '@insight/types';
import { useStyletron } from 'baseui';

import Flex from '../Flex';

type Props = {
  error: APIError;
};

const FormError = React.forwardRef<HTMLElement, Props>(({ error }, ref) => {
  const [_css, theme] = useStyletron();
  return (
    <Flex ref={ref} justifyContent="center" marginTop={theme.sizing.scale600}>
      <Paragraph3 color={theme.colors.borderError}>{error.message}</Paragraph3>
    </Flex>
  );
});

export default React.memo(FormError);
