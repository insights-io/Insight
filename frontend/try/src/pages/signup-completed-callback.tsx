import { appBaseURL } from 'shared/config';
import type { GetServerSideProps } from 'next';

// eslint-disable-next-line lodash/prefer-constant
const SignUpCompletedCallback = () => null;

export const getServerSideProps: GetServerSideProps = async () => {
  return {
    redirect: {
      destination: appBaseURL,
      permanent: true,
    },
  };
};

export default SignUpCompletedCallback;
