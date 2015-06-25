import React from 'react';
import CustomAttributes from './../mixins/CustomAttributes.jsx';
export default React.createClass({
  mixins: [CustomAttributes],
  render(){
    return <paper-dialog  ref="ca-dialog"  attrs={{heading:this.props.heading}} onClick={this._onClick} >
      {this.props.children}
      <div className="buttons">
        <paper-button ref="ca-1" attrs={{"dialog-dismiss": true}}>Cancel</paper-button>
        <paper-button  ref="ca-2" attrs={{ "dialog-confirm": true}}>Accept</paper-button>
      </div>
    </paper-dialog>;
  },
  show(){
    const dialog = this.refs['ca-dialog'];
    dialog.getDOMNode().toggle();
  },
  _onClick(e){
    if(e.target.parentElement && e.target.parentElement.getAttribute('dialog-confirm')){
      this.props.onSave(e);
    }
  }
});
