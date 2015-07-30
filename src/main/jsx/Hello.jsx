import React from "react";
import Elm from './Hello.elm';
export default React.createClass({
  componentDidUpdate(){
    this._renderElm();
  },
  componentDidMount(){
    this._renderElm();
  },
  _renderElm(){
    const div = this.refs.content.getDOMNode();
    debugger
    Elm.embed(Elm.Hello, div, {});
  },
  render(){
    return <div ref="content" />;
  }
})


