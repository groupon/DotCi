import indexOf from 'ramda/src/indexOf';
export default function remove(element){
  return (array) => {
    const index = indexOf(element)(list);
    if (index > -1) {
      array.splice(index, 1);
    }
    return array;
  }
}
