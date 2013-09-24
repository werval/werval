
var list_breweries = function() {
    $.getJSON("/api/breweries", function(data) {
        $("#output").prepend(JSON.stringify(data));
    });
};

$(document).ready(function() {
    console.log("Beer Database UI");
    $("#main").append("<h1>Welcome to the Beer Database QiWeb Sample!</h1><pre id=\"output\"></pre>");
    list_breweries();
});
