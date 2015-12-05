var colors = require('colors/safe');
export default function(result){
  switch(result){
    case 'SUCCESS': 
      return colors.green
    case 'FAILURE': 
      return colors.red
    case 'ABORTED': 
      return colors.grey
    default:
      return colors.yellow
  }
}
