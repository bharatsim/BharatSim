import { act, renderHook } from "@testing-library/react-hooks";

import useFetchExecutor from "../useFetchExecuter";

describe('useFetchExecutor hook', () => {
  let api;
  beforeEach(() => {
    api = jest.fn().mockImplementation(async () => Promise.resolve('Hello NewUserHomeScreen'));
  });
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should return fetch data for given url', async () => {
    const { result } = renderHook(() => useFetchExecutor());
    let apiResponse;
    await act(async ()=>{
      apiResponse = await result.current.executeFetch(api)
    })

    expect(apiResponse).toEqual('Hello NewUserHomeScreen');

    expect(api).toHaveBeenCalledWith();
  });

  it('should set loading state to LOADING at start of api call', async () => {
    const { result } = renderHook(() => useFetchExecutor());

    act(()=>{
      result.current.executeFetch(api)
    })

    expect(result.current.loadingState).toEqual("LOADING");

    await act(async ()=>{})
  });

  it('should set loading state to SUCCESS after successful api call', async () => {
    const { result } = renderHook(() => useFetchExecutor());

    await act(async ()=>{
      await result.current.executeFetch(api)
    })

    expect(result.current.loadingState).toEqual("SUCCESS");
  });

  it('should set loading state to ERROR after failure of api call', async () => {
    api = jest.fn().mockRejectedValue('error');
    const { result } = renderHook(() => useFetchExecutor());

    await act(async ()=>{
      await result.current.executeFetch(api)
    })

    expect(result.current.loadingState).toEqual("ERROR");
  });

  it('should set loading state to SUCCESS as default state', async () => {
    const { result } = renderHook(() => useFetchExecutor());

    expect(result.current.loadingState).toEqual("SUCCESS");
  });

});
