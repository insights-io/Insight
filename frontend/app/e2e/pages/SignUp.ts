import { getByPlaceholderText, getByText } from '@testing-library/testcafe';
import { v4 as uuid } from 'uuid';

import config from '../config';
import { findLinkFromDockerLogs } from '../utils';

import Login, { LoginCredentials } from './Login';

export type SignUpProperties = {
  fullName: string;
  company: string;
  phoneNumber: string;
};

export type SignUpDetails = LoginCredentials & Partial<SignUpProperties>;

class SignUp {
  /* Selectors */
  public readonly emailInput = Login.emailInput;
  public readonly passwordInput = Login.passwordInput;
  public readonly fullNameInput = getByPlaceholderText('Full name');
  public readonly companyInput = getByPlaceholderText('Company');
  public readonly getStartedButton = getByText('Get started');
  public readonly phoneNumberInput = getByText('Phone number');

  public readonly userFullNameDefault = 'Miha Novak';
  public readonly userCompanyDefault = 'Insight';

  /* Utils */
  public signUp = (
    t: TestController,
    {
      email,
      password,
      phoneNumber = '',
      fullName = this.userFullNameDefault,
      company = this.userCompanyDefault,
    }: SignUpDetails
  ) => {
    return t
      .typeText(this.fullNameInput, fullName)
      .typeText(this.companyInput, company)
      .typeText(this.emailInput, email)
      .typeText(this.passwordInput, password)
      .typeText(this.phoneNumberInput, phoneNumber)
      .click(this.getStartedButton);
  };

  public signUpAndLogin = async (t: TestController, data: SignUpDetails) => {
    await t.navigateTo(config.tryBaseURL);
    await this.signUp(t, data);
    return this.signUpVerifyEmail(t);
  };

  public generateRandomCredentials = () => {
    const password = uuid();
    const email = `${uuid()}@gmail.com`;
    return { password, email };
  };

  public signUpVerifyEmail = (t: TestController) => {
    const link = findLinkFromDockerLogs();
    if (!link) {
      throw new Error('Sign up link not found');
    }

    return t.navigateTo(link);
  };
}

export default new SignUp();
