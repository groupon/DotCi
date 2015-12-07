import React from 'react';
import page from 'page';
export default React.createClass({
  render(){
    if (this.props.disabled) {
      return <span className={this.props.className}>
        {this.props.children}
      </span>
    }
    return <a className={this.props.className} href="#" onClick={this._onClick}>
      {this.props.children}
    </a>
  },
  _onClick(e){
    e.preventDefault();
    page(this.props.href+'')
  }

});
