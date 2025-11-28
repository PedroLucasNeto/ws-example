# Frontend development Dockerfile - runs `npm run dev`
FROM node:20-alpine
WORKDIR /app

# Install dependencies (cache package.json separately)
COPY package*.json ./
COPY package-lock*.json ./
RUN npm i

# Copy app source (can be overridden by a bind mount in docker-compose for local dev)
COPY . .

EXPOSE 5173
CMD ["npm", "run", "dev"]
