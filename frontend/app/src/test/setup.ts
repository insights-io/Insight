import { setupEnvironment } from '@insight/testing';
import { config } from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

config({ path: '.env.development' });
