import React from 'react';
import { StoryFn, StoryContext } from '@storybook/addons';

export function fullHeightDecorator<T>(
  storyFn: StoryFn<T>,
  context: StoryContext
) {
  return (
    <>
      <style
        // eslint-disable-next-line react/no-danger
        dangerouslySetInnerHTML={{
          __html: `
            html, body, #root, #root > div:first-child { height: 100% !important; }
            .sb-show-main { margin: 0px !important; padding: 0px !important; }
            #root > div:first-child {
              display: flex !important;
              flex-direction: column !important;
            }
        `,
        }}
      />
      {storyFn(context)}
    </>
  );
}
