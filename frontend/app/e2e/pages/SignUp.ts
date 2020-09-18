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
  public readonly phoneNumberInput = getByPlaceholderText('Phone number');

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
    let promise = t
      .typeText(this.fullNameInput, fullName)
      .typeText(this.companyInput, company)
      .typeText(this.emailInput, email)
      .typeText(this.passwordInput, password);

    if (phoneNumber.length > 0) {
      promise = promise.typeText(this.phoneNumberInput, phoneNumber);
    }

    return promise.click(this.getStartedButton);
  };

  public signUpAndLogin = async (t: TestController, data: SignUpDetails) => {
    await t.navigateTo(config.tryBaseURL);
    await this.signUp(t, data);
    return this.verifyEmail(t);
  };

  public generateRandomCredentials = () => {
    return this.generateRandomCredentialsForDomain('gmail.com');
  };

  public generateRandomCredentialsForDomain = (domain: string) => {
    const password = uuid();
    const email = `${uuid()}@${domain}`;
    return { password, email };
  };

  public generateRandomBussinessCredentials = () => {
    const domain = `${uuid()}.com`;
    return { ...this.generateRandomCredentialsForDomain(domain), domain };
  };

  public verifyEmail = (t: TestController) => {
    const link = findLinkFromDockerLogs();
    if (!link) {
      throw new Error('Sign up link not found');
    }

    return t.navigateTo(link);
  };
}

export default new SignUp();
