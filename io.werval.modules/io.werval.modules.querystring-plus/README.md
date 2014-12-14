# QueryString Plus Module

## Array and Hash QueryString Parameters

Here comes the magic.

WARNING: All magic comes at a price, this one comes with unsafety. Make sure you really need this and understand what is
implied.

Some frameworks allows using special syntax to pass hashes and arrays into a query string.
The most well known of this kind must be
[Ruby on Rails](http://guides.rubyonrails.org/action_controller_overview.html#hash-and-array-parameters).

Werval implement support for similar behaviour. It is controlled by the `werval.http.query-string.array-parameters.*`
configuration properties.

Here are the default values:

    werval.http.query-string.array-n-hash.enabled = no
    werval.http.query-string.array-n-hash.multi-valued-policy = single

Valid values for `multi-valued-policy` are `last`, `first`, and `single`.

In short: `foo[]=bar&foo=bazar&foo[0]=cathedral`.

With the former example you'll get `foo = [ "cathedral", "bar", "bazar" ]`.

    foo=bar&foo[]=bazar&foo[2]=awesome&foo[2]=tricky&foo[0]=cathedral
    # LAST
    foo = [ "cathedral", "bar", "tricky", "bazar ]
    # FIRST
    foo = [ "cathedral", "bar", "awesome", "bazar ]
    # SINGLE
    BAD REQUEST

