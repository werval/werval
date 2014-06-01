
$(document).ready(function() {
    // Highlight current menu item
    $("ul.navbar-nav li a").each(function(index,link){
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
