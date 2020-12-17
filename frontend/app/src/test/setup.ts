import { setupEnvironment } from '@rebrowse/testing';
import * as dotenv from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

dotenv.config({ path: '.env.development' });

// TODO: investigate why baseweb Modal rendering is so slow
jest.setTimeout(60000);
