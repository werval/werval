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
          <a class="navbar-brand" href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>index.html">QiWeb ${config.qiweb_version}</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">Getting started <b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>get-started-gradle.html">Get started using Gradle</a></li>
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>get-started-maven.html">Get started using Maven</a></li>
                <li class="divider"></li>
                <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>getting-started.html">What are Gradle & Maven?</a></li>
              </ul>
            </li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>guides.html">Guides</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>manual.html">Manual</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>modules/index.html">Modules</a></li>
            <li><a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>api/index.html" target="_blank">API Javadoc</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
    <div class="container">