var request = require('request');
import build from './build.js';
import contrib from 'blessed-contrib';
import blessed from 'blessed';
var colors = require('colors/safe');
function buildRow(build){
  let {number,displayTime,result,commit} = build;
  let {committerName,message,shortSha,branch} = commit;
  let color = colors.black;
  switch(result){
    case 'SUCCESS': 
      color = colors.green
    break;
    case 'FAILURE': 
      color = colors.red
    break;
    case 'ABORTED': 
      color = colors.grey
    break;
    default:
      color = colors.yellow
    break;
  }
  let cols = [displayTime, result, branch, committerName,message,shortSha].map( col => color(col));
  return [number + ''].concat(cols);
}
function buildHistoryWidget(title,url,screen,inputBuilds,onBack){
  let builds =[];
  builds.push(['number','Ago', 'Result','Branch', 'Commiter','Message','Sha']);
  inputBuilds.forEach(build => builds.push( buildRow(build)));
  const table=  blessed.ListTable({
    parent:screen,
    screen,
    width: '100%',
    height: '100%',
    border: {
      type: 'line',
      left: true,
      top: true,
      right: false,
      bottom: false
    },
    align: 'center',
    tags: true,
    keys:true,
    vi:true,
    mouse: true,
    noCellBorders: true,
    style: {
      header: {
        fg: 'blue',
        bold: true
      },
      cell: {
        fg: 'white',
        selected: {
          bg: 'blue'
        }
      }
    },
    rows:builds
  });
  table.on('select',item => {
    const number =item.parent.rows[item.parent.selected][0];
    build(url,screen,number,onBack);
  });
  table.focus();
}

export default function(title,url,screen,onBack){
  request(
    {
      uri: url+ 'api/json/',
      qs: {
        tree:  "appData[info[buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]]]",
        branchTab: 'All',
        count: 50
      }
    }, function (error, response, body) {
      if (!error && response.statusCode == 200) {
        var builds = JSON.parse(body)['appData']['info']['builds'];
        buildHistoryWidget(title,url,screen,builds,onBack);
        screen.render();
      }
    })

}
