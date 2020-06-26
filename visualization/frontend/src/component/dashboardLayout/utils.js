import Text from "../text/Text";
import React from "react";

export function createElement(element) {
  return (
    <div key={element.i} data-grid={element} data-testid={element.i}>
      <Text />
    </div>
  );
}

export function getInitialLayout() {
  return [0].map(function (i, key, list) {
    return {
      i: i.toString(),
      x: i * 2,
      y: 0,
      w: 2,
      h: 2,
    };
  });
}