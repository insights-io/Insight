import { setupEnvironment } from '@rebrowse/testing';
import * as dotenv from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

dotenv.config({ path: '.env.development' });

// TODO: investigate why baseweb Modal rendering is so slow
jest.setTimeout(60000);

// https://testing-library.com/docs/dom-testing-library/api-helpers#debugging
process.env.DEBUG_PRINT_LIMIT = '50000';
