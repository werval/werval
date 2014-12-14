
$(document).ready(function() {
    // Highlight current menu item
    $("ul.navbar-nav li a").each(function(index, link) {
        var baseHref = link.href;
        if (baseHref.indexOf('/doc/') !== -1 && baseHref.lastIndexOf('.html') === baseHref.length - 5) {
            baseHref = baseHref.substring(0, baseHref.lastIndexOf('/'));
        }
        if (window.location.href.indexOf(baseHref) === 0) {
            $(link).parent().addClass("active");
        } else {
            $(link).parent().removeClass("active");
        }
    });
    // Highlight current ToC item
    $.fn.isOnScreen = function() {
        // Function from http://upshots.org/javascript/jquery-test-if-element-is-in-viewport-visible-on-screen
        var win = $(window);
        var viewport = {
            top: win.scrollTop(),
            left: win.scrollLeft()
        };
        viewport.right = viewport.left + win.width();
        viewport.bottom = viewport.top + win.height();
        var bounds = this.offset();
        bounds.right = bounds.left + this.outerWidth();
        bounds.bottom = bounds.top + this.outerHeight();
        return (!(viewport.right < bounds.left || viewport.left > bounds.right || viewport.bottom < bounds.top || viewport.top > bounds.bottom));
    };
    var $toc = $('#toc.toc2');
    if ($toc) {
        var headingIds = [];
        $toc.find('li > a').each(function(index, tocLink) {
            headingIds.push($(tocLink).attr('href').substring(1));
        });
        var $window = $(window);
        var deactivateAllTocItems = function() {
            $toc.find('li > a').each(function(index, tocLink) {
                $(tocLink).parent().removeClass('active');
            });
        };
        var highlightCurrentTocItem = function() {
            var viewportCenter = $window.scrollTop() + ($window.height() / 4);
            var nearestId = null;
            var nearestDistance = Number.MAX_VALUE;
            var headingIdsLength = headingIds.length;
            for (var idx = 0; idx < headingIdsLength; idx++) {
                var headingId = headingIds[idx];
                var distance = Math.abs($('#' + headingId).offset().top - viewportCenter);
                if (distance < nearestDistance) {
                    nearestId = headingId;
                    nearestDistance = distance;
                }
            }
            deactivateAllTocItems();
            $toc.find('li > a[href="#' + nearestId + '"]').each(function(index, tocLink) {
                var $tocLink = $(tocLink);
                $tocLink.parent().addClass('active');
                if (!$tocLink.isOnScreen()) {
                    $toc.scrollTop($tocLink.position().top - 20);
                }
            });
        };
        $window.scroll(highlightCurrentTocItem);
        $window.resize(highlightCurrentTocItem);
        highlightCurrentTocItem();
    }
    // Open absolute links to other domains in a new window/tab
    $("a").each(function(index, link) {
        var $link = $(link);
        if (($link.attr('href').indexOf('http:') === 0
                || $link.attr('href').indexOf('https:') === 0
                || $link.attr('href').indexOf('//') === 0
                ) && link.href.indexOf(window.location.protocol + '//' + window.location.host) !== 0) {
            $link.attr('target', '_blank');
        }
    });
});
