import dialog_polyfill from './../../vendor/dialog-polyfill/dialog-polyfill.js'
import dialog_polyfill_css from './../../vendor/dialog-polyfill/dialog-polyfill.css'
import React from "react";
require('./dialog.less');
export default React.createClass({
  render(){
    return (
      <dialog ref ="dialog">
          <div id="dialogClose" onClick={this._onClose} className="ui icon button">
            <i className="fa fa-times-circle"></i>
          </div>
        <div>
          <div className="ui top block header">
              {this.props.title}
            </div>
           {this.props.childern}
        </div>
      </dialog>
    );
  },
  open(){
    this.refs.dialog.getDOMNode().show();
  },
  _onClose(){
    this.refs.dialog.getDOMNode().close();
  }

})
