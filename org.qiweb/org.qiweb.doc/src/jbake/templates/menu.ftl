	<!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html">QiWeb Documentation</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>getting-started.html">Getting started</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>guides.html">Guides</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>manual.html">Manual</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>modules/index.html">Modules</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>api/index.html" target="_blank">API Javadoc</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>release-notes.html">Release notes</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
    <div class="container">