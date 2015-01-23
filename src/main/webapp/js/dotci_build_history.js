var updateDotCiBuildHistory = function() {
     var firstBuildNumber=jQuery('#dotCiBuildHistory').attr('data-first-build-number');
     var lastBuildNumber=jQuery('#dotCiBuildHistory').attr('data-last-build-number');
    jQuery.ajax({
     url: "buildHistory/ajax?firstBuildNumber="+firstBuildNumber+"&lastBuildNumber="+lastBuildNumber
    })
    .done(function( data ) {
      jQuery(data).select("tr")
      .each(function( index ) {
       var row =jQuery( this )
       var  rowId = row.attr("id")
       jQuery("#"+rowId).replaceWith(row.prop('outerHTML'));
      });
    });
 };


    jQuery( document ).ready(function() {
     var interval = 1000 * 5 ; //Every 5 seconds
     setInterval(updateDotCiBuildHistory , interval);
    });