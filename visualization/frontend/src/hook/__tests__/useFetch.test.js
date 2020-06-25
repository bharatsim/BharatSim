import {renderHook} from "@testing-library/react-hooks";
import {wait} from "@testing-library/react"

import useFetch from "../useFetch";
import {httpMethods} from "../../constants/httpMethods";
import fetch from "../../utils/fetch";

jest.mock('../../utils/fetch')

describe('Use fetch hook', () => {

  beforeEach(() => {
    fetch.mockReturnValue(Promise.resolve("Hello Welcome"))
  })

  it('should return fetch data for given url and method', async function () {
    const {result, waitForNextUpdate} = renderHook(() => useFetch('/test/api', httpMethods.GET));

    await waitForNextUpdate();

    expect(result.current).toEqual('Hello Welcome')
  });
})