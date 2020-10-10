import { setupEnvironment } from '@insight/testing';
import * as dotenv from 'dotenv';
import '@testing-library/jest-dom/extend-expect';

setupEnvironment();

dotenv.config({ path: '.env.development' });
