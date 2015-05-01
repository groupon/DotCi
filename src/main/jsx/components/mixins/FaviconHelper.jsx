export default {
  setFavicon(result){
    if(result){
      const icon = document.querySelector('[rel=icon]');
      const defaultUrl = resURL + 'favicons/'+result+'.ico'
      icon.setAttribute('href',defaultUrl);
    }
  },
  componentWillUnmount() {
    this.setFavicon('default');
  }
}
