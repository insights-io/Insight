FROM node:14-alpine as build

WORKDIR /src

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/browser/tracking/package.json /src/frontend/browser/tracking/package.json
COPY frontend/shared/types/package.json /src/frontend/shared/types/package.json
RUN yarn install --frozen-lockfile

COPY frontend/browser/tracking/ /src/frontend/browser/tracking/
COPY frontend/shared/types/ /src/frontend/shared/types/

RUN yarn workspace @insight/types build
RUN yarn workspace @insight/browser-tracking build
RUN yarn install --frozen-lockfile --production --ignore-scripts --prefer-offline

FROM node:14-alpine

WORKDIR /usr/app

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/app/node_modules
COPY --from=build /src/frontend/browser/tracking/package.json /usr/app/package.json
COPY --from=build /src/frontend/browser/tracking/dist /usr/app/dist

CMD ["yarn", "serve"]
