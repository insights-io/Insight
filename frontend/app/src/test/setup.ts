import { setupEnvironment } from '@rebrowse/testing';
import * as dotenv from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

dotenv.config({ path: '.env.development' });

// TODO: investigate why baseweb Modal rendering is so slow
jest.setTimeout(60000);

afterEach(() => {
  // Clear all cookies
  if (document.cookie !== '') {
    document.cookie.split(';').forEach((v) => {
      document.cookie = v
        .replace(/^ +/, '')
        .replace(/=.*/, `=;expires=${new Date().toUTCString()};path=/`);
    });
  }
});
