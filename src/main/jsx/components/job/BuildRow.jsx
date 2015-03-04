
import React from 'react';
var BuildRow = React.createClass({
    render(){
        return (
            <a className ={"build-row-"+this.props.result}  href={this.props.number+''} >
               <span className="flex-row">
               {this._avatar()}
               {this._commitInfo()}
                {this._buildDuration()}
              </span>
            </a>
        );
    },
    _commitInfo(){
      return <span className="commit-info">
        <b>{this.props.commit.message}
        <button type="button" className="btn btn-link" >
          {this.props.commit.shortSha} <i className="fa fa-external-link-square"></i>
        </button>
        </b>
        <span className="flex-row">
        <small>{this.props.commit.committerName}</small>
      </span>
      </span>;
    },
    _avatar(){
      return <img  alt={this.props.commit.emailDigest}  src={"https://secure.gravatar.com/avatar/"+this.props.commit.emailDigest+".png?r=PG&s=46"}/>;
    },
    _buildDuration(){
      return( <span className="build-duration">
        <span ><small><i className="fa fa-clock-o"></i> Duration:</small> {this.props.duration} </span>
        <span ><small><i className="fa fa-clock-o"></i> Started:</small> {this.props.displayTime} </span>
      </span>
            );
    }
});
export default BuildRow;
