import { withStyle } from 'baseui';

import Flex from '../Flex';

const VerticalAligned = withStyle(Flex, {
  justifyContent: 'center',
  flexDirection: 'column',
});

export default VerticalAligned;
