function styles(theme) {
  return {
    reactGridLayout: {
      background: 'gray',
      minHeight: theme.spacing(75),
      '& .react-grid-item': {
        background: '#f0f8ff',
      },
    },
  };
}

export default styles;
