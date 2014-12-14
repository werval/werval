<#include "header.ftl">

	<#include "menu.ftl">

    <ol class="breadcrumb">
        <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html">Documentation</a></li>
        <li class="active"><#escape x as x?xml>${content.title}</#escape></li>
    </ol>

	<div class="page-header">
		<h1><#escape x as x?xml>${content.title}</#escape></h1>
	</div>

	<p>${content.body}</p>

<#include "footer.ftl">