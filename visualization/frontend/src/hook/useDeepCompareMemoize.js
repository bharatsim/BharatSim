import { useRef } from 'react';
import equal from 'fast-deep-equal/es6/react';

function deepCompareEquals(value1, value2) {
  return equal(value1, value2);
}

function useDeepCompareMemoize(value) {
  const ref = useRef();

  if (!deepCompareEquals(value, ref.current)) {
    ref.current = value;
  }

  return ref.current;
}

export default useDeepCompareMemoize;
