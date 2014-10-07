		</div>
		<div id="push"></div>
    </div>

    <div id="footer">
        <p class="muted credit">&copy; ${.now?string("yyyy")} | QiWeb ${config.qiweb_version} | The QiWeb Community</p>
        <p class="muted credit">Mixed with <a href="http://getbootstrap.com/">Bootstrap</a> | Baked with <a href="http://jbake.org">JBake</a></p>
      </div>
    </div>

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/jquery-1.11.1.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/bootstrap.min.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/prettify.js"></script>
    <script src="<#if (content.rootpath)??>${content.rootpath}<#else></#if>js/org.qiweb.doc.js"></script>

  </body>
</html>