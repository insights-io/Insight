import React from 'react';
import { useStyletron } from 'baseui';
import { invoiceStatusIcon } from 'billing/utils';
import { ListItem, ListItemLabel } from 'baseui/list';
import { StatefulTooltip } from 'baseui/tooltip';
import { ExternalLink } from '@rebrowse/elements';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { FaFileDownload, FaLink } from 'react-icons/fa';
import { capitalize } from 'shared/utils/string';
import type { Invoice } from '@rebrowse/types';

type Props = {
  invoices: Invoice[];
};

export const InvoiceList = ({ invoices }: Props) => {
  const [css, theme] = useStyletron();

  return (
    <ul className={css({ padding: 0 })}>
      {invoices.map((invoice) => {
        const artwork = invoiceStatusIcon[invoice.status](theme);

        return (
          <ListItem
            key={invoice.id}
            artwork={() => (
              <StatefulTooltip
                content={capitalize(invoice.status)}
                placement="top"
                showArrow
              >
                {artwork}
              </StatefulTooltip>
            )}
            endEnhancer={() => (
              <>
                <StatefulTooltip
                  content="Download invoice"
                  placement="top"
                  showArrow
                >
                  <ExternalLink
                    link={`${invoice.link}/pdf`}
                    data-testid="invoice-pdf"
                  >
                    <Button size={SIZE.compact} shape={SHAPE.pill}>
                      <FaFileDownload />
                    </Button>
                  </ExternalLink>
                </StatefulTooltip>
                <StatefulTooltip
                  content="Open invoice"
                  placement="top"
                  showArrow
                >
                  <ExternalLink
                    link={invoice.link}
                    className={css({
                      marginLeft: theme.sizing.scale400,
                    })}
                  >
                    <Button
                      size={SIZE.compact}
                      shape={SHAPE.pill}
                      data-testid="invoice-link"
                    >
                      <FaLink />
                    </Button>
                  </ExternalLink>
                </StatefulTooltip>
              </>
            )}
          >
            <ListItemLabel>
              Amount: {invoice.amountDue} {invoice.currency}
            </ListItemLabel>
          </ListItem>
        );
      })}
    </ul>
  );
};
