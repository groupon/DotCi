var request = require('request');
import build from './build.js';
import contrib from 'blessed-contrib';
import blessed from 'blessed';
function buildRow(build){
  let {number,displayTime,result,commit} = build;
  let {committerName,message,shortSha,branch} = commit;
  return [number+'', displayTime, result, branch, committerName,message,shortSha];
}
function header(title){
  return {
    obj: blessed.box,
    opts:{
      top: 'center',
      left: 'center',
      width: '100%',
      height: '100%',
      content: title,
      tags: true
    }
  }
}
function buildHistoryWidget(title,url,screen,inputBuilds,onBack){

  var grid = new contrib.grid({rows: 12, cols: 12, screen: screen})
  //grid.set(row, col, rowSpan, colSpan, obj, opts)
  let headerRow = header(title);
  var map = grid.set(0, 0, 2, 12, headerRow.obj, headerRow.opts);
  var parent = grid.set(2, 0, 12, 12, blessed.box, {});

  let builds =[];
  builds.push(['number','Ago', 'Result','Branch', 'Commiter','Message','Sha']);
  inputBuilds.forEach(build => builds.push( buildRow(build)));
  const table=  blessed.ListTable({
    parent,
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
    style: {
      header: {
        fg: 'blue',
        bold: true
      },
      cell: {
        fg: 'green',
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
