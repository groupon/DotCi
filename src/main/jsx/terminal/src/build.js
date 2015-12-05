var request = require('request');
import blessed from 'blessed';
import buildColor from './buildColors.js'
function buildHeader(serverUrl,onBack,axisList,screen,logWidget){
  const header = blessed.listbar({
    height: 'shrink',// : 3,
    mouse: true,
    width: '100%',
    keys: true,
    autoCommandKeys: true,
    border: 'line',
    vi: true,
    style: {
      // bg: 'green',
      item: {
        // bg: 'red',
        hover: {
          bg: 'blue'
        },
        focus: {
          bg: 'blue'
        }
      },
      selected: {
        bg: 'blue'
      }
    }
  });
  const commands= {
    '<---Back': {
      keys: ['b'],
      callback: onBack,
    }
  }
  axisList.forEach( axis =>{
    commands[buildColor(axis.result)((axis['script']))] = { callback: ()=>{
      loadLog(serverUrl+'/'+axis.url,onBack,logWidget,screen);
    }}
  })
  header.setItems(commands);
  return header;
}
function logWidget(parent){
  const log = blessed.log({
    scrollable: true,
    parent,
    top: 2,
    alwaysScroll: true,
    tags: true,
    keys:true,
    vi: true,
    mouse: true,
    border: {
      type: 'line'
    },
    style: {
      fg: 'green',
      bg: 'black',
      border: {
        fg: 'blue'
      },
      scrollbar: {
        bg: 'blue'
      }
    }
  });
  log.focus();
  return log;
}
function loadLog(url,onBack,logWidget,screen){
  request(
    {
      uri:url+'/logTail',
    }, function (error, response, body) {
      if (!error && response.statusCode == 200) {
        logWidget.setContent(body);
        // screen.append(buildScreen);
        screen.render();
      }else{
        console.error(error);
      }
    })
}
export default  function(serverUrl,baseUrl,screen,buildNumber,axisList,onBack){
  screen.key(['C-b'], function(ch, key) {
    onBack();
  });
  const log = logWidget(screen);
  const buildHeaderWidget = buildHeader(serverUrl,onBack,axisList,screen,log);
  screen.append(buildHeaderWidget);
  screen.render();

  const loading = blessed.loading({
    // screen,
    top: 3,
    alwaysScroll: true
  });
  // loading.load("loading..");
  // screen.append(loading);
  // screen.render();

  loadLog(baseUrl+buildNumber,onBack,log,screen);
}
