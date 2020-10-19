import React from 'react';

import { render } from '@testing-library/react';
import LoaderOrError from '../LoaderOrError';

const DummyComponent = () => <div>Dummy Components</div>;

describe('<InlineLoader />', () => {
  it('should match snapshot with loader component if loading is in progress', () => {
    const { container } = render(
      <LoaderOrError loadingState="LOADING">
        <DummyComponent />
      </LoaderOrError>,
    );

    expect(container).toMatchSnapshot();
  });

  it('should match snapshot with error component if loading is failed', () => {
    const { container } = render(
      <LoaderOrError loadingState="ERROR">
        <DummyComponent />
      </LoaderOrError>,
    );

    expect(container).toMatchSnapshot();
  });

  it('should match snapshot with child component if loading is successful', () => {
    const { queryByText } = render(
      <LoaderOrError loadingState="SUCCESS">
        <DummyComponent />
      </LoaderOrError>,
    );

    expect(queryByText('Dummy Components')).not.toBeNull();
  });

  it('should return children if snackbar is true and loading is successful', () => {
    const { queryByText } = render(
      <LoaderOrError loadingState="SUCCESS" snackbar>
        <DummyComponent />
      </LoaderOrError>,
    );

    expect(queryByText('Dummy Components')).not.toBeNull();
  });

  it('should return children if snackbar is true and loading is failed', () => {
    const { queryByText } = render(
      <LoaderOrError loadingState="ERROR" snackbar>
        <DummyComponent />
      </LoaderOrError>,
    );

    expect(queryByText('Dummy Components')).not.toBeNull();
  });

  it('should return loader if snackbar is true and loading is in progress', () => {
    render(
      <LoaderOrError loadingState="LOADING" snackbar>
        <DummyComponent />
      </LoaderOrError>,
    );

    expect(document.getElementsByTagName('circle')).not.toBeNull();
  });
});
