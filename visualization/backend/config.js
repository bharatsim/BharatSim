const DB_USER = process.env.DB_USER || 'root';
const DB_PASS = process.env.DB_PASS || 'password';
const DB_PORT = process.env.DB_PORT || 27017;
const DB_HOST = process.env.DB_HOST || 'localhost';

module.exports = {
  PORT: process.env.PORT || 3005,
  DATABASE_URL: `mongodb://${DB_USER}:${DB_PASS}@${DB_HOST}:${DB_PORT}/bharatSim?authSource=admin&readPreference=primary&ssl=false`,
};
