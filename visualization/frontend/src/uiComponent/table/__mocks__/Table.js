import React from 'react';

export default (props) => {
  return (
    <div>
      Table
      <div>{JSON.stringify(props, null, 2)}</div>
    </div>
  );
};
