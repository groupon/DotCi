import moment from 'moment';
export default function(dbObject){
  const projectParts = dbObject.projectName.split('/')
  dbObject.url =`${rootURL}/job/${projectParts[0]}/job/${projectParts[1]}/${dbObject.number}`
  dbObject.startTime = moment(dbObject.startTime).fromNow();    
  return dbObject;
}
