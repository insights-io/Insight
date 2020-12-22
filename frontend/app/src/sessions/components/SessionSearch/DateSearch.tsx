import React, { useState, useEffect, useRef } from 'react';
import { StatefulPopover } from 'baseui/popover';
import { StatefulMenu, ItemsT } from 'baseui/menu';
import { SIZE } from 'baseui/button';
import { ArrowDown } from 'baseui/icon';
import { Theme } from 'baseui/theme';
import { startOfDay, addMonths } from 'date-fns';
import { Button } from '@rebrowse/elements';

import DateOption from './DateOption';
import { OPTIONS, OptionItem, DateRange, createDateRange } from './utils';

type Props = {
  theme: Theme;
  onDateRangeChange: (range: DateRange) => void;
};

const DateSearch = ({ onDateRangeChange, theme }: Props) => {
  const didMountRef = useRef(false);
  const [option, setOption] = useState(OPTIONS[4] as OptionItem);
  const [toDate, setToDate] = useState(() => startOfDay(new Date()));
  const [fromDate, setFromDate] = useState(() => addMonths(toDate, -1));
  const [selectingFrom, setSelectingFrom] = useState(true);

  let text: string = option.label;
  if (option.id === 'custom') {
    text = `${fromDate.toLocaleDateString()} -> ${toDate.toLocaleDateString()}`;
  }

  useEffect(() => {
    if (didMountRef.current) {
      onDateRangeChange(createDateRange(option.id, fromDate, toDate));
    } else {
      didMountRef.current = true;
    }
  }, [fromDate, toDate, option, onDateRangeChange]);

  return (
    <StatefulPopover
      content={({ close }) => (
        <StatefulMenu
          items={(OPTIONS as unknown) as ItemsT}
          overrides={{
            List: { style: { width: '312px' } },
            Option: {
              component: DateOption,
              props: {
                toDate,
                setToDate,
                fromDate,
                setFromDate,
                selectingFrom,
                setSelectingFrom,
                close,
                theme,
              },
            },
          }}
          onItemSelect={({ item }) => {
            const typedItem = item as typeof OPTIONS[number];
            setOption(typedItem);
            if (typedItem.id !== 'custom') {
              close();
            }
          }}
        />
      )}
    >
      <Button size={SIZE.compact} $style={{ minWidth: '150px' }}>
        {text}
        <ArrowDown
          overrides={{
            Svg: { style: { marginLeft: theme.sizing.scale600 } },
          }}
        />
      </Button>
    </StatefulPopover>
  );
};

export default React.memo(DateSearch);
