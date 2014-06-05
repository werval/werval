<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??><#escape x as x?xml>${content.title}</#escape><#else>QiWeb</#if></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="QiWeb Documentation">
    <meta name="keywords" content="qiweb, documentation">

    <!-- Le styles -->
    <!-- See https://github.com/nerk/asciidoctor-bs-themes -->
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/bootstrap_lumen.css" rel="stylesheet">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/base.css" rel="stylesheet">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/org.qiweb.doc.css" rel="stylesheet">
    <link href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>css/prettify.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/html5shiv.min.js"></script>
    <![endif]-->
  </head>
  <body onload="prettyPrint()" class="article toc2 toc-right">
    <div id="wrap">
   