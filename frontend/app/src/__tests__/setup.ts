import { setupEnvironment, clearAllCookies } from '@rebrowse/testing';
import * as dotenv from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

dotenv.config({ path: '.env.development' });

jest.setTimeout(30000);

afterEach(() => {
  clearAllCookies();
});
