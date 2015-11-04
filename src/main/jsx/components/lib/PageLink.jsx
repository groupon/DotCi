import React from 'react';
import page from 'page';
export default React.createClass({
  render(){
    return    <a className={this.props.className} href="#" onClick={this._onClick}>
      {this.props.children}
    </a>
  },
  _onClick(e){
    debugger
    e.preventDefault();
    page(this.props.href+'')
  }

});
