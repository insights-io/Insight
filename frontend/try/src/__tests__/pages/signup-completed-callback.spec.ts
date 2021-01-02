import { getPage } from 'next-page-tester';

describe('/', () => {
  test('As a user I get redirected to /app', async () => {
    await expect(
      getPage({ route: '/signup-completed-callback' })
    ).rejects.toThrow(
      '[next page tester] No matching page found for given route'
    );
  });
});
