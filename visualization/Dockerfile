FROM node:12.19.0-alpine
COPY package*.json ./
RUN npm install
RUN mkdir visualisation
COPY . ./visualisation
RUN cd visualisation/frontend && yarn install
RUN cd visualisation/backend && yarn install