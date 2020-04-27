import React from 'react';
import { StoryFn, StoryContext } from '@storybook/addons';

function fullHeightDecorator<T>(storyFn: StoryFn<T>, context: StoryContext) {
  return (
    <>
      <style
        // eslint-disable-next-line react/no-danger
        dangerouslySetInnerHTML={{
          __html: `
            html, body, #root, #root > div:first-child { height: 100%; }
            .sb-show-main { margin: 0px; }
            #root > div:first-child {
              display: flex;
              flex-direction: column;
            }
        `,
        }}
      />
      {storyFn(context)}
    </>
  );
}

export default fullHeightDecorator;
