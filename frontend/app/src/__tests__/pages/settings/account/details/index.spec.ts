import { sandbox } from '@rebrowse/testing';
import { PhoneNumber } from '@rebrowse/types';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { ACCOUNT_SETTINGS_DETAILS_PAGE } from 'shared/constants/routes';
import { capitalize } from 'shared/utils/string';
import { REBROWSE_ADMIN_DTO } from '__tests__/data';
import { mockAccountSettingsDetailsPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/account/details', () => {
  /* Data */
  const route = ACCOUNT_SETTINGS_DETAILS_PAGE;
  const fullName = REBROWSE_ADMIN_DTO.fullName as string;
  const fullNameUpdate = '-extra';
  const updatedFullName = `${fullName}${fullNameUpdate}`;

  const phoneNumber = REBROWSE_ADMIN_DTO.phoneNumber as PhoneNumber;
  const phoneNumberDigitsUpdate = '1234567';
  const updatedPhoneNumber: PhoneNumber = {
    digits: phoneNumberDigitsUpdate,
    countryCode: phoneNumber.countryCode,
  };

  test('As a user I can see details about my account', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    mockAccountSettingsDetailsPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

    expect(screen.getByDisplayValue(fullName)).toBeInTheDocument();

    expect(
      screen.getByDisplayValue(REBROWSE_ADMIN_DTO.email)
    ).toBeInTheDocument();

    expect(
      screen.getByDisplayValue(capitalize(REBROWSE_ADMIN_DTO.role))
    ).toBeInTheDocument();
  });

  test('As a user I can update my full name', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { updateUserStub } = mockAccountSettingsDetailsPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

    userEvent.type(screen.getByDisplayValue(fullName), fullNameUpdate);
    userEvent.tab();

    await screen.findByText(
      `Successfully changed user full name from "${fullName}" to "${updatedFullName}"`
    );

    sandbox.assert.calledWithExactly(
      updateUserStub,
      { fullName: updatedFullName },
      INCLUDE_CREDENTIALS
    );
  });

  test('As a user I can update my phone number', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { updatePhoneNumberStub } = mockAccountSettingsDetailsPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    /* Client */
    renderPage(page);

    const digitsInput = screen.getByLabelText(
      'Please enter a phone number without the country dial code.'
    );
    userEvent.clear(digitsInput);
    userEvent.type(digitsInput, phoneNumberDigitsUpdate);
    userEvent.tab();

    await screen.findByText(
      `Successfully changed user phone number from "${phoneNumber.countryCode}${phoneNumber.digits}" to "${updatedPhoneNumber.countryCode}${updatedPhoneNumber.digits}"`
    );

    sandbox.assert.calledWithExactly(
      updatePhoneNumberStub,
      updatedPhoneNumber,
      INCLUDE_CREDENTIALS
    );
  });
});
