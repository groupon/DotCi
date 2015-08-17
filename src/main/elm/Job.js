import Job from './Job.elm'
window.onload = function(){
  Job.embed(Job.Job, document.getElementById('content'), {}); 
}
