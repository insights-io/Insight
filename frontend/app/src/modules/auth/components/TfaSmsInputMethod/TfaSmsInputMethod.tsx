import React, { MutableRefObject, useEffect, useRef, useState } from 'react';
import { CodeValidityDTO } from '@insight/types';
import { Block } from 'baseui/block';
import { toaster } from 'baseui/toast';
import { Flex, CodeInput, FlexColumn, Button } from '@insight/elements';

import { TfaInputMethodProps } from '../types';

type Props = TfaInputMethodProps & {
  sendCode: () => Promise<CodeValidityDTO>;
};

export const TfaSmsInputMethod = ({
  code,
  handleChange,
  error,
  sendCode,
}: Props) => {
  const [validitySeconds, setValiditySeconds] = useState(0);
  const countdownInterval = useRef(null) as MutableRefObject<number | null>;

  useEffect(() => {
    if (countdownInterval.current !== null && validitySeconds === 0) {
      clearInterval(countdownInterval.current);
      countdownInterval.current = null;
    }
  }, [validitySeconds, countdownInterval]);

  const handleActionClick = async (
    event: React.MouseEvent<HTMLButtonElement, MouseEvent>
  ) => {
    event.stopPropagation();
    event.preventDefault();
    if (validitySeconds !== 0) {
      return;
    }

    const response = await sendCode();
    toaster.positive('Success', {});
    setValiditySeconds(response.validitySeconds);

    countdownInterval.current = window.setInterval(() => {
      setValiditySeconds((v) => v - 1);
    }, 1000);
  };

  return (
    <Flex>
      <Block>
        <CodeInput
          label="Mobile verification code"
          code={code}
          handleChange={handleChange}
          error={error}
        />
      </Block>
      <FlexColumn justifyContent={error ? 'center' : 'flex-end'} width="100%">
        <Button onClick={handleActionClick}>
          {validitySeconds ? `${validitySeconds}s` : 'Send Code'}
        </Button>
      </FlexColumn>
    </Flex>
  );
};
