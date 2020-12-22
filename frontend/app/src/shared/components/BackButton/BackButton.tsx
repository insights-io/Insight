import React from 'react';
import { SIZE } from 'baseui/button';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import { Button } from '@rebrowse/elements';
import { ArrowLeft } from 'baseui/icon';

type Props = {
  label: string;
};

export const BackButton = React.memo(({ label }: Props) => {
  return (
    <StatefulTooltip content={label} placement={PLACEMENT.bottom} showArrow>
      <Button size={SIZE.mini} aria-label={label} kind="secondary">
        <ArrowLeft />
      </Button>
    </StatefulTooltip>
  );
});
