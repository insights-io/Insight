import React from 'react';
import { Theme } from 'baseui/theme';
import { OptionListProps, OptionList, StatefulMenu, ItemsT } from 'baseui/menu';
import { Block } from 'baseui/block';
import { createBorderRadius } from 'shared/styles/input';
import { StatefulPopover, PLACEMENT } from 'baseui/popover';
import { Button, KIND, SIZE, SHAPE } from 'baseui/button';
import AutocompleteInput from 'shared/components/AutocompleteInput';
import { Plus, Delete } from 'baseui/icon';
import { SpacedBetween, VerticalAligned } from '@insight/elements';

import useAutocomplete from './useAutocomplete';
import {
  SessionFilter,
  FilterOption,
  FILTER_LOOKUPS,
  FILTER_OPTIONS,
} from './utils';

type Props = {
  filter: SessionFilter;
  index: number;
  onPlus: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
  onDelete: (
    id: string,
    event: React.MouseEvent<HTMLButtonElement, MouseEvent>
  ) => void;
  onUpdateFilter: (filter: SessionFilter) => void;
  theme: Theme;
};

const OptionWithIcon = ({ item, ...rest }: OptionListProps) => {
  const typedOption = item as FilterOption;
  const IconComponent = typedOption.icon;
  return (
    <OptionList
      item={item}
      {...rest}
      getItemLabel={() => (
        <>
          <IconComponent style={{ marginRight: '12px' }} />
          {typedOption.label}
        </>
      )}
    />
  );
};

const SessionFilterRow = ({
  filter: { id, key, value },
  index,
  onPlus,
  onDelete,
  onUpdateFilter,
  theme,
}: Props) => {
  const autocompleteOptions = useAutocomplete(key);
  const option = key === undefined ? undefined : FILTER_LOOKUPS[key];

  return (
    <SpacedBetween
      as="li"
      $style={{
        padding: theme.sizing.scale200,
        ...createBorderRadius(theme),
        ':hover': { background: theme.colors.mono400 },
      }}
    >
      <Block display="flex" flex="1">
        {index > 0 && (
          <VerticalAligned marginRight={theme.sizing.scale400}>
            and
          </VerticalAligned>
        )}
        <StatefulPopover
          placement={PLACEMENT.bottom}
          content={({ close }) => (
            <StatefulMenu
              items={(FILTER_OPTIONS as unknown) as ItemsT}
              overrides={{ Option: { component: OptionWithIcon } }}
              onItemSelect={({ item }) => {
                const typedItem = item as FilterOption;
                onUpdateFilter({ id, key: typedItem.key, value: '' });
                close();
              }}
            />
          )}
        >
          <Button kind={KIND.secondary} size={SIZE.mini} shape={SHAPE.pill}>
            {!option ? (
              'Filter event by...'
            ) : (
              <>
                <option.icon style={{ marginRight: theme.sizing.scale400 }} />
                {option.label}
              </>
            )}
          </Button>
        </StatefulPopover>
        <VerticalAligned marginLeft="8px">is</VerticalAligned>
        <VerticalAligned
          flex="1"
          maxWidth="250px"
          marginLeft={theme.sizing.scale400}
        >
          <AutocompleteInput
            placeholder="Type something"
            size={SIZE.mini}
            value={value}
            onChange={(newValue) =>
              onUpdateFilter({ key, id, value: newValue })
            }
            overrides={{
              ControlContainer: { style: createBorderRadius(theme, '38px') },
            }}
            options={autocompleteOptions}
          />
        </VerticalAligned>
      </Block>

      <VerticalAligned marginLeft={theme.sizing.scale400}>
        <Block display="flex" justifyContent="flex-end">
          <Button
            size={SIZE.mini}
            kind={KIND.tertiary}
            shape={SHAPE.pill}
            onClick={onPlus}
          >
            <Plus />
          </Button>
          <Button
            size={SIZE.mini}
            kind={KIND.tertiary}
            shape={SHAPE.pill}
            onClick={(event) => onDelete(id, event)}
          >
            <Delete />
          </Button>
        </Block>
      </VerticalAligned>
    </SpacedBetween>
  );
};
export default React.memo(SessionFilterRow);
