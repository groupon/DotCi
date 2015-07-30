import React from "react";
export default {
  componentWillUnmount() {
    this.clearAutoRefresh();
  },
  setRefreshTimer(refreshHelper){
    if(!this.refreshTimer){
    }
    // this.refreshTimer = setInterval(refreshHelper, 5000);
  },
  clearAutoRefresh(){
    clearInterval(this.refreshTimer);
  }
}
