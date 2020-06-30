import {renderHook, act} from "@testing-library/react-hooks";
import useModal from "../useModal";

describe('Use Modal hook', () => {

  it('should return isOpen by default false',  () => {
    const {result} = renderHook(() => useModal());

    expect(result.current.isOpen).toEqual(false);
  });

  it('should change isOpen state to true to open modal',  () => {
    const {result} = renderHook(() => useModal());

    act(()=>{
      result.current.openModal();
    })

    expect(result.current.isOpen).toEqual(true);
  });

  it('should change isOpen state to false to close modal',  () => {
    const {result} = renderHook(() => useModal());

    act(()=>{
      result.current.openModal();
      result.current.closeModal();
    })

    expect(result.current.isOpen).toEqual(false);
  });
})