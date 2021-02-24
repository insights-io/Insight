import { cleanup } from 'next-page-tester';
import { setupEnvironment } from '@rebrowse/testing';
import * as dotenv from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

dotenv.config({ path: '.env.development' });

jest.setTimeout(15000);

afterEach(cleanup);
