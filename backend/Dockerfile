FROM node:24-alpine
WORKDIR /app
COPY --chown=node:node package.json server.mjs plant-catalog.mjs plant-lookup.mjs assistant.mjs provider-fetch.mjs ./
RUN mkdir -p /app/data && chown node:node /app/data
USER node
EXPOSE 8787
CMD ["node", "server.mjs"]
