import React from 'react';
import Header from './header/Header';

function withHeader(WrappedComponent) {
  return function ComponentWithHeader() {
    return (
      <div>
        <Header />
        <WrappedComponent />
      </div>
    );
  };
}

export default withHeader;
