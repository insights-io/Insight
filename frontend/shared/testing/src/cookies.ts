export const clearAllCookies = () => {
  if (document.cookie !== '') {
    document.cookie.split(';').forEach((v) => {
      document.cookie = v
        .replace(/^ +/, '')
        .replace(/=.*/, `=;expires=${new Date().toUTCString()};path=/`);
    });
  }
};

export function deleteCookie(name: string) {
  document.cookie = `${name}=;expiresAt=${new Date().toUTCString()};path=/`;
}
