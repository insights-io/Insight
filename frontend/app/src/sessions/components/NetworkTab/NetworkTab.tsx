import React from 'react';
import { Block } from 'baseui/block';
import { StyledSpinnerNext } from 'baseui/spinner';
import { useStyletron } from 'baseui';
import {
  StatefulDataTable,
  StringColumn,
  NumericalColumn,
  CategoricalColumn,
} from 'baseui/data-table';
import { BrowserXhrEventDTO } from '@rebrowse/types';

type Props = {
  loading: boolean;
  events: BrowserXhrEventDTO[];
};

const NetworkTab = ({ events, loading }: Props) => {
  const [_css, theme] = useStyletron();

  const columns = [
    CategoricalColumn({
      title: 'Method',
      mapDataToValue: (data: BrowserXhrEventDTO) => data.method,
    }),
    NumericalColumn({
      title: 'Status',
      mapDataToValue: (data: BrowserXhrEventDTO) => data.status,
    }),
    CategoricalColumn({
      title: 'Protocol',
      mapDataToValue: (data: BrowserXhrEventDTO) => data.nextHopProtocol,
    }),
    CategoricalColumn({
      title: 'Initiator',
      mapDataToValue: (data: BrowserXhrEventDTO) => data.initiatorType,
    }),
    CategoricalColumn({
      title: 'Type',
      mapDataToValue: (data: BrowserXhrEventDTO) => data.type,
    }),
    StringColumn({
      title: 'Name',
      mapDataToValue: (data: BrowserXhrEventDTO) => {
        let pathname;
        let search;
        if (data.url[0] === '/') {
          pathname = data.url;
          const [_, searchQuery] = pathname.split('?');
          if (searchQuery) {
            search = `?${searchQuery}`;
          } else {
            search = '';
          }
        } else {
          const url = new URL(data.url);
          pathname = url.pathname;
          search = url.search;
        }
        const split = pathname.split('/');

        return `${split[split.length - 1]}${search}`;
      },
    }),
  ];

  const rows = events.map((e) => ({ data: e, id: e.url }));

  return (
    <Block
      overflow="auto"
      width="100%"
      height={!loading ? `${50 * rows.length + 110}px` : undefined}
    >
      {loading ? (
        <Block display="flex" justifyContent="center">
          <StyledSpinnerNext $style={{ marginTop: theme.sizing.scale500 }} />
        </Block>
      ) : (
        <StatefulDataTable
          columns={columns}
          rows={rows}
          $style={{ width: '100%', overflow: 'auto' }}
          rowHeight={50}
        />
      )}
    </Block>
  );
};

export default React.memo(NetworkTab);
