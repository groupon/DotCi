export default {
  componentDidMount(){
    const caRefs = Object.getOwnPropertyNames(this.refs).filter( ref => ref.lastIndexOf('ca-',0) === 0)
    caRefs.forEach(ref => {
      const refNode = this.refs[ref];
      const domNode = refNode.getDOMNode();
      for(let attr in refNode.props.attrs){
        domNode.setAttribute(attr, refNode.props.attrs[attr])
      }
    })
  }
}
