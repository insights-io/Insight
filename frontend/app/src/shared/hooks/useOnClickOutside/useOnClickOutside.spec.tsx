import React from 'react';
import { sandbox } from '@insight/testing';
import userEvent from '@testing-library/user-event';
import { render } from '@testing-library/react';

import useOnClickOutside from './index';

describe('useOnClickOutside', () => {
  it('works with svg', () => {
    const handler = sandbox.stub();

    const Comp = () => {
      const ref = useOnClickOutside<HTMLDivElement>(handler);
      return (
        <>
          <div id="parent" ref={ref}>
            <ul>
              <li>
                <button type="button">
                  <svg id="child" />
                </button>
              </li>
            </ul>
          </div>
          <div id="outside" />
        </>
      );
    };

    const { container } = render(<Comp />);

    userEvent.click(container.querySelector('#child') as SVGElement);
    sandbox.assert.notCalled(handler);

    userEvent.click(container.querySelector('#outside') as SVGElement);
    sandbox.assert.calledOnce(handler);
  });
});
