import PropTypes from 'prop-types';

const ChildrenPropTypes = PropTypes.oneOfType([
  PropTypes.string,
  PropTypes.number,
  PropTypes.array,
  PropTypes.element,
]);

export { ChildrenPropTypes };
