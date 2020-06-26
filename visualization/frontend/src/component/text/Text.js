import React from "react";
import {Typography} from "@material-ui/core";

import {url} from "../../utils/url";

import useFetch from "../../hook/useFetch";

const Text = () => {
  const text = useFetch(url.HELLO)
  return <><Typography variant="h6">{text}</Typography></>
}

export default Text;