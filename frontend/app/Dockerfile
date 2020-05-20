FROM node:14-alpine as build

COPY . /src
WORKDIR /src

RUN yarn install
RUN yarn workspace @insight/app build

FROM node:14-alpine

WORKDIR /usr/app

COPY --from=build /src/node_modules /usr/app/node_modules
COPY --from=build /src/frontend/app/package.json /usr/app/package.json
COPY --from=build /src/frontend/app/.next /usr/app/.next

ENV NODE_ENV production

CMD ["npm", "start"]
