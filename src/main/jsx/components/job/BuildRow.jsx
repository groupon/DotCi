
import React from 'react';
require('./build_row.less');
var BuildRow = React.createClass({
    render(){
        return (
            <div className ={"build-row-"+this.props.result}  href={this.props.number+''} >
               {this._avatar()}
               {this._commitInfo()}
                {this._buildDuration()}
            </div>
        );
    },
    _commitInfo(){
      return(<span className="commit-info">
        <span className="commit-info-column">
        <b>{this.props.commit.message}</b>
        <a className="commit-url"  href={this.props.commit.commitUrl} >
          {this.props.commit.shortSha} <i className="icon fa fa-external-link-square"></i>
        </a>
      </span>
      <span className="commit-info-column">
        <small>{this.props.commit.committerName}</small>
        <span>{this.props.commit.branch}</span>
        <a href={this.props.number+"/console"} className="compact mini ui black  button right labeled icon ">
          <i className="fa fa-arrow-circle-right icon"></i>
          Console
        </a>
      </span>
      </span>);
    },
    _avatar(){
      return <img  className="ui avatar image" alt={this.props.commit.emailDigest}  src={"https://secure.gravatar.com/avatar/"+this.props.commit.emailDigest+".png"}/>;
    },
    _buildDuration(){
      return( <span className="build-duration">
        <span ><small><i className="fa fa-clock-o"></i>Duration:</small>{this.props.duration}</span>
        <span ><small><i className="fa fa-clock-o"></i>Started:</small>{this.props.displayTime}</span>
      </span>
            );
    }
});
export default BuildRow;
