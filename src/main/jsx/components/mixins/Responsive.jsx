import React from "react";
import enquire from 'enquire.js';
export default (breakPoints) => {
  return { 
    getInitialState(){
      return {};
    },
    componentDidMount(){
      if(!this._mediaHandlers){
        this._mediaHandlers = {};
      }
      for(let breakPoint in breakPoints){
        if (breakPoints.hasOwnProperty(breakPoint)) {
          this._mediaHandlers[breakPoint] = {
            match: ()=> this.setState({handler: breakPoints[breakPoint]}),
              setup: ()=> {
              if(enquire.queries && enquire.queries[breakPoint].matches()){
                this.setState({handler: breakPoints[breakPoint]});
              }
            }
          }

        }
      }

      for (let query in this._mediaHandlers) {
        if (this._mediaHandlers.hasOwnProperty(query)) {
          enquire.register(query, this._mediaHandlers[query]);
        }
      }
    },
    componentWillUnmount(){
      if(this._mediaHandlers){
        for (var query in this._mediaHandlers) {
          if (this._mediaHandlers.hasOwnProperty(query)) {
            enquire.unregister(query, this._mediaHandlers[query]);
          }
        }
      }
    },
    render(){
      // console.log(this.state.handler);
      return this.state.handler? this[this.state.handler](): <span/>;
    }
  }
};

