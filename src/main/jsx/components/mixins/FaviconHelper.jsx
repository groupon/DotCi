export default {
  setFavicon(result){
    const icon = document.querySelector('[rel=icon]');
    const defaultUrl = resURL + 'favicons/'+result+'.ico'
    icon.setAttribute('href',defaultUrl);
  },
  componentWillUnmount() {
    this.setFavicon('default');
  }
}
