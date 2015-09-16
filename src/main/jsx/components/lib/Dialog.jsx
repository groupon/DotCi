import React from 'react';
import Loading from  './../mixins/Loading.jsx';
export default React.createClass({
  getInitialState(){
    return {showing: false};
  },
  render(){
    return <paper-dialog  ref="ca-dialog"  entry-animation="scale-up-animation" exit-animation="fade-out-animation">
      <h2>{this.props.heading}</h2>
      <paper-dialog-scrollable>
        {(this.state.showing || !this.props.lazy)?this.props.children:this._loading()}
      </paper-dialog-scrollable>
      {this.props.noButtons? "": this._buttons()}
    </paper-dialog>;
  },
  _loading(){
    <Loading/>;
  },
  _buttons(){
    return <div className="buttons">
      <paper-button  dialog-dismiss>Cancel</paper-button>
      <paper-button  onClick={this._onClick} dialog-confirm>Accept</paper-button>
    </div>
  },
  show(){
    this._toggle(true);
  },
  hide(){
    this._toggle(false);
  },
  _toggle(showing){
    if(this.props.lazy){
      this.setState({showing});
    }
    const dialog = this.refs['ca-dialog'];
    dialog.toggle();
  },
  _onClick(e){
    this.props.onSave(e);
  }
});
