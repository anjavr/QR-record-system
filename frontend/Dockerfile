FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY cert.pem /etc/nginx/certs/cert.pem
COPY key.pem /etc/nginx/certs/key.pem
EXPOSE 80 443 8443