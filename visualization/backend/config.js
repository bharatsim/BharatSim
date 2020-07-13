module.exports = {
  PORT: process.env.PORT || 3005,
  DATABASE_URL:
    process.env.DATABASE_URL ||
    'mongodb://root:password@localhost:27017/bharatSim?authSource=admin&readPreference=primary&ssl=false',
};
