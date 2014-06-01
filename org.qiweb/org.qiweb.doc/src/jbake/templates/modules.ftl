<#include "header.ftl">

	<#include "menu.ftl">

    <ol class="breadcrumb">
        <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html">Documentation</a></li>
        <li class="active">Modules</li>
    </ol>

	<div class="page-header">
		<h1><#escape x as x?xml>${content.title}</#escape></h1>
	</div>

	<p>${content.body}</p>

    <#list modules as module>
        <#if (module.status == "published")>
            <h2><#escape x as x?xml>${module.title}</#escape></h2>
            <div class="paragraph">
                <p>
                    <a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>${module.uri}">Documentation</a>
                </p>
            </div>
        </#if>
    </#list>

<#include "footer.ftl">
