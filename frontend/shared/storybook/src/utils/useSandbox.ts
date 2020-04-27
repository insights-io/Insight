import { useEffect, useRef } from 'react';
import sinon from 'sinon';

import { SetupMocks } from './types';

const useMocks = <T>(setupMocks: SetupMocks<T>) => {
  const sandbox = sinon.createSandbox();
  const didSetupMocks = useRef(false);
  if (!didSetupMocks.current) {
    setupMocks(sandbox);
  }
  didSetupMocks.current = true;

  useEffect(() => {
    return () => {
      sandbox.restore();
    };
  }, [sandbox]);
};

export default useMocks;
