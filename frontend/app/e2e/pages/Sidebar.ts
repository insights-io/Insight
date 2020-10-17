import { queryByText, within } from '@testing-library/testcafe';
import { Selector } from 'testcafe';

import {
  MEMBERS_SECTION,
  ORGANIZATION_SETTINGS_SECTION,
  USAGE_AND_BILLING_SECTION,
  ACCOUNT_SETTINGS_SECTION,
  AUTH_TOKENS_SECTION,
  SIGN_OUT_SECTION,
} from '../../src/shared/constants/copy';

class Sidebar {
  private readonly container = Selector('nav.sidebar');

  private readonly topList = this.container.find('ul').nth(0);
  private readonly bottomList = this.container.find('ul').nth(1);

  public readonly avatarRoot = this.container.find(
    'div[data-baseweb="avatar"]'
  );

  public readonly homeItem = this.topList
    .find('a')
    .withAttribute('href', '/')
    .child();

  public readonly sessionsItem = this.topList
    .find('a')
    .withAttribute('href', '/sessions')
    .child();

  public readonly toggleItem = this.bottomList
    .find('svg')
    .withAttribute('id', 'sidebar--togle')
    .parent();

  public readonly toggleExpandTooltip = queryByText('Expand');
  public readonly toggleCollapseTooltip = queryByText('Collapse');

  private readonly bannerMenu = Selector('div.banner--menu');
  private readonly bannerMenuOrganizationCard = this.bannerMenu
    .find('div.banner--card')
    .nth(0);

  private readonly bannerMenuUserCard = this.bannerMenu
    .find('div.banner--card')
    .nth(1);

  private readonly withinBannerMenu = within(this.bannerMenu);

  public readonly banner = {
    trigger: this.topList.find('li').nth(0).child(),
    menu: {
      organization: {
        cardTitle: this.bannerMenuOrganizationCard.child(1).child(0).child(0),
        cardSubtitle: this.bannerMenuOrganizationCard
          .child(1)
          .child(0)
          .child(1),
        settings: this.withinBannerMenu.queryByText(
          ORGANIZATION_SETTINGS_SECTION
        ),
        members: this.withinBannerMenu.queryByText(MEMBERS_SECTION),
        usageAndBilling: this.withinBannerMenu.queryByText(
          USAGE_AND_BILLING_SECTION
        ),
      },

      account: {
        cardTitle: this.bannerMenuUserCard.child(1).child(0).child(0),
        cardSubtitle: this.bannerMenuUserCard.child(1).child(0).child(1),
        settings: this.withinBannerMenu.queryByText(ACCOUNT_SETTINGS_SECTION),
        authTokens: this.withinBannerMenu.queryByText(AUTH_TOKENS_SECTION),
        signOut: this.withinBannerMenu.queryByText(SIGN_OUT_SECTION),
      },
    },
  };

  public signOut = (t: TestController) => {
    return t.click(this.banner.trigger).click(this.banner.menu.account.signOut);
  };
}

export default new Sidebar();
