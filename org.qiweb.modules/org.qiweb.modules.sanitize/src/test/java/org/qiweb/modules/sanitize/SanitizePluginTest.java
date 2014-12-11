/*
 * Copyright (c) 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.modules.sanitize;

import io.werval.test.QiWebRule;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static io.werval.util.Strings.EMPTY;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.qiweb.modules.sanitize.Sanitize.ANTISAMY_ANYTHINGGOES;

/**
 * Sanitize Plugin Test.
 */
public class SanitizePluginTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();

    private static Sanitize sanitize;

    @BeforeClass
    public static void setupSanitizer()
    {
        sanitize = QIWEB.application().plugin( Sanitize.class );
    }

    @Test
    public void sanitizeHtml()
    {
        // Use a policy that allow <a/> and <img/> tags for the test to be relevant
        Sanitize sanitize = SanitizePluginTest.sanitize.withPolicy( ANTISAMY_ANYTHINGGOES );

        // XSS Filter Evasion Techniques
        // See https://www.owasp.org/index.php/XSS_Filter_Evasion_Cheat_Sheet
        assertThat(
            sanitize.html(
                "';alert(String.fromCharCode(88,83,83))//';alert(String.fromCharCode(88,83,83))//\";"
                + "alert(String.fromCharCode(88,83,83))//\";alert(String.fromCharCode(88,83,83))//--"
                + "></SCRIPT>\">'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>"
            ).toLowerCase(),
            allOf(
                not( containsString( "script" ) ),
                containsString( "alert" )
            )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\"javascript:alert('XSS');\">" ).toLowerCase(),
            noneOf(
                containsString( "javascript" ),
                containsString( "img" )
            )
        );
        assertThat(
            sanitize.html( "<IMG SRC=JaVaScRiPt:alert('XSS')>" ).toLowerCase(),
            noneOf(
                containsString( "javascript" ),
                containsString( "img" )
            )
        );
        assertThat(
            sanitize.html( "<IMG SRC=`javascript:alert(\"RSnake says, 'XSS'\")`>" ).toLowerCase(),
            noneOf(
                containsString( "javascript" ),
                containsString( "img" )
            )
        );
        assertThat(
            sanitize.html( "<a onmouseover=\"alert(document.cookie)\">xss link</a>" ).toLowerCase(),
            allOf(
                not( containsString( "cookie" ) ),
                containsString( "<a" )
            )
        );
        assertThat(
            sanitize.html( "<a onmouseover=alert(document.cookie)>xss link</a>" ).toLowerCase(),
            allOf(
                not( containsString( "cookie" ) ),
                containsString( "<a" )
            )
        );
        assertThat(
            sanitize.html( "<IMG \"\"\"><SCRIPT>alert(\"XSS\")</SCRIPT>\">" ).toLowerCase(),
            allOf(
                not( containsString( "script" ) ),
                containsString( "<img" )
            )
        );
        assertThat(
            sanitize.html( "<IMG SRC=# onmouseover=\"alert('xss')\">" ).toLowerCase(),
            allOf(
                not( containsString( "xss" ) ),
                containsString( "<img" )
            )
        );
        assertThat(
            sanitize.html( "<IMG SRC= onmouseover=\"alert('xss')\">" ).toLowerCase(),
            noneOf(
                containsString( "xss" ),
                containsString( "img" )
            )
        );
        assertThat(
            sanitize.html( "<IMG onmouseover=\"alert('xss')\">" ).toLowerCase(),
            allOf(
                not( containsString( "xss" ) ),
                containsString( "img" )
            )
        );
        assertThat(
            sanitize.html( "<IMG SRC=/ onerror=\"alert(String.fromCharCode(88,83,83))\"></img>" ).toLowerCase(),
            allOf(
                not( containsString( "onerror" ) ),
                containsString( "img" )
            )
        );
        assertThat(
            sanitize.html(
                "<IMG SRC=&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;&#116;&#58;&#97;"
                + "&#108;&#101;&#114;&#116;&#40;&#39;&#88;&#83;&#83;&#39;&#41;>"
            ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html(
                "<IMG SRC=&#0000106&#0000097&#0000118&#0000097&#0000115&#0000099&#0000114&#0000105&#0000112&#0000116"
                + "&#0000058&#0000097&#0000108&#0000101&#0000114&#0000116&#0000040&#0000039&#0000088&#0000083"
                + "&#0000083&#0000039&#0000041>"
            ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html(
                "<IMG SRC=&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&"
                + "#x27&#x58&#x53&#x53&#x27&#x29>"
            ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\"jav	ascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\"jav&#x09;ascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\"jav&#x0A;ascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\"jav&#x0D;ascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\" &#14;  javascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<SCRIPT/XSS SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>" ).toLowerCase(),
            not( containsString( "script" ) )
        );
        assertThat(
            sanitize.html( "<BODY onload!#$%&()*~+-_.,:;?@[/|\\]^`=alert(\"XSS\")>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<SCRIPT/SRC=\"http://ha.ckers.org/xss.js\"></SCRIPT>" ).toLowerCase(),
            not( containsString( "script" ) )
        );
        assertThat(
            sanitize.html( "<<SCRIPT>alert(\"XSS\");//<</SCRIPT>" ).toLowerCase(),
            not( containsString( "script" ) )
        );
        assertThat(
            sanitize.html( "<SCRIPT SRC=http://ha.ckers.org/xss.js?< B >" ).toLowerCase(),
            not( containsString( "script" ) )
        );
        assertThat(
            sanitize.html( "<SCRIPT SRC=//ha.ckers.org/.j>" ).toLowerCase(),
            not( containsString( "script" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC=\"javascript:alert('XSS')\"" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<iframe src=http://ha.ckers.org/scriptlet.html <" ).toLowerCase(),
            not( containsString( "iframe" ) )
        );
        assertThat(
            sanitize.html( "</TITLE><SCRIPT>alert(\"XSS\");</SCRIPT>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<INPUT TYPE=\"IMAGE\" SRC=\"javascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<BODY BACKGROUND=\"javascript:alert('XSS')\">" ).toLowerCase(),
            not( containsString( "background" ) )
        );
        assertThat(
            sanitize.html( "<IMG DYNSRC=\"javascript:alert('XSS')\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<IMG LOWSRC=\"javascript:alert('XSS')\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html(
                "<STYLE>li {list-style-image: url(\"javascript:alert('XSS')\");}</STYLE><UL><LI>XSS</br>"
            ).toLowerCase(),
            not( containsString( "alert" ) )
        );
        assertThat(
            sanitize.html( "<IMG SRC='vbscript:msgbox(\"XSS\")'>" ).toLowerCase(),
            not( containsString( "img" ) )
        );
        assertThat(
            sanitize.html( "<BODY ONLOAD=alert('XSS')>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<BGSOUND SRC=\"javascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<BR SIZE=\"&{alert('XSS')}\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<LINK REL=\"stylesheet\" HREF=\"javascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<META HTTP-EQUIV=\"refresh\" CONTENT=\"0;url=javascript:alert('XSS');\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html(
                "<META HTTP-EQUIV=\"refresh\" "
                + "CONTENT=\"0;url=data:text/html base64,PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4K\">"
            ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html(
                "<META HTTP-EQUIV=\"refresh\" CONTENT=\"0; URL=http://;URL=javascript:alert('XSS');\">"
            ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<IFRAME SRC=\"javascript:alert('XSS');\"></IFRAME>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<IFRAME SRC=# onmouseover=\"alert(document.cookie)\"></IFRAME>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<FRAMESET><FRAME SRC=\"javascript:alert('XSS');\"></FRAMESET>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<TABLE BACKGROUND=\"javascript:alert('XSS')\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<TABLE><TD BACKGROUND=\"javascript:alert('XSS')\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<DIV STYLE=\"background-image: url(javascript:alert('XSS'))\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html(
                "<DIV STYLE=\"background-image:\\0075\\0072\\006C\\0028'\\006a\\0061\\0076\\0061\\0073"
                + "\\0063\\0072\\0069\\0070\\0074\\003a\\0061\\006c\\0065\\0072\\0074\\0028.1027\\0058.1053"
                + "\\0053\\0027\\0029'\\0029\">"
            ).toLowerCase(),
            not( containsString( "background-image" ) )
        );
        assertThat(
            sanitize.html( "<DIV STYLE=\"background-image: url(&#1;javascript:alert('XSS'))\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<DIV STYLE=\"width: expression(alert('XSS'));\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html(
                "<!--[if gte IE 4]>\n"
                + "<SCRIPT>alert('XSS');</SCRIPT>\n"
                + "<![endif]-->"
            ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "<BASE HREF=\"javascript:alert('XSS');//\">" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html(
                "<OBJECT TYPE=\"text/x-scriptlet\" DATA=\"http://ha.ckers.org/scriptlet.html\"></OBJECT>"
            ).toLowerCase(),
            not( containsString( "scriptlet" ) )
        );
        assertThat(
            sanitize.html(
                "<EMBED SRC=\"data:image/svg+xml;base64,PHN2ZyB4bWxuczpzdmc9Imh0dH A6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4b"
                + "Wxucz0iaHR0cDovL3d3dy53My5vcmcv MjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hs aW"
                + "5rIiB2ZXJzaW9uPSIxLjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxOTQiIGhlaWdodD0iMjAw IiBpZD0ieHNzIj48c2NyaXB0IHR5"
                + "cGU9InRleHQvZWNtYXNjcmlwdCI+YWxlcnQoIlh TUyIpOzwvc2NyaXB0Pjwvc3ZnPg==\" "
                + "type=\"image/svg+xml\" AllowScriptAccess=\"always\"></EMBED>"
            ).toLowerCase(),
            not( containsString( "AllowScriptAccess" ) )
        );

        // mXSS - Mutation XSS - Relies on the browser dom auto-mutation if used as innerHtml
        // See https://www.youtube.com/watch?v=Ld6prXQPnf4
        assertThat(
            sanitize.html( "<IMG SRC=\"x\" CLASS=\"``onerror=alert('XSS')\"/>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "0<aside xmlns=\"x><img src=x onerror=alert('XSS')\">123</aside>" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.html( "0<aside xmlns=\"foo:img src=x onerror=alert('XSS')>\">123" ).toLowerCase(),
            not( containsString( "xss" ) )
        );

        // Browser downgrade
        // See https://www.youtube.com/watch?v=Ld6prXQPnf4
        assertThat(
            sanitize.html( "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=IE5\"/>" ).toLowerCase(),
            not( containsString( "meta" ) )
        );

        // CSS escapes harmful if used as innerHtml
        // See https://www.youtube.com/watch?v=Ld6prXQPnf4
        assertThat(
            sanitize.html(
                "<p style=\"font-family:'foo\\27\\3b color\\3a expression\\28 alert('XSS'))/* bar'\">hello</p>"
            ).toLowerCase(),
            equalTo( "<p style=\"\">hello</p>" )
        );
        assertThat(
            sanitize.html(
                "<p style=\"fon\\22\\3e\\3cimg\\20src\\3dx\\20onerror\\3d alert\\28 1\\29\\3et-family:'foobar'\">h</p>"
            ).toLowerCase(),
            equalTo( "<p style=\"\">h</p>" )
        );
        assertThat(
            sanitize.html( "<p style=\"filter: 'expression(alert(1))'\"/>" ).toLowerCase(),
            equalTo( "<p style=\"\"></p>" )
        );
        assertThat(
            sanitize.html(
                "<p style=\"font-family: 'foo\\27\\3b color\\3a expression\\28 alert('XSS'))/* ,bar'\"/>"
            ).toLowerCase(),
            equalTo( "<p style=\"\"></p>" )
        );

        // HTML Entities
        // See https://www.youtube.com/watch?v=Ld6prXQPnf4
        assertThat(
            sanitize.html( "<svg><style>&ltimg src=x onerror=alert(1)&gt</svg>" ).toLowerCase(),
            equalTo( EMPTY )
        );

    }

    @Test
    public void sanitizeCss()
    {
        // Use a policy that allow styles for the test to be relevant
        Sanitize sanitize = SanitizePluginTest.sanitize.withPolicy( ANTISAMY_ANYTHINGGOES );

        // XSS Filter Evasion Techniques
        // See https://www.owasp.org/index.php/XSS_Filter_Evasion_Cheat_Sheet
        assertThat(
            sanitize.css( "li {list-style-image: url(\"javascript:alert('XSS')\");}" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.css(
                "background-image:\\0075\\0072\\006C\\0028'\\006a\\0061\\0076\\0061\\0073"
                + "\\0063\\0072\\0069\\0070\\0074\\003a\\0061\\006c\\0065\\0072\\0074\\0028.1027\\0058.1053"
                + "\\0053\\0027\\0029'\\0029"
            ).toLowerCase(),
            not( containsString( "background-image" ) )
        );
        assertThat(
            sanitize.css( "background-image: url(&#1;javascript:alert('XSS'))" ).toLowerCase(),
            not( containsString( "xss" ) )
        );

        // CSS escapes harmful if used as innerHtml
        // See https://www.youtube.com/watch?v=Ld6prXQPnf4
        assertThat(
            sanitize.css( "font-family:'foo\\27\\3b color\\3a expression\\28 alert('XSS'))/* bar'" ).toLowerCase(),
            not( containsString( "xss" ) )
        );
        assertThat(
            sanitize.css(
                "fon\\22\\3e\\3cimg\\20src\\3dx\\20onerror\\3d alert\\28 1\\29\\3et-family:'foobar'"
            ).toLowerCase(),
            not( containsString( "onerror" ) )
        );
        assertThat(
            sanitize.css( "filter: 'expression(alert(1))'" ).toLowerCase(),
            not( containsString( "filter" ) )
        );
        assertThat(
            sanitize.css( "font-family: 'foo\\27\\3b color\\3a expression\\28 alert('XSS'))/* ,bar'" ).toLowerCase(),
            not( containsString( "font-family" ) )
        );
    }

    @Test
    public void sanitizeJson()
    {
        assertThat( sanitize.json( "\uffef\u000042\u0008\ud800\uffff\udc00" ), equalTo( "42" ) );
    }

    @Test
    public void encodeForHtml()
    {
        assertThat( sanitize.forHtml( ">" ), equalTo( "&gt;" ) );
    }

    @Test
    public void encodeForCss()
    {
        assertThat( sanitize.forCssString( "'1" ), equalTo( "\\27 1" ) );
        assertThat( sanitize.forCssString( "'x" ), equalTo( "\\27x" ) );
        assertThat( sanitize.forCssString( "\0" ), equalTo( "\\0" ) );

        assertThat( sanitize.forCssUrl( "'1" ), equalTo( "\\27 1" ) );
        assertThat( sanitize.forCssUrl( "'x" ), equalTo( "\\27x" ) );
        assertThat( sanitize.forCssUrl( "\0" ), equalTo( "\\0" ) );
    }

    @Test
    public void encodeForJavascript()
    {
        assertThat( sanitize.forJavascript( "\'" ), equalTo( "\\x27" ) );
    }

    @Test
    public void encodeForXml()
    {
        assertThat( sanitize.forXml( "unencoded & encoded" ), equalTo( "unencoded &amp; encoded" ) );
        assertThat( sanitize.forCDATA( "]]>" ), equalTo( "]]>]]<![CDATA[>" ) );
    }

    private static <T> Matcher<T> noneOf( Matcher<? super T>... matchers )
    {
        return not( anyOf( matchers ) );
    }
}
