module.exports = {
  env: {
    node: true,
    jest: true,
  },
  extends: ['airbnb', 'prettier'],
  parserOptions: {
    ecmaVersion: 11,
  },
  overrides: [
    {
      files: ['*.test.js'],
      rules: {
        'arrow-body-style': 'warn',
      },
    },
  ],
  rules: {
    'max-len': [
      'warn',
      {
        code: 120,
      },
    ],
    'no-use-before-define': ['off'],
    'func-names': ['off'],
  },
};
