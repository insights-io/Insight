import { CodeValidityDTO } from '@insight/types';
import { Block } from 'baseui/block';
import { Button } from 'baseui/button';
import { toaster } from 'baseui/toast';
import React, { MutableRefObject, useEffect, useRef, useState } from 'react';
import { Flex, CodeInput } from '@insight/elements';

import { TfaInputMethodProps } from '../types';

type Props = TfaInputMethodProps & {
  sendCode: () => Promise<CodeValidityDTO>;
};

const TfaSmsInputMethod = ({ code, handleChange, error, sendCode }: Props) => {
  const [validitySeconds, setValiditySeconds] = useState(0);
  const countdownInterval = useRef(
    null
  ) as MutableRefObject<NodeJS.Timeout | null>;

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

    countdownInterval.current = setInterval(() => {
      setValiditySeconds((v) => v - 1);
    }, 1000);
  };

  return (
    <Block display="flex">
      <Block>
        <CodeInput
          label="Mobile verification code"
          code={code}
          handleChange={handleChange}
          error={error}
        />
      </Block>
      <Flex
        flexDirection="column"
        justifyContent={error ? 'center' : 'flex-end'}
        width="100%"
      >
        <Button onClick={handleActionClick}>
          {validitySeconds ? `${validitySeconds}s` : 'Send Code'}
        </Button>
      </Flex>
    </Block>
  );
};

export default TfaSmsInputMethod;
