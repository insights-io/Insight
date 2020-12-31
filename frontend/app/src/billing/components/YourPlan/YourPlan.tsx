import React, { useMemo } from 'react';
import { H4, ParagraphSmall } from 'baseui/typography';
import { Block } from 'baseui/block';
import { SpacedBetween, Panel, Button } from '@rebrowse/elements';
import { ProgressBar, ProgressBarProps } from 'baseui/progress-bar';
import { useStyletron } from 'baseui';
import { format } from 'date-fns';
import { SIZE } from 'baseui/button';
import type { Plan } from '@rebrowse/types';

type Props = {
  resetsOn?: Date;
  plan?: Plan['type'];
  sessionsUsed?: number;
  dataRetention?: Plan['dataRetention'];
  isLoading: boolean;
  onUpgradeClick: (
    event: React.MouseEvent<HTMLButtonElement, MouseEvent>
  ) => void;
};

export const YourPlan = ({
  plan,
  sessionsUsed,
  resetsOn,
  isLoading,
  dataRetention,
  onUpgradeClick,
}: Props) => {
  const [_css, theme] = useStyletron();

  const {
    colors: { negative400: planViolationErrorColor },
  } = theme;

  const totalSessions = plan === 'free' ? 1000 : undefined;

  const violatesPlan =
    sessionsUsed && totalSessions !== undefined && sessionsUsed > totalSessions;

  const totalSessionsText =
    totalSessions === undefined ? '\u221e' : totalSessions.toLocaleString();

  const progressBarProps: ProgressBarProps = useMemo(
    () =>
      totalSessions === undefined || sessionsUsed === undefined
        ? { infinite: true }
        : {
            value: (sessionsUsed / totalSessions) * 100,
            successValue: 100,
          },
    [sessionsUsed, totalSessions]
  );

  return (
    <Panel>
      <Panel.Header>Your plan</Panel.Header>
      <Panel.Item>
        <SpacedBetween>
          <H4 marginTop={0} marginBottom={theme.sizing.scale500}>
            Rebrowse {plan ? plan[0].toUpperCase() + plan.substring(1) : ''}
          </H4>
          <Block flex={1} maxWidth="400px">
            <ProgressBar
              {...progressBarProps}
              showLabel
              overrides={{
                Label: {
                  style: {
                    textAlign: 'right',
                    marginRight: theme.sizing.scale500,
                  },
                },
                BarProgress: {
                  style: {
                    backgroundColor: violatesPlan
                      ? planViolationErrorColor
                      : undefined,
                  },
                },
              }}
              getProgressLabel={() => (
                <>
                  <span>
                    {sessionsUsed} of {totalSessionsText} sessions
                  </span>
                  <br />
                  {resetsOn && (
                    <span>
                      {`Resets ${format(resetsOn, 'MMM d, yyyy')} at ${format(
                        resetsOn,
                        'h:mma'
                      )}`}
                    </span>
                  )}
                </>
              )}
            />
          </Block>
        </SpacedBetween>
        <Block>
          <ParagraphSmall>
            Sessions: <b>{totalSessionsText}/mo</b>
          </ParagraphSmall>
        </Block>
        <Block>
          <ParagraphSmall>
            Data history:{' '}
            <b>{dataRetention === '1m' ? '1 month' : dataRetention}</b>
          </ParagraphSmall>
        </Block>

        {plan !== 'enterprise' && (
          <Button
            size={SIZE.compact}
            $style={{ width: '100%', maxWidth: '300px', marginTop: '16px' }}
            onClick={onUpgradeClick}
            disabled={isLoading}
          >
            Upgrade
          </Button>
        )}
      </Panel.Item>
    </Panel>
  );
};
