const styles = (theme) => ({
  buttonRoot: {
    '& + &': {
      marginLeft: theme.spacing(1),
    },
  },
  reactGridLayout: {
    background: 'gray',
    minHeight: theme.spacing(75),
    '& .react-grid-item': {
      background: '#f0f8ff',
    },
  },
});

export default styles;
