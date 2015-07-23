<#-- @ftlvariable name="" type="com.example.views.MessageView" -->
<html>
    <body>
        <!-- calls getPerson().getFullName() and sanitizes it -->
        <h1>Message ${message.payload?html}!</h1>
    </body>
</html>