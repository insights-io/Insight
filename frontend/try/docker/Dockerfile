FROM node:14-alpine as build

WORKDIR /src

COPY . /src

RUN yarn install --frozen-lockfile
RUN yarn workspace @insight/try build
RUN yarn install --frozen-lockfile --production --ignore-scripts --prefer-offline

FROM node:14-alpine

WORKDIR /usr/try

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/try/node_modules

COPY --from=build /src/frontend/try/package.json /usr/try/package.json
COPY --from=build /src/frontend/try/.next /usr/try/.next

COPY --from=build /src/frontend/shared/sdk/package.json /usr/try/node_modules/@insight/sdk/package.json
COPY --from=build /src/frontend/shared/sdk/dist /usr/try/node_modules/@insight/sdk/dist
COPY --from=build /src/frontend/shared/elements/package.json /usr/try/node_modules/@insight/elements/package.json
COPY --from=build /src/frontend/shared/elements/dist /usr/try/node_modules/@insight/elements/dist

EXPOSE 3000

CMD ["npm", "start"]
