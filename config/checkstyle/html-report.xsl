<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="html" indent="yes"/>
    <xsl:decimal-format decimal-separator="." grouping-separator="," />

    <xsl:key name="files" match="file" use="@name" />

    <xsl:template match="checkstyle">
        <html>
            <head>
                <style type="text/css">
                    .bannercell {
                    border: 0px;
                    padding: 0px;
                    }
                    body {
                    margin-left: 10;
                    margin-right: 10;
                    font:normal 80% arial,helvetica,sanserif;
                    background-color:#FFFFFF;
                    color:#000000;
                    }
                    .a td {
                    background: #efefef;
                    }
                    .b td {
                    background: #fff;
                    }
                    th, td {
                    text-align: left;
                    vertical-align: top;
                    }
                    th {
                    font-weight:bold;
                    background: #ccc;
                    color: black;
                    }
                    table, th, td {
                    font-size:100%;
                    border: none
                    }
                    table.log tr td, tr th {

                    }
                    h2 {
                    font-weight:bold;
                    font-size:140%;
                    margin-bottom: 5;
                    }
                    h3 {
                    font-size:100%;
                    font-weight:bold;
                    background: #525D76;
                    color: white;
                    text-decoration: none;
                    padding: 5px;
                    margin-right: 2px;
                    margin-left: 2px;
                    margin-bottom: 0;
                    }
                </style>
            </head>
            <body>
                <a name="top"></a>
                <h2>CheckStyle Report</h2>
                <hr size="1"/>

                <!-- Summary part -->
                <xsl:apply-templates select="." mode="summary"/>
                <hr size="1" width="100%" align="left"/>

                <!-- Package List part -->
                <xsl:apply-templates select="." mode="filelist"/>
                <hr size="1" width="100%" align="left"/>

                <!-- For each package create its part -->
                <xsl:apply-templates select="file[@name and generate-id(.) = generate-id(key('files', @name))]" />

                <hr size="1" width="100%" align="left"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="checkstyle" mode="filelist">
        <h3>Files</h3>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th>Name</th>
                <th style="width: 10%; background-color: #99ee99">Informations</th>
                <th style="width: 10%; background-color: #ffbb00">Warnings</th>
                <th style="width: 10%; background-color: #ee9999">Errors</th>
            </tr>
            <xsl:for-each select="file[@name and generate-id(.) = generate-id(key('files', @name))]">
                <!-- Sort -->
                <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error[@severity='error'])"/>
                <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error[@severity='warning'])"/>
                <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error[@severity='info'])"/>
                <!-- Filter -->
                <xsl:variable name="infoCount" select="count(error[@severity='info'])"/>
                <xsl:variable name="warningCount" select="count(error[@severity='warning'])"/>
                <xsl:variable name="errorCount" select="count(error[@severity='error'])"/>
                <xsl:if test="( $infoCount + $warningCount + $errorCount ) > 0">
                    <!-- Output -->
                    <tr>
                        <xsl:call-template name="alternated-row"/>
                        <td>
                            <a href="#f-{@name}">
                                <xsl:value-of select="@name"/>
                            </a>
                        </td>
                        <td>
                            <xsl:value-of select="$infoCount"/>
                        </td>
                        <td>
                            <xsl:value-of select="$warningCount"/>
                        </td>
                        <td>
                            <xsl:value-of select="$errorCount"/>
                        </td>
                    </tr>
                </xsl:if>
            </xsl:for-each>
        </table>
    </xsl:template>


    <xsl:template match="file">
        <xsl:variable name="infoCount" select="count(error[@severity='info'])"/>
        <xsl:variable name="warningCount" select="count(error[@severity='warning'])"/>
        <xsl:variable name="errorCount" select="count(error[@severity='error'])"/>
        <xsl:if test="( $infoCount + $warningCount + $errorCount ) > 0">

            <a name="f-{@name}"></a>
            <h3>File <xsl:value-of select="@name"/></h3>

            <xsl:if test="$errorCount > 0">
                <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
                    <tr>
                        <th style="background-color: #ee9999">Errors</th>
                        <th style="width: 10%; background-color: #ee9999">Line</th>
                    </tr>
                    <xsl:for-each select="key('files', @name)/error[@severity='error']">
                        <xsl:sort data-type="number" order="ascending" select="@line"/>
                        <tr>
                            <xsl:call-template name="alternated-row"/>
                            <td>
                                <xsl:value-of select="@message"/>
                            </td>
                            <td>
                                <xsl:value-of select="@line"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </xsl:if>
            <xsl:if test="$warningCount > 0">
                <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
                    <tr>
                        <th style="background-color: #ffbb00">Warnings</th>
                        <th style="width: 10%; background-color: #ffbb00">Line</th>
                    </tr>
                    <xsl:for-each select="key('files', @name)/error[@severity='warning']">
                        <xsl:sort data-type="number" order="ascending" select="@line"/>
                        <tr>
                            <xsl:call-template name="alternated-row"/>
                            <td>
                                <xsl:value-of select="@message"/>
                            </td>
                            <td>
                                <xsl:value-of select="@line"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </xsl:if>
            <xsl:if test="$infoCount > 0">
                <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
                    <tr>
                        <th style="background-color: #99ee99">Informations</th>
                        <th style="width: 10%; background-color: #99ee99">Line</th>
                    </tr>
                    <xsl:for-each select="key('files', @name)/error[@severity='info']">
                        <xsl:sort data-type="number" order="ascending" select="@line"/>
                        <tr>
                            <xsl:call-template name="alternated-row"/>
                            <td>
                                <xsl:value-of select="@message"/>
                            </td>
                            <td>
                                <xsl:value-of select="@line"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </xsl:if>
            <a href="#top">Back to top</a>
        </xsl:if>
    </xsl:template>


    <xsl:template match="checkstyle" mode="summary">
        <h3>Summary</h3>
        <xsl:variable name="totalFileCount" select="count(file[@name and generate-id(.) = generate-id(key('files', @name))])"/>
        <xsl:variable name="infoCount" select="count(file/error[@severity='info'])"/>
        <xsl:variable name="warningCount" select="count(file/error[@severity='warning'])"/>
        <xsl:variable name="errorCount" select="count(file/error[@severity='error'])"/>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th style="width: 25%">Files</th>
                <th style="width: 25%; background-color: #99ee99">Informations</th>
                <th style="width: 25%; background-color: #ffbb00">Warnings</th>
                <th style="width: 25%; background-color: #ee9999">Errors</th>
            </tr>
            <tr>
                <xsl:call-template name="alternated-row"/>
                <td>
                    <xsl:value-of select="$totalFileCount"/>
                </td>
                <td>
                    <xsl:value-of select="$infoCount"/>
                </td>
                <td>
                    <xsl:value-of select="$warningCount"/>
                </td>
                <td>
                    <xsl:value-of select="$errorCount"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <xsl:template name="alternated-row">
        <xsl:attribute name="class">
            <xsl:if test="position() mod 2 = 1">a</xsl:if>
            <xsl:if test="position() mod 2 = 0">b</xsl:if>
        </xsl:attribute>
    </xsl:template>

</xsl:stylesheet>
