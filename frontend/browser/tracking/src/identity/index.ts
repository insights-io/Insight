/* eslint-disable camelcase */
/* eslint-disable no-restricted-globals */
/* eslint-disable no-underscore-dangle */

import { logger } from 'logger';
import type { PageVisitSessionLink } from '@rebrowse/types';
import {
  MILLIS_IN_SECOND,
  currentTimeSeconds,
  yearFromNow,
  expiresUTC,
} from 'time';

import { Cookie, RebrowseIdentity, Connected } from './types';

class Identity implements Connected {
  public static storageKey = '_is_device_id' as const;
  private readonly _cookie: Cookie;

  constructor(cookie: Cookie) {
    this._cookie = cookie;
  }

  public static initFromCookie = (host: string, organizationId: string) => {
    const cookies = document.cookie.split('; ').reduce((acc, value) => {
      const valueSplit = value.split('=');
      return { ...acc, [valueSplit[0]]: valueSplit[1] };
    }, {} as { _is_device_id?: string });

    if (process.env.NODE_ENV !== 'production') {
      logger.debug('[initFromCookie]', { cookies, host, organizationId });
    }
    let maybeCookie = cookies[Identity.storageKey];
    if (!maybeCookie) {
      try {
        maybeCookie = localStorage[Identity.storageKey];
        if (process.env.NODE_ENV !== 'production' && maybeCookie) {
          logger.debug('Restored identity from localStorage', maybeCookie);
        }
      } catch (err) {
        // noop
      }
    } else if (process.env.NODE_ENV !== 'production') {
      logger.debug('Restored identity from cookie', maybeCookie);
    }

    const decoded = Identity.decodeIdentity(maybeCookie);
    if (decoded) {
      if (decoded.organizationId === organizationId) {
        if (process.env.NODE_ENV !== 'production') {
          logger.debug('Matching organizationId, setting identity', decoded);
        }
        return new Identity(decoded);
      }
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('Unmatching identity', { decoded, organizationId });
      }
    } else if (process.env.NODE_ENV !== 'production') {
      logger.debug('Could not parse identity');
    }

    const newIdentity = {
      expiresSeconds: yearFromNow(),
      host,
      organizationId,
      deviceId: '',
      sessionId: '',
    };

    if (process.env.NODE_ENV !== 'production') {
      logger.debug('Created new identity', newIdentity);
    }
    return new Identity(newIdentity);
  };

  private static decodeIdentity = (
    encoded: string | undefined
  ): RebrowseIdentity | undefined => {
    if (!encoded) {
      return undefined;
    }
    const [maybeIdentity, maybeExpiresSeconds] = encoded.split('/');
    const expiresSeconds = parseInt(maybeExpiresSeconds, 10);
    if (isNaN(expiresSeconds) || expiresSeconds < currentTimeSeconds()) {
      if (process.env.NODE_ENV !== 'production') {
        logger.debug('identity expired?', { expiresSeconds });
      }
      return undefined;
    }

    const identitySplit = maybeIdentity.split(/[#,]/);
    if (identitySplit.length !== 3) {
      return undefined;
    }

    const [deviceId, sessionId] = identitySplit[2].split(':');

    return {
      deviceId,
      sessionId,
      host: identitySplit[0],
      organizationId: identitySplit[1],
      expiresSeconds,
    };
  };

  private encode = (expiresSeconds: number) => {
    return `${this._cookie.host}#${this._cookie.organizationId}#${this._cookie.deviceId}:${this._cookie.sessionId}/${expiresSeconds}`;
  };

  public connect = (identity: PageVisitSessionLink) => {
    this._cookie.deviceId = identity.deviceId;
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
      logger.debug('Wrote identity', encoded);
    }
  };

  private setCookie = (encoded: string, expires: string) => {
    let cookie = `${Identity.storageKey}=${encoded}; domain=; Expires=${expires}; path=/; SameSite=Strict`;
    if (location.protocol === 'https:') {
      cookie += '; Secure';
    }
    document.cookie = cookie;
  };

  public deviceId = () => {
    return this._cookie.deviceId;
  };
}

export default Identity;
