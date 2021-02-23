### BASE ###
FROM node:15-alpine as base

WORKDIR /src

### BUILDER ###
FROM base AS builder

# Install production dependencies
COPY package.json yarn.lock tsconfig.json lerna.json ./
COPY frontend/shared/sdk/package.json ./frontend/shared/sdk/package.json
COPY frontend/shared/service-proxy/package.json ./frontend/shared/service-proxy/package.json
COPY frontend/homepage/package.json ./frontend/homepage/package.json

RUN yarn install --production --frozen-lockfile --prefer-offline --ignore-scripts
RUN cp -RL ./node_modules/ /tmp/node_modules/

# Copy source files
COPY frontend/homepage/ ./frontend/homepage/
COPY frontend/shared/ ./frontend/shared/

# Install all dependencies
RUN yarn install --frozen-lockfile --prefer-offline

# Build
RUN yarn frontend:homepage:build

### RUNNER ###
FROM base

WORKDIR /usr/app

# Copy runtime dependencies
COPY --from=builder /tmp/node_modules/ ./node_modules/

COPY --from=builder /src/frontend/homepage/.next ./.next
COPY --from=builder /src/frontend/homepage/public ./public

COPY --from=builder /src/frontend/shared/sdk/package.json ./node_modules/@rebrowse/sdk/package.json
COPY --from=builder /src/frontend/shared/sdk/dist ./node_modules/@rebrowse/sdk/dist

USER node
ENV NODE_ENV production

EXPOSE 3000

CMD ["node", "node_modules/next/dist/bin/next", "start"]
