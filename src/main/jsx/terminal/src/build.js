var request = require('request');
import blessed from 'blessed';
function buildWidget(onBack){
  const build = blessed.form({
    mouse: true,
    border: {
      type: 'line'
    },
    style: {
      fg: 'black',
      // bg: 'white',
      border: {
        fg: 'blue'
      },
    }
  });
  const backButton = blessed.button({
    parent: build,
    height: 1,
    width: 15,
    content: '<--- Back',
    align: 'center',
    bg: 'blue',
    hoverBg: 'green',
    mouse: true
  });
  backButton.on('press',onBack);
  return build;
}
function logWidget(parent){
  return blessed.log({
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
}

export default  function(url,screen,buildNumber,onBack){
  request(
    {
      uri:url+buildNumber+'/logTail',
    }, function (error, response, body) {
      if (!error && response.statusCode == 200) {
        const buildScreen = buildWidget(onBack);
        const logScreen = logWidget(buildScreen);
        logScreen.setContent(body);
        screen.append(buildScreen);
        screen.render();
      }
    })
}
