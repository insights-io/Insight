import React from 'react';
import { Card } from 'baseui/card';

import TfaSetup from './TfaSetup';

const Security = () => {
  return (
    <Card title="Security">
      <TfaSetup />
    </Card>
  );
};

export default Security;
