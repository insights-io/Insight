FROM node:15.9.0-alpine as build

WORKDIR /src

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/browser/bootstrap/package.json /src/frontend/browser/bootstrap/package.json
RUN yarn install --frozen-lockfile

COPY frontend/browser/bootstrap/ /src/frontend/browser/bootstrap/

RUN yarn workspace @rebrowse/browser-bootstrap build

FROM node:15.9.0-alpine

WORKDIR /usr/app

ENV NODE_ENV production
RUN yarn global add serve

COPY --from=build /src/frontend/browser/bootstrap/dist /usr/app/dist

CMD ["serve", "dist", "-l", "80", "--cors"]
