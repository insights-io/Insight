import { getByText, getByPlaceholderText } from '@testing-library/testcafe';

import config from '../config';

fixture('<IndexPage />').page(config.baseURL);

test('Can execute a simple exchange via origin input', async (t) => {
  await t
    .typeText(getByPlaceholderText('First name'), 'Joe')
    .typeText(getByPlaceholderText('Last name'), 'Makarena')
    .typeText(getByPlaceholderText('Company'), 'Insight')
    .typeText(getByPlaceholderText('Email'), 'random')
    .typeText(getByPlaceholderText('Password'), 'short')
    .click(getByText('Get started'));
});
