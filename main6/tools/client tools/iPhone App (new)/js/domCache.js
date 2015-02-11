/**
 * @author Administrator
 */
(function($, undefined) {
    $.fn.subpage = function(options) {
        $(document).bind(
            "changePage",
            function() {
                var forword = $.mobile.urlHistory.getNext();
                if (forword) {
                    var dataUrl = forword.url;
                    var forwordPage=$.mobile.pageContainer
                            .children(":jqmData(url='" + dataUrl + "')");
                    if(forwordPage){
                        forwordPage.remove();
                    }
                }
                $.mobile.urlHistory.clearForward();
            });
    };
//    $(document).bind("pagecreate create", function(e) {
//        $(":jqmData(role='page')", e.target).subpage();
//    });
})(jQuery);