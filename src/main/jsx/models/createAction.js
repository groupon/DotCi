export default function (onCallBack){
  const action = function(...data){
    if(action.active){
      onCallBack(...data,action.onAction);
    }
  }
  return action;
}
