import { appBaseURL } from 'shared/config';
import type { GetServerSideProps } from 'next';

export default function SignUpCompletedCallback() {
  return null;
}

export const getServerSideProps: GetServerSideProps = async () => {
  return {
    redirect: {
      destination: appBaseURL,
      permanent: true,
    },
  };
};
