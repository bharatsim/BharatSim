import React from 'react';
import Header from './header/Header';

function withAppLayout(WrappedComponent) {
  return function ComponentWithHeader() {
    return (
      <div>
        <Header />
        <WrappedComponent />
      </div>
    );
  };
}

export default withAppLayout;
