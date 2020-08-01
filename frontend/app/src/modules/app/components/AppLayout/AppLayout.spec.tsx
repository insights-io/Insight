import React from 'react';
import { render } from 'test/utils';
import userEvent from '@testing-library/user-event';
import { sandbox } from '@insight/testing';

import { Base } from './AppLayout.stories';

describe('<AppLayout />', () => {
  describe('Desktop', () => {
    it('Should correctly toggle sidebar', async () => {
      const { findByText, container, push } = render(<Base />);

      userEvent.hover(container.querySelector('a[href="/"]') as HTMLElement);
      await findByText('Insights');

      const toggleSidebarIcon = container.querySelector(
        'svg[title="Chevron Right"]'
      ) as HTMLElement;
      userEvent.hover(toggleSidebarIcon);
      await findByText('Collapse');

      userEvent.click(toggleSidebarIcon);
      const sessionsItem = await findByText('Sessions');

      userEvent.click(sessionsItem);
      sandbox.assert.calledWithExactly(push, '/sessions', '/sessions', {
        shallow: undefined,
      });
    });
  });

  describe('Mobile', () => {
    beforeEach(() => {
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 500,
      });
    });

    it('Should correctly toggle sidebar', async () => {
      const { findByText, container, push } = render(<Base />);

      const toggleSidebarIcon = container.querySelector(
        'svg[title="Menu"]'
      ) as HTMLElement;

      userEvent.hover(toggleSidebarIcon);
      await findByText('Open sidebar');

      userEvent.click(toggleSidebarIcon);
      const sessionsItem = await findByText('Sessions');

      userEvent.click(sessionsItem);
      sandbox.assert.calledWithExactly(push, '/sessions', '/sessions', {
        shallow: undefined,
      });
    });
  });
});
