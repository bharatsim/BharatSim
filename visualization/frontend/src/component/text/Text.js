import React from "react";

import {url} from "../../utils/url";

import useFetch from "../../hook/useFetch";

const Text = () => {
  const text = useFetch(url.HELLO);
  return <div>{text}</div>
}

export default Text;