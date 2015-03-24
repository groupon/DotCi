import React from "react";
require('./dialog.less');
export default React.createClass({
  render(){
    return (
      <section className="modal--show" ref="dialog"  role="dialog" aria-labelledby="modal-label" aria-hidden="true">
        <div className="modal-inner">
          <header id="modal-label"><h5 className="ui top block header">{this.props.title}</h5></header>
          <div className="modal-content">
            {this.props.children}
          </div>
          <div id="actionButtons">
            <div className="ui primary button" onClick={this._onSave}> Save </div>
            <div className="ui button" onClick={this._onClose}> Cancel </div>
          </div>
        </div>
        <a href="#" onClick={this._onClose} className="modal-close" title="Close this modal" data-close="Close" data-dismiss="modal">?</a>
      </section>
    );
  },
  open(){
    this.refs.dialog.getDOMNode().classList.add("is-active");
  },
  _onSave(){
    this.props.onSave();
    this._onClose();
  },
  _onClose(){
    this.refs.dialog.getDOMNode().classList.remove("is-active");
  }

})
