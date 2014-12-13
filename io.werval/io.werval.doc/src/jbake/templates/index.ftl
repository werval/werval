<#include "header.ftl">
	
	<#include "menu.ftl">

	<div class="page-header">
		<h1><img src="images/logo.png" alt="Logo" height="84"/> Werval ${config.werval_version} Documentation</h1>
	</div>

    <div id="content">
        <div class="row">
            <div class="col-md-4">
                <h4>Getting Started</h4>
                <p>Get started using the build tool of your choice.</p>
                <p>
                    Get Started <a href="get-started-gradle.html">using Gradle</a>
                    <br/>
                    Get Started <a href="get-started-maven.html">using Maven</a>
                </p>
            </div>
            <div class="col-md-4">
                <h4>Guides</h4>
                <p>Browse a growing list of howto-like guides covering HTTP applications development, testing and production.</p>
                <p><a href="guides.html">Guides</a></p>
            </div>
            <div class="col-md-4">
                <h4>Manual</h4>
                <p>Read the comprehensive Werval manual to master every aspects of HTTP applications development.</p>
                <p><a href="manual.html">Manual</a></p>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <h4>API</h4>
                <p>Read the reference documentation of the Werval Core API, SPI and Test Support.</p>
                <p><a href="api/index.html" target="_blank">Werval API</a></p>
            </div>
            <div class="col-md-6">
                <h4>Modules</h4>
                <p>Leverage high quality modules allowing you to build awesome HTTP applications.</p>
                <p><a href="modules/index.html">Modules</a></p>
            </div>
        </div>
    </div>

<#include "footer.ftl">