FROM node:15.9.0-alpine as build

WORKDIR /src

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/browser/tracking/package.json /src/frontend/browser/tracking/package.json
COPY frontend/shared/types/package.json /src/frontend/shared/types/package.json
RUN yarn install --frozen-lockfile

COPY frontend/browser/tracking/ /src/frontend/browser/tracking/
COPY frontend/shared/types/ /src/frontend/shared/types/

RUN yarn workspace @rebrowse/types build
RUN yarn workspace @rebrowse/browser-tracking build

FROM node:15.9.0-alpine

WORKDIR /usr/app

ENV NODE_ENV production
RUN yarn global add serve

COPY --from=build /src/frontend/browser/tracking/dist /usr/app/dist

CMD ["serve", "dist", "-l", "80", "--cors"]
