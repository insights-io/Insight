const path = require('path');

module.exports = {
  stories: ['../src/**/*.stories.tsx'],
  addons: [
    {
      name: '@storybook/preset-typescript',
      options: {
        options: {
          tsLoaderOptions: {
            configFile: path.resolve(__dirname, './tsconfig.storybook.json'),
          },
        },
      },
    },
    '@storybook/addon-actions',
    '@storybook/addon-knobs/register',
  ],
};
