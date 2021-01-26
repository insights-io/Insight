import { getPage } from 'next-page-tester';

describe('/signup-completed-callback', () => {
  test('As a user I get redirected to rebrowse app', async () => {
    await expect(
      getPage({ route: '/signup-completed-callback' })
    ).rejects.toThrow(
      '[next-page-tester] No matching page found for given route'
    );
  });
});
