export default function (onCallBack){
  const action = function(data){
    onCallBack(data,action.onAction);
  }
  return action;
}
