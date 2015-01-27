var updateDotCiBuildHistory = function() {
     var buildHistoryTable = jQuery('#dotCiBuildHistory');
     var firstBuildNumber=buildHistoryTable.attr('data-first-build-number');
     var lastBuildNumber=buildHistoryTable.attr('data-last-build-number');
    jQuery.ajax({
     url: "buildHistory/ajax?firstBuildNumber="+firstBuildNumber+"&lastBuildNumber="+lastBuildNumber
    })
    .done(function( data ) {
      jQuery(data).select("tr")
      .each(function( index ) {
       var newRow =jQuery( this )
       var  rowId = newRow.attr("id")
       var currentRow = jQuery("#"+rowId)
       if(currentRow.length > 0){
        currentRow.replaceWith(newRow.prop('outerHTML'));
       }else{
         buildHistoryTable.prepend(newRow.prop('outerHTML'));
       }
      });
    });
 };


jQuery( document ).ready(function() {
     var interval = 1000 * 5 ; //Every 5 seconds
     setInterval(updateDotCiBuildHistory , interval);
});