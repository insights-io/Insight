module.exports = {
  parser: '@typescript-eslint/parser',
  extends: [
    'eslint:recommended',
    'airbnb',
    'prettier',
    'plugin:jest/recommended',
    'plugin:lodash/recommended',
    'plugin:@typescript-eslint/recommended',
  ],
  plugins: ['prettier', 'jest', 'import', 'lodash'],
  env: {
    browser: true,
    jest: true,
    node: true,
    es6: true,
  },
  rules: {
    'import/no-unresolved': ['off'],
    'import/no-extraneous-dependencies': ['off'],

    'import/prefer-default-export': ['off'],
    'import/extensions': ['off'],

    '@typescript-eslint/explicit-function-return-type': ['off'],
    '@typescript-eslint/no-unused-vars': [
      'error',
      { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
    ],
  },
};
