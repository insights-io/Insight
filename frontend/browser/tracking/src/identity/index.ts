/* eslint-disable no-console */
/* eslint-disable lodash/prefer-lodash-typecheck */
/* eslint-disable no-restricted-globals */
/* eslint-disable @typescript-eslint/camelcase */
/* eslint-disable no-underscore-dangle */

import { PageIdentity } from 'backend/types';
import {
  MILLIS_IN_SECOND,
  currentTimeSeconds,
  yearFromNow,
  expiresUTC,
} from 'time';

import { Cookie, InsightIdentity, Connected } from './types';

class Identity implements Connected {
  private static storageKey = '_is_uid' as const;
  private readonly _cookie: Cookie;

  constructor(cookie: Cookie) {
    this._cookie = cookie;
  }

  public static initFromCookie = (host: string, orgId: string) => {
    const cookies = document.cookie.split('; ').reduce((acc, value) => {
      const valueSplit = value.split('=');
      return { ...acc, [valueSplit[0]]: valueSplit[1] };
    }, {} as { _is_uid?: string });

    if (process.env.NODE_ENV !== 'production') {
      console.debug('[initFromCookie]', { cookies, host, orgId });
    }
    let maybeCookie = cookies[Identity.storageKey];
    if (!maybeCookie) {
      try {
        maybeCookie = localStorage[Identity.storageKey];
        if (process.env.NODE_ENV !== 'production' && maybeCookie) {
          console.debug('Restored identity from localStorage', maybeCookie);
        }
      } catch (err) {
        // noop
      }
    } else if (process.env.NODE_ENV !== 'production') {
      console.debug('Restored identity from cookie', maybeCookie);
    }

    const decoded = Identity.decodeIdentity(maybeCookie);
    if (decoded) {
      if (decoded.orgId === orgId) {
        if (process.env.NODE_ENV !== 'production') {
          console.debug('Matching orgId, setting identity', decoded);
        }
        return new Identity(decoded);
      }
      if (process.env.NODE_ENV !== 'production') {
        console.debug('Unmatching identity', { decoded, orgId });
      }
    } else if (process.env.NODE_ENV !== 'production') {
      console.debug('Could not parse identity');
    }

    const newIdentity = {
      expiresSeconds: yearFromNow(),
      host,
      orgId,
      uid: '',
      sessionId: '',
    };

    if (process.env.NODE_ENV !== 'production') {
      console.debug('Created new identity', newIdentity);
    }
    return new Identity(newIdentity);
  };

  private static decodeIdentity = (
    encoded: string | undefined
  ): InsightIdentity | undefined => {
    if (!encoded) {
      return undefined;
    }
    const [maybeIdentity, maybeExpiresSeconds] = encoded.split('/');
    const expiresSeconds = parseInt(maybeExpiresSeconds, 10);
    if (isNaN(expiresSeconds) || expiresSeconds < currentTimeSeconds()) {
      if (process.env.NODE_ENV !== 'production') {
        console.debug('identity expired?', { expiresSeconds });
      }
      return undefined;
    }

    const identitySplit = maybeIdentity.split(/[#,]/);
    if (identitySplit.length !== 3) {
      return undefined;
    }

    const [uid, sessionId] = identitySplit[2].split(':');

    return {
      uid,
      sessionId,
      host: identitySplit[0],
      orgId: identitySplit[1],
      expiresSeconds,
    };
  };

  private encode = (expiresSeconds: number) => {
    return `${this._cookie.host}#${this._cookie.orgId}#${this._cookie.uid}:${this._cookie.sessionId}/${expiresSeconds}`;
  };

  public connect = (identity: PageIdentity) => {
    this._cookie.uid = identity.uid;
    this._cookie.sessionId = identity.sessionId;
    this.writeIdentity();
  };

  private writeIdentity = () => {
    const expiresSeconds = this._cookie.expiresSeconds as number;
    const encoded = this.encode(expiresSeconds);
    const expires = expiresUTC(MILLIS_IN_SECOND * expiresSeconds);
    this.setCookie(encoded, expires);
    try {
      localStorage[Identity.storageKey] = encoded;
    } catch (e) {
      // noop
    }
    if (process.env.NODE_ENV !== 'production') {
      console.debug('Wrote identity', encoded);
    }
  };

  private setCookie = (encoded: string, expires: string) => {
    let cookie = `${Identity.storageKey}=${encoded}; domain=; Expires=${expires}; path=/; SameSite=Strict`;
    if (location.protocol === 'https:') {
      cookie += '; Secure';
    }
    document.cookie = cookie;
  };

  public uid = () => {
    return this._cookie.uid;
  };
}

export default Identity;
