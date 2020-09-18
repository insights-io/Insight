import React from 'react';

import YourPlan from './YourPlan';

export default {
  title: 'billing/components/YourPlan',
};

export const Base = () => (
  <YourPlan
    sessionsUsed={400}
    resetsOn={new Date()}
    dataRetention="1mo"
    plan="free"
  />
);

export const WithFreePlanViolation = () => (
  <YourPlan
    sessionsUsed={1200}
    resetsOn={new Date()}
    dataRetention="1mo"
    plan="free"
  />
);

export const WithUnlimited = () => (
  <YourPlan
    sessionsUsed={1200}
    resetsOn={new Date()}
    dataRetention="1mo"
    plan="enterprise"
  />
);
