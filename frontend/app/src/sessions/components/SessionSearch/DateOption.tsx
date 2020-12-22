import React from 'react';
import { StatefulPopover, PLACEMENT } from 'baseui/popover';
import { OptionList, OptionListProps } from 'baseui/menu';
import { StatefulCalendar } from 'baseui/datepicker';
import { SIZE } from 'baseui/input';
import { Block } from 'baseui/block';
import { Theme } from 'baseui/theme';
import { Input } from '@rebrowse/elements';

import { OptionItem } from './utils';

type DateOptionProps = Omit<OptionListProps, 'item'> & {
  item: OptionItem;
  fromDate: Date;
  toDate: Date;
  setFromDate: (v: Date) => void;
  setToDate: (v: Date) => void;
  selectingFrom: boolean;
  setSelectingFrom: (v: boolean) => void;
  close: () => void;
  theme: Theme;
};

const DateOption = React.forwardRef<HTMLElement, DateOptionProps>(
  (
    {
      fromDate,
      toDate,
      setFromDate,
      setToDate,
      selectingFrom,
      setSelectingFrom,
      close: closeProp,
      theme,
      ...props
    },
    ref
  ) => {
    const { item } = props;
    const option = (
      <Block ref={ref}>
        <OptionList {...props} />
      </Block>
    );

    if (item.id === 'custom') {
      return (
        <StatefulPopover
          triggerType="click"
          placement={PLACEMENT.auto}
          ignoreBoundary
          content={({ close }) => (
            <Block width="312px">
              <Block
                display="flex"
                width="100%"
                padding="12px"
                backgroundColor={theme.colors.white}
              >
                <Block flex={1}>
                  <Input
                    positive={selectingFrom}
                    size={SIZE.mini}
                    placeholder="From date"
                    value={fromDate.toLocaleDateString()}
                  />
                </Block>

                <Block flex={1}>
                  <Input
                    positive={!selectingFrom}
                    size={SIZE.mini}
                    placeholder="To date"
                    value={toDate.toLocaleDateString()}
                  />
                </Block>
              </Block>

              <StatefulCalendar
                range
                value={[fromDate, toDate]}
                onDayClick={({ event }) => {
                  event.preventDefault();
                  event.stopPropagation();
                }}
                onChange={({ date }) => {
                  const [firstDate, secondDate] = date as Date[];
                  if (!secondDate) {
                    setFromDate(firstDate);
                    setSelectingFrom(false);
                  } else {
                    setToDate(secondDate);
                    setSelectingFrom(true);
                    close();
                    closeProp();
                  }
                }}
              />
            </Block>
          )}
        >
          {option}
        </StatefulPopover>
      );
    }
    return option;
  }
);

export default DateOption;
