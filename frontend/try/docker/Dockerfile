### BASE ###
FROM node:14-alpine as base

WORKDIR /src

### BUILDER ###
FROM base AS builder

# Install production dependencies
COPY package.json yarn.lock tsconfig.json lerna.json ./
COPY frontend/shared/sdk/package.json ./frontend/shared/sdk/package.json
COPY frontend/shared/service-proxy/package.json ./frontend/shared/service-proxy/package.json
COPY frontend/shared/elements/package.json ./frontend/shared/elements/package.json
COPY frontend/try/package.json ./frontend/try/package.json

RUN yarn install --production --frozen-lockfile --prefer-offline
RUN cp -RL ./node_modules/ /tmp/node_modules/

# Copy source files
COPY frontend/try/ ./frontend/try/
COPY frontend/shared/ ./frontend/shared/

# Install all dependencies
RUN yarn install --frozen-lockfile --prefer-offline

# Build
RUN yarn frontend:try:build

### RUNNER ###
FROM base

WORKDIR /usr/app

# Copy runtime dependencies
COPY --from=builder /tmp/node_modules/ ./node_modules/

COPY --from=builder /src/frontend/try/.next ./.next

COPY --from=builder /src/frontend/shared/types/package.json ./node_modules/@rebrowse/types/package.json
COPY --from=builder /src/frontend/shared/types/dist ./node_modules/@rebrowse/types/dist

COPY --from=builder /src/frontend/shared/sdk/package.json ./node_modules/@rebrowse/sdk/package.json
COPY --from=builder /src/frontend/shared/sdk/dist ./node_modules/@rebrowse/sdk/dist

COPY --from=builder /src/frontend/shared/elements/package.json ./node_modules/@rebrowse/elements/package.json
COPY --from=builder /src/frontend/shared/elements/dist ./node_modules/@rebrowse/elements/dist

USER node
ENV NODE_ENV production

EXPOSE 3000

CMD ["node", "node_modules/next/dist/bin/next", "start"]
